package info.justaway.util;

import android.app.Activity;
import android.content.res.Resources;
import android.util.TypedValue;
import android.widget.TextView;

import info.justaway.R;
import info.justaway.settings.BasicSettings;

public class ThemeUtil {
    public static void setTheme(Activity activity) {
        if (BasicSettings.getThemeName().equals("black")) {
            activity.setTheme(R.style.BlackTheme);
        } else {
            activity.setTheme(R.style.WhiteTheme);
        }
    }

    public static void setThemeTextColor(Activity activity, TextView view, int resourceId) {
        TypedValue outValue = new TypedValue();
        Resources.Theme theme = activity.getTheme();
        if (theme != null) {
            theme.resolveAttribute(resourceId, outValue, true);
            view.setTextColor(outValue.data);
        }
    }

    public static int getThemeTextColor(Activity activity, int resourceId) {
        TypedValue outValue = new TypedValue();
        Resources.Theme theme = activity.getTheme();
        if (theme != null) {
            theme.resolveAttribute(resourceId, outValue, true);
        }
        return outValue.data;
    }
}
