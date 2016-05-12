package info.justaway.util;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Paint;
import android.os.BatteryManager;
import android.os.Build;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TwitterUtil {

    private static final Pattern URL_PATTERN = Pattern.compile("(http://|https://)[\\w\\.\\-/:#\\?=&;%~\\+]+");
    private static final int BATTERY_ROW_LEVEL = 14;

    /**
     * ツイートの文字数を数えます
     *
     * @param str ツイート文字列
     * @return 文字数
     */
    public static int count(String str) {
        int length = str.codePointCount(0, str.length());
        int max = str.indexOf("D ") == 0 ? 10000 : 140;

        // 短縮URLを考慮
        Matcher matcher = URL_PATTERN.matcher(str);
        while (matcher.find()) {
            length = length - matcher.group().length() + 23;
        }

        return max - length;
    }

    /**
     * 突然の死ジェネレーター
     *
     * @param text 対象のテキスト
     * @param selectStart 選択開始位置
     * @param selectEnd 選択終了位置
     * @return 突然の死
     */
    public static String convertSuddenly(String text, int selectStart, int selectEnd) {
        // 突然の死対象のテキストを取得
        String targetText;
        if (selectStart != selectEnd) {
            targetText = text.substring(selectStart, selectEnd) + "\n";
        } else {
            targetText = text + "\n";
        }

        Paint paint = new Paint();
        float maxTextWidth = 0;
        // 対象のテキストの最大文字列幅を取得
        String[] lines = targetText.split("\n");
        for (String line : lines) {
            if (paint.measureText(line) > maxTextWidth) {
                maxTextWidth = paint.measureText(line);
            }
        }

        // 上と下を作る
        String top = "";
        String under = "";
        int i;
        for (i = 0; (maxTextWidth / 12) > i; i++) {
            top += "人";
        }
        for (i = 0; (maxTextWidth / 13) > i; i++) {
            under += "^Y";
        }

        String suddenly = "";
        for (String line : lines) {
            float spaceWidth = maxTextWidth - paint.measureText(line);
            // maxとくらべて13以上差がある場合はスペースを挿入して調整する
            if (spaceWidth >= 12) {
                int spaceNumber = (int) spaceWidth / 12;
                for (i = 0; i < spaceNumber; i++) {
                    line += "　";
                }
                if ((spaceWidth % 12) >= 6) {
                    line += "　";
                }
            }
            suddenly = suddenly.concat("＞ " + line + " ＜\n");
        }

        if (selectStart != selectEnd) {
            return text.substring(0, selectStart) + "＿" + top + "＿\n" + suddenly + "￣" + under + "￣" + text.substring(selectEnd);
        } else {
            return "＿" + top + "＿\n" + suddenly + "￣" + under + "￣";
        }
    }

    /**
     * バッテリー情報の文字列を返す
     *
     * @param context コンテキスト
     * @return バッテリー情報
     */
    public static String getBatteryStatus(Context context) {
        Intent batteryIntent = context.registerReceiver(null,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        if (batteryIntent == null) {
            return null;
        }
        int level = batteryIntent.getIntExtra("level", 0);
        int status = batteryIntent.getIntExtra("status", 0);

        String batteryText = Build.MODEL + " のバッテリー残量：" + level;
        switch (status) {
            case BatteryManager.BATTERY_STATUS_FULL:
                batteryText += "% (0゜・◡・♥​​)";
                break;
            case BatteryManager.BATTERY_STATUS_CHARGING:
                batteryText +=  "% 充電なう(・◡・♥​​)";
                break;
            default:
                if (level <= BATTERY_ROW_LEVEL) {
                    batteryText +=  "% (◞‸◟)";
                } else {
                    batteryText += "% (・◡・♥​​)";
                }
                break;
        }
        return batteryText;
    }
}
