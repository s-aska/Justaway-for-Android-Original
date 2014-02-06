package info.justaway.plugin;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.text.Html;

import java.util.List;

import twitter4j.GeoLocation;
import twitter4j.Status;
import twitter4j.User;

/**
 * Twiccaプラグイン用クラス
 * PICK_TREND,UPLOAD,EDIT_TWEETに関しては、onActivityResultにて受信まで行う必要があるので注意が必要
 *
 * @author oboenikui
 */
public class TwiccaPlugin {

    public static final String TWICCA_ACTION_SHOW_TWEET      = "jp.r246.twicca.ACTION_SHOW_TWEET";
    public static final String TWICCA_ACTION_SHOW_USER       = "jp.r246.twicca.ACTION_SHOW_USER";
    public static final String TWICCA_ACTION_PICK_TREND      = "jp.r246.twicca.ACTION_PICK_TREND";
    public static final String TWICCA_ACTION_UPLOAD          = "jp.r246.twicca.ACTION_UPLOAD";
    public static final String TWICCA_ACTION_EDIT_TWEET      = "jp.r246.twicca.ACTION_EDIT_TWEET";
    public static final String TWICCA_ACTION_PLUGIN_SETTINGS = "jp.r246.twicca.ACTION_PLUGIN_SETTINGS";
    public static final String TWICCA_CATEGORY_OWNER         = "jp.r246.twicca.category.OWNER";
    public static final String TWICCA_CATEGORY_USER          = "jp.r246.twicca.category.USER";
    public static final String TWICCA_USER_SCREEN_NAME       = "jp.r246.twicca.USER_SCREEN_NAME";

    /**
     * ツイートからtwiccaプラグインに飛ばすためのIntentを作成
     *
     * @param status ステータス
     * @param pkgName パッケージ名
     * @param className クラス名
     * @return アプリを起動するためのIntent
     */
    public static Intent createIntentShowTweet(Status status, String pkgName, String className) {
        Intent intent = new Intent(TWICCA_ACTION_SHOW_TWEET)
        .putExtra(Intent.EXTRA_TEXT, status.getText())
        .putExtra("id", String.valueOf(status.getId()))
        .putExtra("created_at", String.valueOf(status.getCreatedAt().getTime()))
        .putExtra("source", Html.fromHtml(status.getSource()).toString())
        .putExtra("in_reply_to_status_id", String.valueOf(status.getInReplyToStatusId()))
        .putExtra("user_screen_name", status.getUser().getScreenName())
        .putExtra("user_name", status.getUser().getName())
        .putExtra("user_id", String.valueOf(status.getUser().getId()))
        .putExtra("user_profile_image_url", status.getUser().getOriginalProfileImageURL())
        .putExtra("user_profile_image_url_mini", status.getUser().getMiniProfileImageURL())
        .putExtra("user_profile_image_url_normal", status.getUser().getProfileImageURL())
        .putExtra("user_profile_image_url_bigger", status.getUser().getBiggerProfileImageURL())
        .addCategory(Intent.CATEGORY_DEFAULT)
        .setClassName(pkgName, className);
        
        GeoLocation geoLocation = status.getGeoLocation();
        if (geoLocation != null) {
            intent.putExtra("latitude", String.valueOf(geoLocation.getLatitude()))
            .putExtra("longitude", String.valueOf(geoLocation.getLongitude()));
        }
        return intent;
    }

    /**
     * ユーザー画面からtwiccaプラグインに飛ばすIntentを作成
     *
     * @param user ユーザー
     * @param owner Justaway使用者
     * @param pkgName パッケージ名
     * @param className クラス名
     * @return アプリを起動するためのIntent
     */
    public static Intent createIntentShowUser(User user, User owner, String pkgName, String className) {
        return new Intent(TWICCA_ACTION_SHOW_USER)
        .putExtra(Intent.EXTRA_TEXT, user.getScreenName())
        .putExtra("name", user.getName())
        .putExtra("id", String.valueOf(user.getId()))
        .putExtra("location", user.getLocation())
        .putExtra("url", user.getURL())
        .putExtra("description", user.getDescription())
        .putExtra("profile_image_url", user.getOriginalProfileImageURL())
        .putExtra("profile_image_url_mini", user.getMiniProfileImageURL())
        .putExtra("profile_image_url_normal", user.getProfileImageURL())
        .putExtra("profile_image_url_bigger", user.getBiggerProfileImageURL())
        .putExtra("owner_screen_name", owner.getScreenName())
        .putExtra("owner_name", owner.getName())
        .putExtra("owner_id", String.valueOf(owner.getId()))
        .putExtra("owner_location", owner.getLocation())
        .putExtra("owner_url", owner.getURL())
        .putExtra("owner_description", owner.getDescription())
        .putExtra("owner_profile_image_url", owner.getOriginalProfileImageURL())
        .putExtra("owner_profile_image_url_mini", owner.getMiniProfileImageURL())
        .putExtra("owner_profile_image_url_normal", owner.getProfileImageURL())
        .putExtra("owner_profile_image_url_bigger", owner.getBiggerProfileImageURL())
        .addCategory(Intent.CATEGORY_DEFAULT)
        .addCategory(user.getId()==owner.getId()?TWICCA_CATEGORY_OWNER:TWICCA_CATEGORY_USER)
        .setClassName(pkgName, className);
    }

    /**
     * ツイート編集中に呼び出すプラグイン 引数の説明はtwicca公式サイト(http://twicca.r246.jp/developers/edit_tweet_action/)より
     *
     * @param prefix 編集中のテキストの接頭辞(例："@screen_name ")
     * @param user_input 編集中のテキストのうち、接頭辞と接尾辞を取り除いた部分。
     * @param suffix 編集中のテキストの接尾辞(例：" RT @screen_name: quoted tweet")
     * @param cursor カーソル位置
     * @param pkgName パッケージ名
     * @param className クラス名
     * @return アプリを起動するためのIntent
     */
    public static Intent createIntentEditTweet(String prefix, String user_input, String suffix, int cursor, String pkgName, String className) {
        String all_text = (prefix==null?"":prefix) + (user_input==null?"":user_input) + (suffix==null?"":suffix);
        return new Intent(TWICCA_ACTION_EDIT_TWEET)
        .putExtra(Intent.EXTRA_TEXT, all_text)
        .putExtra("prefix", prefix)
        .putExtra("suffix", suffix)
        .putExtra("user_input", user_input)
        .putExtra("cursor", cursor)
        .addCategory(Intent.CATEGORY_DEFAULT)
        .setClassName(pkgName, className);
    }
    
    /**
     * ツイート編集中に呼び出すプラグイン 引数の説明はtwicca公式サイト(http://twicca.r246.jp/developers/edit_tweet_action/)より
     *
     * @param prefix 編集中のテキストの接頭辞(例："@screen_name ")
     * @param user_input 編集中のテキストのうち、接頭辞と接尾辞を取り除いた部分。
     * @param suffix 編集中のテキストの接尾辞(例：" RT @screen_name: quoted tweet")
     * @param cursor カーソル位置
     * @param in_reply_to 返信先のID
     * @param pkgName パッケージ名
     * @param className クラス名
     * @return アプリを起動するためのIntent
     */
    public static Intent createIntentEditTweet(String prefix, String user_input, String suffix, int cursor, long in_reply_to, String pkgName, String className) {
        return createIntentEditTweet(prefix, user_input, suffix, cursor, pkgName, className)
        .putExtra("in_reply_to_status_id", in_reply_to);
    }

    /**
     * 画像アップロード時に呼ばれるプラグイン 要らない気がする
     *
     * @param uri 画像/動画のContent Uri
     * @param tweet 画像アップロード時点でのツイート
     * @param screen_name ユーザーのスクリーンネーム
     * @param pkgName パッケージ名
     * @param className クラス名
     * @return アプリを起動するためのIntent
     */
    public static Intent createIntentUpload(Uri uri,String tweet , String screen_name, String pkgName, String className) {
        return new Intent(TWICCA_ACTION_UPLOAD)
        .putExtra(Intent.EXTRA_TEXT, tweet)
        .putExtra(TWICCA_USER_SCREEN_NAME, screen_name)
        .setData(uri)
        .addCategory(Intent.CATEGORY_DEFAULT)
        .setClassName(pkgName, className);
    }
    
    /**
     * 画像アップロード時に呼ばれるプラグイン 要らない気がする
     *
     * @param uri 画像/動画のContent Uri
     * @param tweet 画像アップロード時点でのツイート
     * @param screen_name ユーザーのスクリーンネーム
     * @param in_reply_to 返信先ID
     * @param pkgName パッケージ名
     * @param className クラス名
     * @return アプリを起動するためのIntent
     */
    public static Intent createIntentUpload(Uri uri,String tweet , String screen_name, long in_reply_to, String pkgName, String className) {
        return createIntentUpload(uri, tweet, screen_name, pkgName, className)
                .putExtra("in_reply_to_status_id", String.valueOf(in_reply_to));
    }
    
    /**
     * カスタム「話題のトピック」プラグイン用 私はそのようなプラグインを見たことがありませんし今のところ「話題のトピック」機能もありませんが一応
     *
     * @param pkgName パッケージ名
     * @param className クラス名
     * @return アプリを起動するためのIntent
     */
    public static Intent createIntentPickTrend(String pkgName, String className) {
        return new Intent(TWICCA_ACTION_PICK_TREND)
        .addCategory(Intent.CATEGORY_DEFAULT)
        .setClassName(pkgName, className);
    }
    
    /**
     * それぞれのプラグインの設定画面に飛ばす
     * 
     * @param pkgName パッケージ名
     * @param className クラス名
     * @return 設定画面を起動するためのIntent
     */
    public static Intent createIntentPluginSettings(String pkgName, String className) {
        return new Intent(TWICCA_ACTION_PLUGIN_SETTINGS)
        .addCategory(Intent.CATEGORY_DEFAULT);
    }
    
    /**
     * 飛ばせるtwiccaプラグインのリスト
     *
     * @param pm パッケージマネージャ
     * @param action アクション名
     * @return ResolveInfoのList
     */
    public static List<ResolveInfo> getResolveInfo(PackageManager pm, String action) {
        return pm.queryIntentActivities(new Intent(action), PackageManager.MATCH_DEFAULT_ONLY);
    }
}