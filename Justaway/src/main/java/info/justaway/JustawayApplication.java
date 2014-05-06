package info.justaway;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.StrictMode;
import android.widget.ImageView;

import info.justaway.model.AccessTokenManager;
import info.justaway.model.FavRetweetManager;
import info.justaway.model.TabManager;
import info.justaway.model.TwitterManager;
import info.justaway.model.UserIconManager;
import info.justaway.settings.BasicSettings;
import info.justaway.settings.MuteSettings;
import info.justaway.util.ImageUtil;
import info.justaway.util.MessageUtil;
import info.justaway.util.ThemeUtil;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.UserList;

public class JustawayApplication extends Application {

    private static JustawayApplication sApplication;
    private static Typeface sFontello;
    private static ResponseList<UserList> sUserLists;
    private static AccessTokenManager sAccessTokenManager;
    private static FavRetweetManager sFavRetweetManager;
    private static UserIconManager sUserIconManager;
    private static TwitterManager sTwitterManager;
    private static MuteSettings sMuteSettings;
    private static final BasicSettings sBasicSettings = new BasicSettings();
    private static final TabManager sTabManager = new TabManager();

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;

        // Twitter4J の user stream の shutdown() で NetworkOnMainThreadException が発生してしまうことに対する暫定対応
        if (!BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        }

        /**
         * 画像のキャッシュや角丸の設定を行う
         */
        ImageUtil.init();

        sAccessTokenManager = new AccessTokenManager();
        sFavRetweetManager = new FavRetweetManager();
        sUserIconManager = new UserIconManager();
        sTwitterManager = new TwitterManager();
        sMuteSettings = new MuteSettings();

        sFontello = Typeface.createFromAsset(getAssets(), "fontello.ttf");

        // 例外発生時の処理を指定（スタックトレースを保存）
        if (BuildConfig.DEBUG) {
            Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtExceptionHandler(sApplication));
        }

        sBasicSettings.resetDisplaySettings();

        sUserIconManager.warmUpUserIconMap();
    }

    /**
     * 終了時
     * 
     * @see android.app.Application#onTerminate()
     */
    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    /**
     * 空きメモリ逼迫時
     * 
     * @see android.app.Application#onLowMemory()
     */
    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    /**
     * 実行時において変更できるデバイスの設定時 ( 画面のオリエンテーション、キーボードの使用状態、および言語など )
     * https://sites.google.com/a/techdoctranslator.com/jp/android/guide/resources/runtime-changes
     *
     * @see android.app.Application#onConfigurationChanged(android.content.res.Configuration)
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public static JustawayApplication getApplication() {
        return sApplication;
    }

    public static Typeface getFontello() {
        return sFontello;
    }

    public FavRetweetManager getFavRetweetManager() {
        return sFavRetweetManager;
    }

    public TwitterManager getTwitterManager() {
        return sTwitterManager;
    }

    public MuteSettings getMuteSettings() {
        return sMuteSettings;
    }

    public AccessTokenManager getAccessTokenManager() {
        return sAccessTokenManager;
    }

    public TabManager getTabManager() {
        return sTabManager;
    }

    public UserIconManager getUserIconManager() {
        return sUserIconManager;
    }

    public BasicSettings getBasicSettings() {
        return sBasicSettings;
    }

    /**
     * ソース互換性維持のためのエイリアス、ここから下のソースはいずれすべて消す
     */
    public void displayImage(String url, ImageView view) {
        ImageUtil.displayImage(url, view);
    }

    public void displayRoundedImage(String url, ImageView view) {
        ImageUtil.displayRoundedImage(url, view);
    }

    public static void showToast(String text) {
        MessageUtil.showToast(text);
    }

    public static void showToast(int id) {
        MessageUtil.showToast(id);
    }

    public static void showProgressDialog(Context context, String message) {
        MessageUtil.showProgressDialog(context,  message);
    }

    public static void dismissProgressDialog() {
        MessageUtil.dismissProgressDialog();
    }

    public void setTheme(Activity activity) {
        ThemeUtil.setTheme(activity);
    }

    public long getUserId() {
        return sAccessTokenManager.getUserId();
    }

    public String getScreenName() {
        return sAccessTokenManager.getScreenName();
    }

    public Twitter getTwitter() {
        return sTwitterManager.getTwitter();
    }

    /**
     * ユーザーリストを必要とする画面がいくつかあり、都度APIを引くと重いと考え用意したメソッド達
     */
    public ResponseList<UserList> getUserLists() {
        return sUserLists;
    }

    public void setUserLists(ResponseList<UserList> userLists) {
        sUserLists = userLists;
    }

    public UserList getUserList(long id) {
        if (sUserLists == null) {
            return null;
        }
        for (UserList userList : sUserLists) {
            if (userList.getId() == id) {
                return userList;
            }
        }
        return null;
    }
}
