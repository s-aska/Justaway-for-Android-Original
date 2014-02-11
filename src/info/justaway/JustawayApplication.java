package info.justaway;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.LongSparseArray;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;

import info.justaway.contextmenu.TweetContextMenu;
import info.justaway.display.FadeInRoundedBitmapDisplayer;
import info.justaway.task.DestroyStatusTask;
import info.justaway.task.FavoriteTask;
import info.justaway.task.RetweetTask;
import info.justaway.task.UnFavoriteTask;
import info.justaway.task.UnRetweetTask;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.UserList;
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
    private static Typeface sFontello;
    private ResponseList<UserList> mUserLists;
    private static ProgressDialog mProgressDialog;

    public ResponseList<UserList> getUserLists() {
        return mUserLists;
    }

    public void setUserLists(ResponseList<UserList> userLists) {
        mUserLists = userLists;
    }

    public UserList getUserList(int id) {
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

        resetFontSize();
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
        sImageLoader.displayImage(url, view, sRoundedDisplayImageOptions);
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
    private ArrayList<Integer> mTabs = new ArrayList<Integer>();

    public ArrayList<Integer> loadTabs() {
        SharedPreferences preferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        String tabs_string = preferences.getString(TABS.concat(String.valueOf(getUserId())), "-1,-2,-3");
        String[] tabs_strings = tabs_string.split(",");
        mTabs.clear();
        for (String tab_string : tabs_strings) {
            mTabs.add(Integer.valueOf(tab_string));
        }
        return mTabs;
    }

    public void saveTabs(ArrayList<Integer> tabs) {
        mTabs = tabs;
        ArrayList<String> tabs_strings = new ArrayList<String>();
        for (Integer tab : tabs) {
            tabs_strings.add(String.valueOf(tab));
        }
        SharedPreferences preferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putString(TABS.concat(String.valueOf(getUserId())), TextUtils.join(",", tabs_strings));
        editor.commit();
    }

    public boolean existsTab(Integer findTab) {
        for (Integer tab : mTabs) {
            if (tab.equals(findTab)) {
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
     * 設定管理
     */
    private static final String PREF_NAME_SETTINGS = "settings";
    private int mFontSize;

    public boolean getKeepScreenOn() {
        SharedPreferences preferences = getSharedPreferences(PREF_NAME_SETTINGS, Context.MODE_PRIVATE);
        return preferences.getBoolean("keep_screen_on", true);
    }

    public int getFontSize() {
        return mFontSize;
    }

    public void resetFontSize() {
        SharedPreferences preferences = getSharedPreferences(PREF_NAME_SETTINGS, Context.MODE_PRIVATE);
        mFontSize = Integer.parseInt(preferences.getString("font_size", "12"));
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
        Gson gson = new Gson();
        JustawayApplication.AccountSettings accountSettings = gson.fromJson(json, JustawayApplication.AccountSettings.class);

        return accountSettings.accessTokens;
    }

    /**
     * Twitterアクセストークン取得
     *
     * @return Twitterアクセストークン
     */
    private AccessToken getAccessToken() {
        // キャッシュしておく
        if (mAccessToken != null) {
            return mAccessToken;
        }

        SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = preferences.getString(TOKENS, null);

        if (json != null) {
            Gson gson = new Gson();
            AccountSettings accountSettings = gson.fromJson(json, AccountSettings.class);
            this.mAccessToken = accountSettings.accessTokens.get(accountSettings.index);

            return mAccessToken;
        } else {
            return null;
        }
    }

    /**
     * Twitterアクセストークン保存
     *
     * @param accessToken Twitterアクセストークン
     */
    public void setAccessToken(AccessToken accessToken) {

        this.mAccessToken = accessToken;

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

    private LongSparseArray<Boolean> mIsFavMap = new LongSparseArray<Boolean>();
    private LongSparseArray<Long> mRtIdMap = new LongSparseArray<Long>();

    public void setFav(Long id) {
        mIsFavMap.put(id, true);
    }

    public void removeFav(Long id) {
        mIsFavMap.remove(id);
    }

    public Boolean isFav(Status status) {
        if (status.isFavorited()) {
            return true;
        }
        if (mIsFavMap.get(status.getId()) != null) {
            return true;
        }
        Status retweet = status.getRetweetedStatus();
        return retweet != null && ((mIsFavMap.get(retweet.getId()) != null) || retweet.isFavorited());
    }

    public void setRtId(Long sourceId, Long retweetId) {
        mRtIdMap.put(sourceId, retweetId);
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

    public void doRetweet(Long id) {
        sApplication.setRtId(id, (long) 0);
        new RetweetTask().execute(id);
    }

    public void doDestroyRetweet(Status status) {
        if (status.getUser().getId() == getUserId()) {
            // 自分がRTしたStatus
            Status retweet = status.getRetweetedStatus();
            if (retweet != null) {
                mRtIdMap.remove(retweet.getId());
            }
            new UnRetweetTask().execute(status.getId());
        } else {
            // 他人のStatusで、それを自分がRTしている
            Long statusId = mRtIdMap.get(status.getId());
            if (statusId != null && statusId > 0) {
                // そのStatusそのものをRTしている
                mRtIdMap.remove(status.getId());
            } else {
                Status retweet = status.getRetweetedStatus();
                if (retweet != null) {
                    statusId = mRtIdMap.get(retweet.getId());
                    if (statusId != null && statusId > 0) {
                        // そのStatusがRTした元StatusをRTしている
                        mRtIdMap.remove(retweet.getId());
                    }
                }
            }

            if (statusId != null && statusId == 0L) {
                // 処理中は 0
                JustawayApplication.showToast(R.string.toast_destroy_retweet_progress);
            } else if (statusId != null && statusId > 0) {
                new UnRetweetTask().execute(statusId);
            }
        }
    }

    public void doDestroyStatus(long id) {
        new DestroyStatusTask().execute(id);
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

    private TweetContextMenu mTweetContextMenu;

    public void onCreateContextMenu(FragmentActivity activity, ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        mTweetContextMenu = new TweetContextMenu(activity, menu, v, menuInfo);
    }

    public boolean onContextItemSelected(MenuItem item) {
        return mTweetContextMenu.onContextItemSelected(item);
    }

}
