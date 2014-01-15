package info.justaway.plugin;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.Html;

import java.util.List;

import twitter4j.GeoLocation;
import twitter4j.Status;

/**
 * Twiccaプラグイン用クラス
 * ツイート画面用とかも作ればいいと思う
 *
 * @author oboenikui
 */
public class TwiccaPlugin {

    private static final String TWICCA_ACTION_SHOW_TWEET = "jp.r246.twicca.ACTION_SHOW_TWEET";

    /**
     * Statusからtwiccaプラグインに飛ばす用Intent作成
     *
     * @param status    ステータス
     * @param pkgName   パッケージ名
     * @param className クラス名
     * @return そのまま飛ばせるIntent
     */
    public static Intent createIntentShowTweet(Status status, String pkgName, String className) {
        Intent intent = new Intent(TWICCA_ACTION_SHOW_TWEET);
        intent.putExtra(Intent.EXTRA_TEXT, status.getText());
        intent.putExtra("id", String.valueOf(status.getId()));
        GeoLocation geoLocation = status.getGeoLocation();
        if (geoLocation != null) {
            intent.putExtra("latitude", String.valueOf(geoLocation.getLatitude()));
            intent.putExtra("longitude", String.valueOf(geoLocation.getLongitude()));
        }
        intent.putExtra("created_at", String.valueOf(status.getCreatedAt().getTime()));
        intent.putExtra("source", Html.fromHtml(status.getSource()).toString());
        intent.putExtra("in_reply_to_status_id", String.valueOf(status.getInReplyToStatusId()));
        intent.putExtra("user_screen_name", status.getUser().getScreenName());
        intent.putExtra("user_name", status.getUser().getName());
        intent.putExtra("user_id", String.valueOf(status.getUser().getId()));
        intent.putExtra("user_profile_image_url", status.getUser().getOriginalProfileImageURL());
        intent.putExtra("user_profile_image_url_mini", status.getUser().getMiniProfileImageURL());
        intent.putExtra("user_profile_image_url_normal", status.getUser().getProfileImageURL());
        intent.putExtra("user_profile_image_url_bigger", status.getUser().getBiggerProfileImageURL());
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setClassName(pkgName, className);
        return intent;
    }

    /**
     * Statusから飛ばせるtwiccaプラグインのリスト
     *
     * @param pm パッケージマネージャ
     * @return ResolveInfoのList
     */
    public static List<ResolveInfo> getResolveInfoForShowTweet(PackageManager pm) {
        return pm.queryIntentActivities(new Intent(TWICCA_ACTION_SHOW_TWEET), PackageManager.MATCH_DEFAULT_ONLY);
    }
}