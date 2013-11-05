package info.justaway;

import java.util.ArrayList;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * アプリケーション、アクティビティ間でのデータの共有などに利用
 * 
 * @author aska
 */
public class JustawayApplication extends Application {

    private static JustawayApplication sApplication;
    private ArrayList<Integer> lists = new ArrayList<Integer>();
    private ImageLoader mImageLoader;
    private DisplayImageOptions mRoundedDisplayImageOptions;

    /**
     * 毎回キャストしなくて良いように
     * 
     * @return アプリケーションのインスタンス
     */
    public static JustawayApplication getApplication() {
        return sApplication;
    }

    public ArrayList<Integer> getLists() {
        return lists;
    }

    public void setLists(ArrayList<Integer> lists) {
        this.lists = lists;
    }

    /*
     * 起動時
     * 
     * @see android.app.Application#onCreate()
     */
    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;

        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisc(true).build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .defaultDisplayImageOptions(defaultOptions).build();
        ImageLoader.getInstance().init(config);
        mImageLoader = ImageLoader.getInstance();
        mRoundedDisplayImageOptions = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisc(true).resetViewBeforeLoading(true)
                .displayer(new RoundedBitmapDisplayer(5)).build();

        // 例外発生時の処理を指定（スタックトレースを保存）
        Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtExceptionHandler(sApplication));
    }

    public void displayImage(String url, ImageView view) {
        mImageLoader.displayImage(url, view);
    }

    public void displayRoundedImage(String url, ImageView view) {
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

        // ちゃんと接続を切らないとアプリが凍結されるらしい
        if (twitterStream != null) {
            twitterStream.cleanUp();
            twitterStream.shutdown();
        }
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

    /**
     * タブ管理
     */
    private static final String TABS = "tabs";

    public ArrayList<Integer> loadTabs() {
        SharedPreferences preferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        String tabs_string = preferences.getString(TABS, "-1,-2,-3");
        String[] tabs_strings = tabs_string.split(",");
        ArrayList<Integer> tabs = new ArrayList<Integer>();
        for (String tab_string : tabs_strings) {
            tabs.add(Integer.valueOf(tab_string));
        }
        return tabs;
    }

    public void saveTabs(ArrayList<Integer> tabs) {
        ArrayList<String> tabs_strings = new ArrayList<String>();
        for (Integer tab : tabs) {
            tabs_strings.add(String.valueOf(tab));
        }
        SharedPreferences preferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putString(TABS, TextUtils.join(",", tabs_strings));
        editor.commit();
    }

    /**
     * Twitterインスタンス管理
     */
    private static final String TOKEN = "token";
    private static final String TOKEN_SECRET = "token_secret";
    private static final String PREF_NAME = "twitter_access_token";
    private AccessToken accessToken;
    private Twitter twitter;
    private TwitterStream twitterStream;
    private User user;

    /**
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(User user) {
        this.user = user;
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
        return getAccessToken() != null ? true : false;
    }

    /**
     * Twitterアクセストークン取得
     * 
     * @return Twitterアクセストークン
     */
    private AccessToken getAccessToken() {
        // キャッシュしておく
        if (accessToken != null) {
            return accessToken;
        }
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String token = preferences.getString(TOKEN, null);
        String tokenSecret = preferences.getString(TOKEN_SECRET, null);
        if (token != null && tokenSecret != null) {
            this.accessToken = new AccessToken(token, tokenSecret);
            return accessToken;
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
        this.accessToken = accessToken;
    }

    /**
     * Twitterインスタンスを取得
     * 
     * @return Twitterインスタンス
     */
    public Twitter getTwitter() {
        if (twitter != null) {
            return twitter;
        }
        TwitterFactory factory = new TwitterFactory();
        Twitter twitter = factory.getInstance();
        twitter.setOAuthConsumer(getConsumerKey(), getConsumerSecret());
        AccessToken token = getAccessToken();
        if (token != null) {
            twitter.setOAuthAccessToken(token);
            // アクセストークンまである時だけキャッシュしておく
            this.twitter = twitter;
        }
        return twitter;
    }

    /**
     * TwitterStreamインスタンスを取得
     * 
     * @return TwitterStreamインスタンス
     */
    public TwitterStream getTwitterStream() {
        // キャッシュしておく
        if (twitterStream != null) {
            return twitterStream;
        }
        AccessToken token = getAccessToken();
        if (token == null) {
            return null;
        }
        ConfigurationBuilder confbuilder = new ConfigurationBuilder();
        twitter4j.conf.Configuration conf = confbuilder.setOAuthConsumerKey(getConsumerKey())
                .setOAuthConsumerSecret(getConsumerSecret()).setOAuthAccessToken(token.getToken())
                .setOAuthAccessTokenSecret(token.getTokenSecret()).build();
        TwitterStream twitterStream = new TwitterStreamFactory(conf).getInstance();
        this.twitterStream = twitterStream;
        return twitterStream;
    }

    /**
     * アクセストークンを削除（認証失敗時などトークンが無効な場合に使用）
     */
    public void resetAccessToken() {
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.remove(TOKEN);
        editor.remove(TOKEN_SECRET);
        editor.commit();
        this.accessToken = null;
    }
}
