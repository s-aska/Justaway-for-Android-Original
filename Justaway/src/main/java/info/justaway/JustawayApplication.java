package info.justaway;

import android.app.Activity;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v4.util.LongSparseArray;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.HashMap;

import de.greenrobot.event.EventBus;
import info.justaway.adapter.MyUserStreamAdapter;
import info.justaway.display.FadeInRoundedBitmapDisplayer;
import info.justaway.event.action.AccountChangeEvent;
import info.justaway.event.connection.CleanupEvent;
import info.justaway.event.action.EditorEvent;
import info.justaway.event.connection.ConnectEvent;
import info.justaway.event.connection.DisconnectEvent;
import info.justaway.listener.MyConnectionLifeCycleListener;
import info.justaway.model.Row;
import info.justaway.settings.MuteSettings;
import info.justaway.task.FavoriteTask;
import info.justaway.task.RetweetTask;
import info.justaway.task.UnFavoriteTask;
import info.justaway.task.UnRetweetTask;
import twitter4j.DirectMessage;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.UserMentionEntity;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

/**
 * アプリケーション、アクティビティ間でのデータの共有などに利用
 *
 * @author aska
 */
public class JustawayApplication extends Application {

    private static JustawayApplication sApplication;
    private static ImageLoader sImageLoader;
    private static DisplayImageOptions sRoundedDisplayImageOptions;
    private static MuteSettings sMuteSettings;
    private static Typeface sFontello;
    private ResponseList<UserList> mUserLists;
    private static ProgressDialog mProgressDialog;

    public ResponseList<UserList> getUserLists() {
        return mUserLists;
    }

    public void setUserLists(ResponseList<UserList> userLists) {
        mUserLists = userLists;
    }

    public UserList getUserList(long id) {
        if (mUserLists == null) {
            return null;
        }
        for (UserList userList : mUserLists) {
            if (userList.getId() == id) {
                return userList;
            }
        }
        return null;
    }

    public static JustawayApplication getApplication() {
        return sApplication;
    }

    public static Typeface getFontello() {
        return sFontello;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;

        // Twitter4J の user stream の shutdown() で NetworkOnMainThreadException が発生してしまうことに対する暫定対応
        if (!BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        }

        DisplayImageOptions defaultOptions = new DisplayImageOptions
                .Builder()
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .resetViewBeforeLoading(true)
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration
                .Builder(this)
                .defaultDisplayImageOptions(defaultOptions)
                .build();

        ImageLoader.getInstance().init(config);

        sImageLoader = ImageLoader.getInstance();

        sRoundedDisplayImageOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .resetViewBeforeLoading(true)
                .displayer(new FadeInRoundedBitmapDisplayer(5))
                .build();

        sFontello = Typeface.createFromAsset(getAssets(), "fontello.ttf");

        // 例外発生時の処理を指定（スタックトレースを保存）
        if (BuildConfig.DEBUG) {
            Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtExceptionHandler(sApplication));
        }

        resetDisplaySettings();

        getAccessToken();

        sMuteSettings = new MuteSettings();

        warmUpUserIconMap();
    }

    public void displayImage(String url, ImageView view) {
        String tag = (String) view.getTag();
        if (tag != null && tag.equals(url)) {
            return;
        }
        view.setTag(url);
        sImageLoader.displayImage(url, view);
    }

    public void displayRoundedImage(String url, ImageView view) {
        String tag = (String) view.getTag();
        if (tag != null && tag.equals(url)) {
            return;
        }
        view.setTag(url);
        if (getUserIconRoundedOn()) {
            sImageLoader.displayImage(url, view, sRoundedDisplayImageOptions);
        } else {
            sImageLoader.displayImage(url, view);
        }
    }

    public MuteSettings getMuteSettings() {
        return sMuteSettings;
    }

    public static Boolean isMute(Row row) {
        if (row.isStatus()) {
            return sMuteSettings.isMute(row.getStatus());
        } else {
            return false;
        }
    }

    /**
     * userIdとアイコンの対応、DiskCacheすると「古いアイコン〜〜〜〜」ってなるのでしない。
     */
    private HashMap<String, String> mUserIconMap = new HashMap<String, String>();

    public void displayUserIcon(User user, final ImageView view) {
        String url;
        if (getUserIconSize().equals("bigger")) {
            url = user.getBiggerProfileImageURL();
        } else if (getUserIconSize().equals("normal")) {
            url = user.getProfileImageURL();
        } else if (getUserIconSize().equals("mini")) {
            url = user.getMiniProfileImageURL();
        } else {
            view.setVisibility(View.GONE);
            return;
        }
        if (getUserIconRoundedOn()) {
            displayRoundedImage(url, view);
        } else {
            displayImage(url, view);
        }
    }

    /**
     * userIdからアイコンを取得する
     */
    public void displayUserIcon(final long userId, final ImageView view) {
        String url = mUserIconMap.get(String.valueOf(userId));
        if (url != null) {
            displayRoundedImage(url, view);
            return;
        }

        // すぐにURLが取れない時は一旦消す
        view.setImageDrawable(null);
    }

    /**
     * アプリケーション起動時にキャッシュを温めておく
     * 起動時のネットワーク通信がこれでまた一つ増えてしまった
     */
    private static final String PREF_NAME_USER_ICON_MAP = "user_icon_map";
    private static final String PREF_KEY_USER_ICON_MAP = "data/v2";

    @SuppressWarnings("unchecked")
    public void warmUpUserIconMap() {
        ArrayList<AccessToken> accessTokens = getAccessTokens();
        if (accessTokens == null || accessTokens.size() == 0) {
            return;
        }

        final SharedPreferences preferences = getSharedPreferences(PREF_NAME_USER_ICON_MAP, Context.MODE_PRIVATE);
        final Gson gson = new Gson();
        String json = preferences.getString(PREF_KEY_USER_ICON_MAP, null);
        if (json != null) {
            mUserIconMap = gson.fromJson(json, mUserIconMap.getClass());
        }

        final long userIds[] = new long[accessTokens.size()];
        int i = 0;
        for (AccessToken accessToken : accessTokens) {
            userIds[i] = accessToken.getUserId();
            i++;
        }

        new AsyncTask<Void, Void, ResponseList<User>>() {

            @Override
            protected ResponseList<User> doInBackground(Void... voids) {
                try {
                    return getTwitter().lookupUsers(userIds);
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(ResponseList<User> users) {
                if (users == null) {
                    return;
                }
                mUserIconMap.clear();
                for (User user : users) {
                    mUserIconMap.put(String.valueOf(user.getId()), user.getBiggerProfileImageURL());
                }
                String exportJson = gson.toJson(mUserIconMap);
                SharedPreferences.Editor editor = preferences.edit();
                editor.clear();
                editor.putString(PREF_KEY_USER_ICON_MAP, exportJson);
                editor.commit();

            }
        }.execute();
    }

    /*
     * 終了時
     * 
     * @see android.app.Application#onTerminate()
     */
    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    /*
     * 空きメモリ逼迫時
     * 
     * @see android.app.Application#onLowMemory()
     */
    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    /*
     * 実行時において変更できるデバイスの設定時 ( 画面のオリエンテーション、キーボードの使用状態、および言語など )
     * https://sites.google
     * .com/a/techdoctranslator.com/jp/android/guide/resources/runtime-changes
     * 
     * @see android.app.Application#onConfigurationChanged(android.content.res.
     * Configuration)
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * あると便利な簡易通知
     *
     * @param text 表示するメッセージ
     */
    public static void showToast(String text) {
        Toast.makeText(sApplication, text, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(int id) {
        String text = sApplication.getString(id);
        Toast.makeText(sApplication, text, Toast.LENGTH_SHORT).show();
    }

    public static void showProgressDialog(Context context, String message) {
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage(message);
        mProgressDialog.show();
    }

    public static void dismissProgressDialog() {
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
    }

    /**
     * タブ管理
     */
    private static final String TABS = "tabs-";
    private ArrayList<Tab> mTabs = new ArrayList<Tab>();

    public ArrayList<Tab> loadTabs() {
        mTabs.clear();
        SharedPreferences preferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        String json = preferences.getString(TABS.concat(String.valueOf(getUserId())).concat("/v2"), null);
        if (json != null) {
            Gson gson = new Gson();
            TabData tabData = gson.fromJson(json, TabData.class);
            mTabs = tabData.tabs;
        }
        if (mTabs.size() == 0) {
            mTabs = generalTabs();
        }
        return mTabs;
    }

    public void saveTabs(ArrayList<Tab> tabs) {
        TabData tabData = new TabData();
        tabData.tabs = tabs;
        Gson gson = new Gson();
        String json = gson.toJson(tabData);
        SharedPreferences preferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.remove(TABS.concat(String.valueOf(getUserId())));
        editor.putString(TABS.concat(String.valueOf(getUserId())).concat("/v2"), json);
        editor.commit();
        mTabs = tabs;
    }

    public ArrayList<Tab> generalTabs() {
        ArrayList<Tab> tabs = new ArrayList<Tab>();
        tabs.add(new Tab(-1L));
        tabs.add(new Tab(-2L));
        tabs.add(new Tab(-3L));
        return tabs;
    }

    public static class TabData {
        ArrayList<Tab> tabs;
    }

    public static class Tab {
        public Long id;
        public String name;

        public Tab(Long id) {
            this.id = id;
        }

        public String getName() {
            if (id == -1L) {
                return getApplication().getString(R.string.title_main);
            } else if (id == -2L) {
                return getApplication().getString(R.string.title_interactions);
            } else if (id == -3L) {
                return getApplication().getString(R.string.title_direct_messages);
            } else {
                return name;
            }
        }

        public int getIcon() {
            if (id == -1L) {
                return R.string.fontello_home;
            } else if (id == -2L) {
                return R.string.fontello_at;
            } else if (id == -3L) {
                return R.string.fontello_mail;
            } else {
                return R.string.fontello_list;
            }
        }
    }

    public boolean hasTabId(Long findTab) {
        for (Tab tab : mTabs) {
            if (tab.id.equals(findTab)) {
                return true;
            }
        }
        return false;
    }

    /**
     * クックモードの記憶
     */
    private static final String QUICK_MODE = "quickMode";

    public void setQuickMod(Boolean quickMode) {
        SharedPreferences preferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putBoolean(QUICK_MODE, quickMode);
        editor.commit();
    }

    public Boolean getQuickMode() {
        SharedPreferences preferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        return preferences.getBoolean(QUICK_MODE, false);
    }

    /**
     * ストリーミングモードの記憶
     */
    private static final String STREAMING_MODE = "streamingMode";
    private Boolean mStreamingMode;

    public void setStreamingMode(Boolean streamingMode) {
        SharedPreferences preferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putBoolean(STREAMING_MODE, streamingMode);
        editor.commit();
        mStreamingMode = streamingMode;
    }

    public Boolean getStreamingMode() {
        if (mStreamingMode != null) {
            return mStreamingMode;
        }
        SharedPreferences preferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        return preferences.getBoolean(STREAMING_MODE, true);
    }

    /**
     * 設定管理
     */
    private static final String PREF_NAME_SETTINGS = "settings";
    private int mFontSize;
    private String mLongTapAction;
    private String mThemeName;
    private Boolean mUserIconRounded;
    private Boolean mDisplayThumbnail;
    private String mUserIconSize;
    private int mPageCount;

    public boolean getKeepScreenOn() {
        SharedPreferences preferences = getSharedPreferences(PREF_NAME_SETTINGS, Context.MODE_PRIVATE);
        return preferences.getBoolean("keep_screen_on", true);
    }

    public int getFontSize() {
        return mFontSize;
    }

    public String getLongTapAction() {
        return mLongTapAction;
    }

    public void setTheme(Activity activity) {
        if (mThemeName.equals("black")) {
            activity.setTheme(R.style.BlackTheme);
        } else {
            activity.setTheme(R.style.WhiteTheme);
        }
    }

    public void setThemeTextColor(Activity activity, TextView view, int resourceId) {
        TypedValue outValue = new TypedValue();
        Resources.Theme theme = activity.getTheme();
        if (theme != null) {
            theme.resolveAttribute(resourceId, outValue, true);
            view.setTextColor(outValue.data);
        }
    }

    public String getThemeName() {
        return mThemeName;
    }

    public int getThemeTextColor(Activity activity, int resourceId) {
        TypedValue outValue = new TypedValue();
        Resources.Theme theme = activity.getTheme();
        if (theme != null) {
            theme.resolveAttribute(resourceId, outValue, true);
        }
        return outValue.data;
    }

    public void resetDisplaySettings() {
        SharedPreferences preferences = getSharedPreferences(PREF_NAME_SETTINGS, Context.MODE_PRIVATE);
        mFontSize = Integer.parseInt(preferences.getString("font_size", "12"));
        mLongTapAction = preferences.getString("long_tap", "nothing");
        mThemeName = preferences.getString("themeName", "black");
        mUserIconRounded = preferences.getBoolean("user_icon_rounded_on", true);
        mUserIconSize = preferences.getString("user_icon_size", "bigger");
        mDisplayThumbnail = preferences.getBoolean("display_thumbnail_on", true);
        mPageCount = Integer.parseInt(preferences.getString("page_count", "200"));
    }

    public boolean getUserIconRoundedOn() {
        if (mUserIconRounded != null) {
            return mUserIconRounded;
        }
        SharedPreferences preferences = getSharedPreferences(PREF_NAME_SETTINGS, Context.MODE_PRIVATE);
        mUserIconRounded = preferences.getBoolean("user_icon_rounded_on", true);
        return mUserIconRounded;
    }

    public String getUserIconSize() {
        if (mUserIconSize != null) {
            return mUserIconSize;
        }
        SharedPreferences preferences = getSharedPreferences(PREF_NAME_SETTINGS, Context.MODE_PRIVATE);
        mUserIconSize = preferences.getString("user_icon_size", "bigger");
        return mUserIconSize;
    }

    public boolean getDisplayThumbnailOn() {
        if (mDisplayThumbnail != null) {
            return mDisplayThumbnail;
        }
        SharedPreferences preferences = getSharedPreferences(PREF_NAME_SETTINGS, Context.MODE_PRIVATE);
        mDisplayThumbnail = preferences.getBoolean("display_thumbnail_on", true);
        return mDisplayThumbnail;
    }

    public int getPageCount() {
        if (mPageCount > 0) {
            return mPageCount;
        }
        SharedPreferences preferences = getSharedPreferences(PREF_NAME_SETTINGS, Context.MODE_PRIVATE);
        mPageCount = Integer.parseInt(preferences.getString("page_count", "200"));
        return mPageCount;
    }

    /**
     * Twitterインスタンス管理
     */
    private static final String TOKENS = "tokens";
    private static final String PREF_NAME = "twitter_access_token";
    private AccessToken mAccessToken;
    private Twitter mTwitter;

    public long getUserId() {
        if (mAccessToken == null) {
            return -1L;
        }
        return mAccessToken.getUserId();
    }

    public String getScreenName() {
        if (mAccessToken == null) {
            return "";
        }
        return mAccessToken.getScreenName();
    }

    private String getConsumerKey() {
        return getString(R.string.twitter_consumer_key);
    }

    private String getConsumerSecret() {
        return getString(R.string.twitter_consumer_secret);
    }

    /**
     * Twitterアクセストークン有無
     *
     * @return Twitterアクセストークン有無
     */
    public Boolean hasAccessToken() {
        return getAccessToken() != null;
    }

    public ArrayList<AccessToken> getAccessTokens() {
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = preferences.getString(TOKENS, null);
        if (json == null) {
            return null;
        }

        Gson gson = new Gson();
        JustawayApplication.AccountSettings accountSettings = gson.fromJson(json, JustawayApplication.AccountSettings.class);
        return accountSettings.accessTokens;
    }

    /**
     * Twitterアクセストークン取得
     *
     * @return Twitterアクセストークン
     */
    public AccessToken getAccessToken() {
        // キャッシュしておく
        if (mAccessToken != null) {
            return mAccessToken;
        }

        SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = preferences.getString(TOKENS, null);
        if (json == null) {
            return null;
        }

        Gson gson = new Gson();
        AccountSettings accountSettings = gson.fromJson(json, AccountSettings.class);
        mAccessToken = accountSettings.accessTokens.get(accountSettings.index);
        return mAccessToken;
    }

    /**
     * Twitterアクセストークン保存
     *
     * @param accessToken Twitterアクセストークン
     */
    public void setAccessToken(AccessToken accessToken) {

        mAccessToken = accessToken;

        getTwitter().setOAuthAccessToken(mAccessToken);

        SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = preferences.getString(TOKENS, null);
        Gson gson = new Gson();

        AccountSettings accountSettings;
        if (json != null) {
            accountSettings = gson.fromJson(json, AccountSettings.class);

            boolean existUser = false;
            int i = 0;
            for (AccessToken sharedAccessToken : accountSettings.accessTokens) {
                if (accessToken.getUserId() == sharedAccessToken.getUserId()) {
                    accountSettings.accessTokens.set(i, accessToken);
                    accountSettings.index = i;
                    existUser = true;
                }
                i++;
            }

            if (!existUser) {
                accountSettings.index = accountSettings.accessTokens.size();
                accountSettings.accessTokens.add(mAccessToken);
            }
        } else {
            accountSettings = new AccountSettings();
            accountSettings.accessTokens = new ArrayList<AccessToken>();
            accountSettings.accessTokens.add(mAccessToken);
        }

        String exportJson = gson.toJson(accountSettings);

        Editor editor = preferences.edit();
        editor.putString(TOKENS, exportJson);
        editor.commit();
    }

    public static class AccountSettings {
        int index;
        ArrayList<AccessToken> accessTokens;
    }

    /**
     * Twitterインスタンス(アクセストークン付き)を取得
     *
     * @return Twitterインスタンス
     */
    public Twitter getTwitter() {
        if (mTwitter != null) {
            return mTwitter;
        }
        Twitter twitter = getTwitterInstance();

        AccessToken token = getAccessToken();
        if (token != null) {
            twitter.setOAuthAccessToken(token);
            // アクセストークンまである時だけキャッシュしておく
            this.mTwitter = twitter;
        }
        return twitter;
    }

    /**
     * Twitterインスタンスを取得
     *
     * @return Twitterインスタンス
     */
    public Twitter getTwitterInstance() {

        TwitterFactory factory = new TwitterFactory();
        Twitter twitter = factory.getInstance();
        twitter.setOAuthConsumer(getConsumerKey(), getConsumerSecret());

        return twitter;
    }

    /**
     * TwitterStreamインスタンスを取得
     *
     * @return TwitterStreamインスタンス
     */
    public TwitterStream getTwitterStream() {
        AccessToken token = getAccessToken();
        if (token == null) {
            return null;
        }
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        twitter4j.conf.Configuration conf = configurationBuilder.setOAuthConsumerKey(getConsumerKey())
                .setOAuthConsumerSecret(getConsumerSecret()).setOAuthAccessToken(token.getToken())
                .setOAuthAccessTokenSecret(token.getTokenSecret()).build();
        return new TwitterStreamFactory(conf).getInstance();
    }

    private TwitterStream mTwitterStream;
    private boolean mTwitterStreamConnected;
    private MyUserStreamAdapter mUserStreamAdapter;

    public void startStreaming() {
        if (mTwitterStream != null) {
            if (!mTwitterStreamConnected) {
                mUserStreamAdapter.start();
                mTwitterStream.setOAuthAccessToken(getAccessToken());
                mTwitterStream.user();
            }
            return;
        }
        mTwitterStream = getTwitterStream();
        mUserStreamAdapter = new MyUserStreamAdapter();
        mTwitterStream.addListener(mUserStreamAdapter);
        mTwitterStream.addConnectionLifeCycleListener(new MyConnectionLifeCycleListener());
        mTwitterStream.user();
    }

    public void stopStreaming() {
        if (mTwitterStream == null) {
            return;
        }
        mStreamingMode = false;
        mUserStreamAdapter.stop();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                mTwitterStream.cleanUp();
                mTwitterStream.shutdown();
                return null;
            }

            @Override
            protected void onPostExecute(Void status) {

            }
        }.execute();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(ConnectEvent event) {
        mTwitterStreamConnected = true;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(DisconnectEvent event) {
        mTwitterStreamConnected = false;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(CleanupEvent event) {
        mTwitterStreamConnected = false;
    }

    public void removeAccessToken(int position) {
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = preferences.getString(TOKENS, null);
        Gson gson = new Gson();

        AccountSettings accountSettings = gson.fromJson(json, AccountSettings.class);
        accountSettings.accessTokens.remove(position);
        if (accountSettings.index > position) {
            accountSettings.index--;
        }

        String exportJson = gson.toJson(accountSettings);

        Editor editor = preferences.edit();
        editor.putString(TOKENS, exportJson);
        editor.commit();
    }

    public void switchAccessToken(AccessToken accessToken) {
        setAccessToken(accessToken);
        if (getStreamingMode()) {
            stopStreaming();
        }
        EventBus.getDefault().post(new AccountChangeEvent());
    }

    private LongSparseArray<Boolean> mIsFavMap = new LongSparseArray<Boolean>();
    private LongSparseArray<Long> mRtIdMap = new LongSparseArray<Long>();

    public void setFav(Long id) {
        mIsFavMap.put(id, true);
    }

    public void removeFav(Long id) {
        mIsFavMap.remove(id);
    }

    public Boolean isFav(Status status) {
        if (mIsFavMap.get(status.getId(), false)) {
            return true;
        }
        Status retweet = status.getRetweetedStatus();
        return retweet != null && (mIsFavMap.get(retweet.getId(), false));
    }

    public void setRtId(Long sourceId, Long retweetId) {
        if (retweetId != null) {
            mRtIdMap.put(sourceId, retweetId);
        } else {
            mRtIdMap.remove(sourceId);
        }
    }

    public Long getRtId(Status status) {
        Long id = mRtIdMap.get(status.getId());
        if (id != null) {
            return id;
        }
        Status retweet = status.getRetweetedStatus();
        if (retweet != null) {
            return mRtIdMap.get(retweet.getId());
        }
        return null;
    }

    public void doFavorite(Long statusId) {
        new FavoriteTask(statusId).execute();
    }

    public void doDestroyFavorite(Long statusId) {
        new UnFavoriteTask(statusId).execute();
    }

    public void doRetweet(Long statusId) {
        new RetweetTask(statusId).execute();
    }

    public void doDestroyRetweet(Status status) {

        if (status.getUser().getId() == getUserId()) {
            // 自分がRTしたStatus
            Status retweet = status.getRetweetedStatus();
            if (retweet != null) {
                new UnRetweetTask(retweet.getId(), status.getId()).execute();
            }
        } else {
            // 他人のStatusで、それを自分がRTしている

            // 被リツイート
            Long retweetedStatusId = -1L;

            // リツイート
            Long statusId = mRtIdMap.get(status.getId());
            if (statusId != null && statusId > 0) {
                // そのStatusそのものをRTしている
                retweetedStatusId = status.getId();
            } else {
                Status retweet = status.getRetweetedStatus();
                if (retweet != null) {
                    statusId = mRtIdMap.get(retweet.getId());
                    if (statusId != null && statusId > 0) {
                        // そのStatusがRTした元StatusをRTしている
                        retweetedStatusId = retweet.getId();
                    }
                }
            }

            if (statusId != null && statusId == 0L) {
                // 処理中は 0
                JustawayApplication.showToast(R.string.toast_destroy_retweet_progress);
            } else if (statusId != null && statusId > 0) {
                new UnRetweetTask(retweetedStatusId, statusId).execute();
            }
        }
    }

    public void doReply(Status status, Context context) {
        UserMentionEntity[] mentions = status.getUserMentionEntities();
        String text;
        if (status.getUser().getId() == getUserId() && mentions.length == 1) {
            text = "@" + mentions[0].getScreenName() + " ";
        } else {
            text = "@" + status.getUser().getScreenName() + " ";
        }
        if (context instanceof MainActivity) {
            EventBus.getDefault().post(new EditorEvent(text, status, text.length(), null));
        } else {
            Intent intent = new Intent(context, PostActivity.class);
            intent.putExtra("status", text);
            intent.putExtra("selection", text.length());
            intent.putExtra("inReplyToStatus", status);
            context.startActivity(intent);
        }
    }

    public void doReplyAll(Status status, Context context) {
        UserMentionEntity[] mentions = status.getUserMentionEntities();
        String text = "";
        int selection_start = 0;
        if (status.getUser().getId() != getUserId()) {
            text = "@" + status.getUser().getScreenName() + " ";
            selection_start = text.length();
        }
        for (UserMentionEntity mention : mentions) {
            if (status.getUser().getId() == mention.getId()) {
                continue;
            }
            if (getUserId() == mention.getId()) {
                continue;
            }
            text = text.concat("@" + mention.getScreenName() + " ");
            if (selection_start == 0) {
                selection_start = text.length();
            }
        }
        if (context instanceof MainActivity) {
            EventBus.getDefault().post(new EditorEvent(text, status, selection_start, text.length()));
        } else {
            Intent intent = new Intent(context, PostActivity.class);
            intent.putExtra("status", text);
            intent.putExtra("selection", selection_start);
            intent.putExtra("selection_stop", text.length());
            intent.putExtra("inReplyToStatus", status);
            context.startActivity(intent);
        }
    }

    public void doReplyDirectMessage(DirectMessage directMessage, Context context) {
        String text;
        if (getUserId() == directMessage.getSender().getId()) {
            text = "D " + directMessage.getRecipient().getScreenName() + " ";
        } else {
            text = "D " + directMessage.getSender().getScreenName() + " ";
        }
        if (context instanceof MainActivity) {
            EventBus.getDefault().post(new EditorEvent(text, null, text.length(), null));
        } else {
            Intent intent = new Intent(context, PostActivity.class);
            intent.putExtra("status", text);
            intent.putExtra("selection", text.length());
            context.startActivity(intent);
        }
    }

    public void doQuote(Status status, Context context) {
        String text = " https://twitter.com/"
                + status.getUser().getScreenName()
                + "/status/" + String.valueOf(status.getId());
        if (context instanceof MainActivity) {
            EventBus.getDefault().post(new EditorEvent(text, status, null, null));
        } else {
            Intent intent = new Intent(context, PostActivity.class);
            intent.putExtra("status", text);
            intent.putExtra("inReplyToStatus", status);
            context.startActivity(intent);
        }
    }

    public void showKeyboard(final View view) {
        showKeyboard(view, 200);
    }

    public void showKeyboard(final View view, int delay) {
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(view, 0);
            }
        }, delay);
    }

    @SuppressWarnings("unused")
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public String getClientName(String source) {
        String[] tokens = source.split("[<>]");
        if (tokens.length > 1) {
            return tokens[2];
        } else {
            return tokens[0];
        }
    }

    public boolean isMentionForMe(Status status) {
        long userId = getUserId();
        UserMentionEntity[] mentions = status.getUserMentionEntities();
        for (UserMentionEntity mention : mentions) {
            if (mention.getId() == userId) {
                return true;
            }
        }
        return false;
    }
}
