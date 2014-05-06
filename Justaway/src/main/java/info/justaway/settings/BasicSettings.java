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
    private static Boolean mUserIconRounded;
    private static Boolean mDisplayThumbnail;
    private static String mUserIconSize;
    private static int mPageCount;

    private static final String STREAMING_MODE = "streamingMode";
    private static Boolean mStreamingMode;

    private static final String QUICK_MODE = "quickMode";

    public static SharedPreferences getSharedPreferences() {
        return JustawayApplication.getApplication()
                .getSharedPreferences(PREF_NAME_SETTINGS, Context.MODE_PRIVATE);
    }

    public static void setQuickMod(Boolean quickMode) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putBoolean(QUICK_MODE, quickMode);
        editor.commit();
    }

    public static Boolean getQuickMode() {
        return getSharedPreferences().getBoolean(QUICK_MODE, false);
    }

    public static Boolean getNotificationOn() {
        return getSharedPreferences().getBoolean("notification_on", true);
    }

    public static void setStreamingMode(Boolean streamingMode) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putBoolean(STREAMING_MODE, streamingMode);
        editor.commit();
        mStreamingMode = streamingMode;
    }

    public static Boolean getStreamingMode() {
        if (mStreamingMode != null) {
            return mStreamingMode;
        }
        return getSharedPreferences().getBoolean(STREAMING_MODE, true);
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
        if (mUserIconRounded != null) {
            return mUserIconRounded;
        }
        mUserIconRounded = getSharedPreferences().getBoolean("user_icon_rounded_on", true);
        return mUserIconRounded;
    }

    public static String getUserIconSize() {
        if (mUserIconSize != null) {
            return mUserIconSize;
        }
        mUserIconSize = getSharedPreferences().getString("user_icon_size", "bigger");
        return mUserIconSize;
    }

    public static boolean getDisplayThumbnailOn() {
        if (mDisplayThumbnail != null) {
            return mDisplayThumbnail;
        }
        mDisplayThumbnail = getSharedPreferences().getBoolean("display_thumbnail_on", true);
        return mDisplayThumbnail;
    }

    public static int getPageCount() {
        if (mPageCount > 0) {
            return mPageCount;
        }
        mPageCount = Integer.parseInt(getSharedPreferences().getString("page_count", "100"));
        return mPageCount;
    }
}
