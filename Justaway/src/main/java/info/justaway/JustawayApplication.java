package info.justaway;

import android.app.Application;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.StrictMode;

import info.justaway.model.UserIconManager;
import info.justaway.settings.BasicSettings;
import info.justaway.settings.MuteSettings;
import info.justaway.util.ImageUtil;
import twitter4j.ResponseList;
import twitter4j.UserList;

public class JustawayApplication extends Application {

    private static JustawayApplication sApplication;
    private static Typeface sFontello;
    private static ResponseList<UserList> sUserLists;

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

        /**
         * 設定ファイル読み込み
         */
        MuteSettings.init();

        BasicSettings.init();

        UserIconManager.warmUpUserIconMap();

        sFontello = Typeface.createFromAsset(getAssets(), "fontello.ttf");

        // 例外発生時の処理を指定（スタックトレースを保存）
        if (BuildConfig.DEBUG) {
            Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtExceptionHandler(sApplication));
        }
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
