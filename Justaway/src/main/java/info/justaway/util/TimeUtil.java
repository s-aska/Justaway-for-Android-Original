package info.justaway.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeUtil {
    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("yyyy/MM'/'dd' 'HH':'mm':'ss", Locale.ENGLISH);

    /**
     * 相対時刻取得
     *
     * @param date 日付
     * @return 相対時刻
     */
    public static String getRelativeTime(Date date) {
        int diff = (int) (((new Date()).getTime() - date.getTime()) / 1000);
        if (diff < 1) {
            return "now";
        } else if (diff < 60) {
            return diff + "s";
        } else if (diff < 3600) {
            return (diff / 60) + "m";
        } else if (diff < 86400) {
            return (diff / 3600) + "h";
        } else {
            return (diff / 86400) + "d";
        }
    }

    /**
     * 絶対時刻取得
     *
     * @param date 日付
     * @return 絶対時刻
     */
    public static String getAbsoluteTime(Date date) {
        return DATE_FORMAT.format(date);
    }
}
