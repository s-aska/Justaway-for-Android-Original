package info.justaway.settings;

import android.content.Context;
import android.content.SharedPreferences;

import info.justaway.JustawayApplication;
import info.justaway.NotificationService;

public class BasicSettings {

    private static final String PREF_NAME_SETTINGS = "settings";
    private static int mFontSize;
    private static String mLongTapAction;
    private static String mThemeName;
    private static boolean mUserIconRounded;
    private static boolean mDisplayThumbnail;
    private static String mUserIconSize;
    private static int mPageCount;

    private static final String STREAMING_MODE = "streamingMode";
    private static boolean mStreamingMode;

    private static final String QUICK_MODE = "quickMode";

    public static SharedPreferences getSharedPreferences() {
        return JustawayApplication.getApplication()
                .getSharedPreferences(PREF_NAME_SETTINGS, Context.MODE_PRIVATE);
    }

    public static void setQuickMod(boolean quickMode) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putBoolean(QUICK_MODE, quickMode);
        editor.apply();
    }

    public static boolean getQuickMode() {
        return getSharedPreferences().getBoolean(QUICK_MODE, false);
    }

    public static boolean getNotificationOn() {
        return getSharedPreferences().getBoolean("notification_on", true);
    }

    public static void setStreamingMode(boolean streamingMode) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putBoolean(STREAMING_MODE, streamingMode);
        editor.apply();
        mStreamingMode = streamingMode;
    }

    public static boolean getStreamingMode() {
        return mStreamingMode;
    }

    public static boolean getKeepScreenOn() {
        return getSharedPreferences().getBoolean("keep_screen_on", true);
    }

    public static void init() {
        SharedPreferences preferences = getSharedPreferences();
        mFontSize = Integer.parseInt(preferences.getString("font_size", "12"));
        mLongTapAction = preferences.getString("long_tap", "nothing");
        mThemeName = preferences.getString("themeName", "black");
        mUserIconRounded = preferences.getBoolean("user_icon_rounded_on", true);
        mUserIconSize = preferences.getString("user_icon_size", "bigger");
        mDisplayThumbnail = preferences.getBoolean("display_thumbnail_on", true);
        mPageCount = Integer.parseInt(preferences.getString("page_count", "200"));
        mStreamingMode = getSharedPreferences().getBoolean(STREAMING_MODE, true);
    }

    public static void resetNotification() {
        if (getNotificationOn()) {
            NotificationService.start();
        } else {
            NotificationService.stop();
        }
    }

    public static int getFontSize() {
        return mFontSize;
    }

    public static String getThemeName() {
        return mThemeName;
    }

    public static String getLongTapAction() {
        return mLongTapAction;
    }

    public static boolean getUserIconRoundedOn() {
        return mUserIconRounded;
    }

    public static String getUserIconSize() {
        return mUserIconSize;
    }

    public static boolean getDisplayThumbnailOn() {
        return mDisplayThumbnail;
    }

    public static int getPageCount() {
        return mPageCount;
    }
}
