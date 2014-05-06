package info.justaway.settings;

import android.content.Context;
import android.content.SharedPreferences;

import info.justaway.JustawayApplication;
import info.justaway.NotificationService;

public class BasicSettings {

    private static final String PREF_NAME_SETTINGS = "settings";
    private int mFontSize;
    private String mLongTapAction;
    private String mThemeName;
    private Boolean mUserIconRounded;
    private Boolean mDisplayThumbnail;
    private String mUserIconSize;
    private int mPageCount;

    private static final String STREAMING_MODE = "streamingMode";
    private Boolean mStreamingMode;

    private static final String QUICK_MODE = "quickMode";

    public SharedPreferences getSharedPreferences() {
        return JustawayApplication.getApplication()
                .getSharedPreferences(PREF_NAME_SETTINGS, Context.MODE_PRIVATE);
    }

    public void setQuickMod(Boolean quickMode) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putBoolean(QUICK_MODE, quickMode);
        editor.commit();
    }

    public Boolean getQuickMode() {
        return getSharedPreferences().getBoolean(QUICK_MODE, false);
    }

    public Boolean getNotificationOn() {
        return getSharedPreferences().getBoolean("notification_on", true);
    }

    public void setStreamingMode(Boolean streamingMode) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putBoolean(STREAMING_MODE, streamingMode);
        editor.commit();
        mStreamingMode = streamingMode;
    }

    public Boolean getStreamingMode() {
        if (mStreamingMode != null) {
            return mStreamingMode;
        }
        return getSharedPreferences().getBoolean(STREAMING_MODE, true);
    }

    public boolean getKeepScreenOn() {
        return getSharedPreferences().getBoolean("keep_screen_on", true);
    }

    public void resetDisplaySettings() {
        SharedPreferences preferences = getSharedPreferences();
        mFontSize = Integer.parseInt(preferences.getString("font_size", "12"));
        mLongTapAction = preferences.getString("long_tap", "nothing");
        mThemeName = preferences.getString("themeName", "black");
        mUserIconRounded = preferences.getBoolean("user_icon_rounded_on", true);
        mUserIconSize = preferences.getString("user_icon_size", "bigger");
        mDisplayThumbnail = preferences.getBoolean("display_thumbnail_on", true);
        mPageCount = Integer.parseInt(preferences.getString("page_count", "200"));
    }

    public void resetNotification() {
        if (getNotificationOn()) {
            NotificationService.start();
        } else {
            NotificationService.stop();
        }
    }

    public int getFontSize() {
        return mFontSize;
    }

    public String getThemeName() {
        return mThemeName;
    }

    public String getLongTapAction() {
        return mLongTapAction;
    }

    public boolean getUserIconRoundedOn() {
        if (mUserIconRounded != null) {
            return mUserIconRounded;
        }
        mUserIconRounded = getSharedPreferences().getBoolean("user_icon_rounded_on", true);
        return mUserIconRounded;
    }

    public String getUserIconSize() {
        if (mUserIconSize != null) {
            return mUserIconSize;
        }
        mUserIconSize = getSharedPreferences().getString("user_icon_size", "bigger");
        return mUserIconSize;
    }

    public boolean getDisplayThumbnailOn() {
        if (mDisplayThumbnail != null) {
            return mDisplayThumbnail;
        }
        mDisplayThumbnail = getSharedPreferences().getBoolean("display_thumbnail_on", true);
        return mDisplayThumbnail;
    }

    public int getPageCount() {
        if (mPageCount > 0) {
            return mPageCount;
        }
        mPageCount = Integer.parseInt(getSharedPreferences().getString("page_count", "100"));
        return mPageCount;
    }
}
