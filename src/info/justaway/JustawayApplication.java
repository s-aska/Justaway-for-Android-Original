package info.justaway;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.HashMap;

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
    private static ImageLoader mImageLoader;
    private static DisplayImageOptions mRoundedDisplayImageOptions;
    private ResponseList<UserList> mUserLists;

    public ResponseList<UserList> getUserLists() {
        return mUserLists;
    }

    public void setUserLists(ResponseList<UserList> userLists) {
        mUserLists = userLists;
    }

    public String getUserListName(int id) {
        if (mUserLists == null) {
            return null;
        }
        for (UserList userList: mUserLists) {
            if (userList.getId() == id) {
                return userList.getName();
            }
        }
        return null;
    }

    public String getUserListScreenName(int id) {
        if (mUserLists == null) {
            return null;
        }
        for (UserList userList: mUserLists) {
            if (userList.getId() == id) {
                return userList.getUser().getScreenName();
            }
        }
        return null;
    }

    /**
     * 毎回キャストしなくて良いように
     *
     * @return アプリケーションのインスタンス
     */
    public static JustawayApplication getApplication() {
        return sApplication;
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

        mImageLoader = ImageLoader.getInstance();

        mRoundedDisplayImageOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .resetViewBeforeLoading(true)
                .displayer(new FadeInRoundedBitmapDisplayer(5))
                .build();

        // 例外発生時の処理を指定（スタックトレースを保存）
        if (BuildConfig.DEBUG) {
            Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtExceptionHandler(sApplication));
        }
    }

    public void displayImage(String url, ImageView view) {
        String tag = (String) view.getTag();
        if (tag != null && tag.equals(url)) {
            return;
        }
        view.setTag(url);
        mImageLoader.displayImage(url, view);
    }

    public void displayRoundedImage(String url, ImageView view) {
        String tag = (String) view.getTag();
        if (tag != null && tag.equals(url)) {
            return;
        }
        view.setTag(url);
        mImageLoader.displayImage(url, view, mRoundedDisplayImageOptions);
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

    /**
     * タブ管理
     */
    private static final String TABS = "tabs";
    private ArrayList<Integer> mTabs = new ArrayList<Integer>();

    public ArrayList<Integer> loadTabs() {
        SharedPreferences preferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        String tabs_string = preferences.getString(TABS, "-1,-2,-3");
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
        editor.putString(TABS, TextUtils.join(",", tabs_strings));
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
     * Twitterインスタンス管理
     */
    private static final String TOKEN = "token";
    private static final String TOKEN_SECRET = "token_secret";
    private static final String PREF_NAME = "twitter_access_token";
    private AccessToken mAccessToken;
    private Twitter mTwitter;
    private String mScreenName;
    private long mUserId = -1L;

    public long getUserId() {
        if (mUserId != -1L) {
            return mUserId;
        }
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        mUserId = preferences.getLong("user_id", -1L);
        return mUserId;
    }

    public void setUserId(long userId) {
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putLong("user_id", userId);
        editor.commit();
        this.mUserId = userId;
    }

    public String getScreenName() {
        if (mScreenName != null) {
            return mScreenName;
        }
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        mScreenName = preferences.getString("screen_name", null);
        return mScreenName;
    }

    public void setScreenName(String screenName) {
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putString("screen_name", screenName);
        editor.commit();
        this.mScreenName = screenName;
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
        String token = preferences.getString(TOKEN, null);
        String tokenSecret = preferences.getString(TOKEN_SECRET, null);
        if (token != null && tokenSecret != null) {
            this.mAccessToken = new AccessToken(token, tokenSecret);
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
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putString(TOKEN, accessToken.getToken());
        editor.putString(TOKEN_SECRET, accessToken.getTokenSecret());
        editor.commit();
        this.mAccessToken = accessToken;
    }

    /**
     * Twitterインスタンスを取得
     *
     * @return Twitterインスタンス
     */
    public Twitter getTwitter() {
        if (mTwitter != null) {
            return mTwitter;
        }
        TwitterFactory factory = new TwitterFactory();
        Twitter twitter = factory.getInstance();
        twitter.setOAuthConsumer(getConsumerKey(), getConsumerSecret());
        AccessToken token = getAccessToken();
        if (token != null) {
            twitter.setOAuthAccessToken(token);
            // アクセストークンまである時だけキャッシュしておく
            this.mTwitter = twitter;
        }
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

    /**
     * アクセストークンを削除（認証失敗時などトークンが無効な場合に使用）
     */
    public void resetAccessToken() {
        mTwitter.setOAuthAccessToken(null);
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.remove(TOKEN);
        editor.remove(TOKEN_SECRET);
        editor.remove("user_id");
        editor.remove("screen_name");
        editor.commit();
        this.mAccessToken = null;
    }

    private HashMap<Long, Boolean> mIsFavMap = new HashMap<Long, Boolean>();
    private HashMap<Long, Long> mRtIdMap = new HashMap<Long, Long>();

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
        return null;
    }

    public void doFavorite(Long id) {
        mIsFavMap.put(id, true);
        new FavoriteTask().execute(id);
    }

    public void doDestroyFavorite(Long id) {
        mIsFavMap.remove(id);
        new UnFavoriteTask().execute(id);
    }

    public void doRetweet(Long id) {
        sApplication.setRtId(id, (long) 0);
        new RetweetTask().execute(id);
    }

    public void doDestroyRetweet(Long sourceId) {
        Long retweetId = mRtIdMap.get(sourceId);
        if (retweetId == null) {
            return;
        }
        mRtIdMap.remove(sourceId);
        if (retweetId > 0) {
            new UnRetweetTask().execute(retweetId);
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
}
