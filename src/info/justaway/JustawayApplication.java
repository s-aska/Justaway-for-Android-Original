package info.justaway;

import info.justaway.display.FadeInRoundedBitmapDisplayer;
import info.justaway.fragment.TalkFragment;
import info.justaway.model.Row;
import info.justaway.task.DestroyStatusTask;
import info.justaway.task.FavoriteTask;
import info.justaway.task.RetweetTask;
import info.justaway.task.UnFavoriteTask;
import info.justaway.task.UnRetweetTask;

import java.util.ArrayList;
import java.util.HashMap;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import twitter4j.HashtagEntity;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.UserMentionEntity;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

/**
 * アプリケーション、アクティビティ間でのデータの共有などに利用
 *
 * @author aska
 */
public class JustawayApplication extends Application {

    private static JustawayApplication sApplication;
    private ArrayList<Integer> lists = new ArrayList<Integer>();
    private ImageLoader mImageLoader;
    private DisplayImageOptions mRoundedDisplayImageOptions;

    /**
     * 毎回キャストしなくて良いように
     *
     * @return アプリケーションのインスタンス
     */
    public static JustawayApplication getApplication() {
        return sApplication;
    }

    public ArrayList<Integer> getLists() {
        return lists;
    }

    public void setLists(ArrayList<Integer> lists) {
        this.lists = lists;
    }

    /*
     * 起動時
     * 
     * @see android.app.Application#onCreate()
     */
    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;

        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisc(true).resetViewBeforeLoading(true).build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .defaultDisplayImageOptions(defaultOptions).build();
        ImageLoader.getInstance().init(config);
        mImageLoader = ImageLoader.getInstance();
        mRoundedDisplayImageOptions = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisc(true).resetViewBeforeLoading(true)
                .displayer(new FadeInRoundedBitmapDisplayer(5)).build();

        // 例外発生時の処理を指定（スタックトレースを保存）
        Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtExceptionHandler(sApplication));
    }

    public void displayImage(String url, ImageView view) {
        String tag = (String) view.getTag();
        if (tag != null && tag == url) {
            return;
        }
        view.setTag(url);
        mImageLoader.displayImage(url, view);
    }

    public void displayRoundedImage(String url, ImageView view) {
        String tag = (String) view.getTag();
        if (tag != null && tag == url) {
            return;
        }
        view.setTag(url);
        mImageLoader.displayImage(url, view, mRoundedDisplayImageOptions);
    }

    /*
     * 終了時
     * 
     * @see android.app.Application#onTerminate()
     */
    @Override
    public void onTerminate() {
        super.onTerminate();

        // ちゃんと接続を切らないとアプリが凍結されるらしい
        if (twitterStream != null) {
            twitterStream.cleanUp();
            twitterStream.shutdown();
        }
    }

    /*
     * 空きメモリ逼迫時
     * 
     * @see android.app.Application#onLowMemory()
     */
    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    /*
     * 実行時において変更できるデバイスの設定時 ( 画面のオリエンテーション、キーボードの使用状態、および言語など )
     * https://sites.google
     * .com/a/techdoctranslator.com/jp/android/guide/resources/runtime-changes
     * 
     * @see android.app.Application#onConfigurationChanged(android.content.res.
     * Configuration)
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * あると便利な簡易通知
     *
     * @param text 表示するメッセージ
     */
    public static void showToast(String text) {
        Toast.makeText(sApplication, text, Toast.LENGTH_SHORT).show();
    }

    /**
     * タブ管理
     */
    private static final String TABS = "tabs";
    private ArrayList<Integer> mTabs = new ArrayList<Integer>();

    public ArrayList<Integer> loadTabs() {
        SharedPreferences preferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        String tabs_string = preferences.getString(TABS, "-1,-2,-3");
        String[] tabs_strings = tabs_string.split(",");
        mTabs.clear();
        for (String tab_string : tabs_strings) {
            mTabs.add(Integer.valueOf(tab_string));
        }
        return mTabs;
    }

    public void saveTabs(ArrayList<Integer> tabs) {
        mTabs = tabs;
        ArrayList<String> tabs_strings = new ArrayList<String>();
        for (Integer tab : tabs) {
            tabs_strings.add(String.valueOf(tab));
        }
        SharedPreferences preferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putString(TABS, TextUtils.join(",", tabs_strings));
        editor.commit();
    }

    public boolean existsTab(Integer findTab) {
        for (Integer tab : mTabs) {
            if (tab.equals(findTab)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Twitterインスタンス管理
     */
    private static final String TOKEN = "token";
    private static final String TOKEN_SECRET = "token_secret";
    private static final String PREF_NAME = "twitter_access_token";
    private AccessToken accessToken;
    private Twitter twitter;
    private TwitterStream twitterStream;
    private User user;

    /**
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(User user) {
        this.user = user;
    }

    private String getConsumerKey() {
        return getString(R.string.twitter_consumer_key);
    }

    private String getConsumerSecret() {
        return getString(R.string.twitter_consumer_secret);
    }

    /**
     * Twitterアクセストークン有無
     *
     * @return Twitterアクセストークン有無
     */
    public Boolean hasAccessToken() {
        return getAccessToken() != null ? true : false;
    }

    /**
     * Twitterアクセストークン取得
     *
     * @return Twitterアクセストークン
     */
    private AccessToken getAccessToken() {
        // キャッシュしておく
        if (accessToken != null) {
            return accessToken;
        }
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String token = preferences.getString(TOKEN, null);
        String tokenSecret = preferences.getString(TOKEN_SECRET, null);
        if (token != null && tokenSecret != null) {
            this.accessToken = new AccessToken(token, tokenSecret);
            return accessToken;
        } else {
            return null;
        }
    }

    /**
     * Twitterアクセストークン保存
     *
     * @param accessToken Twitterアクセストークン
     */
    public void setAccessToken(AccessToken accessToken) {
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putString(TOKEN, accessToken.getToken());
        editor.putString(TOKEN_SECRET, accessToken.getTokenSecret());
        editor.commit();
        this.accessToken = accessToken;
    }

    /**
     * Twitterインスタンスを取得
     *
     * @return Twitterインスタンス
     */
    public Twitter getTwitter() {
        if (twitter != null) {
            return twitter;
        }
        TwitterFactory factory = new TwitterFactory();
        Twitter twitter = factory.getInstance();
        twitter.setOAuthConsumer(getConsumerKey(), getConsumerSecret());
        AccessToken token = getAccessToken();
        if (token != null) {
            twitter.setOAuthAccessToken(token);
            // アクセストークンまである時だけキャッシュしておく
            this.twitter = twitter;
        }
        return twitter;
    }

    /**
     * TwitterStreamインスタンスを取得
     *
     * @return TwitterStreamインスタンス
     */
    public TwitterStream getTwitterStream() {
        // キャッシュしておく
        if (twitterStream != null) {
            return twitterStream;
        }
        AccessToken token = getAccessToken();
        if (token == null) {
            return null;
        }
        ConfigurationBuilder confbuilder = new ConfigurationBuilder();
        twitter4j.conf.Configuration conf = confbuilder.setOAuthConsumerKey(getConsumerKey())
                .setOAuthConsumerSecret(getConsumerSecret()).setOAuthAccessToken(token.getToken())
                .setOAuthAccessTokenSecret(token.getTokenSecret()).build();
        TwitterStream twitterStream = new TwitterStreamFactory(conf).getInstance();
        this.twitterStream = twitterStream;
        return twitterStream;
    }

    /**
     * アクセストークンを削除（認証失敗時などトークンが無効な場合に使用）
     */
    public void resetAccessToken() {
        SharedPreferences preferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.remove(TOKEN);
        editor.remove(TOKEN_SECRET);
        editor.commit();
        this.accessToken = null;
    }

    private HashMap<Long, Boolean> mIsFavMap = new HashMap<Long, Boolean>();
    private HashMap<Long, Long> mRtIdMap = new HashMap<Long, Long>();

    public void setFav(Long id) {
        mIsFavMap.put(id, true);
    }

    public void removeFav(Long id) {
        mIsFavMap.remove(id);
    }

    public Boolean isFav(Long id) {
        return mIsFavMap.get(id) != null ? true : false;
    }

    public Boolean isFav(Status status) {
        if (status.isFavorited()) {
            return true;
        }
        if (mIsFavMap.get(status.getId()) != null) {
            return true;
        }
        Status retweet = status.getRetweetedStatus();
        if (retweet == null) {
            return false;
        }
        if (retweet.isFavorited()) {
            return true;
        }
        if (mIsFavMap.get(retweet.getId()) != null) {
            return true;
        }
        return false;
    }

    public void setRtId(Long sourceId, Long retweetId) {
        mRtIdMap.put(sourceId, retweetId);
    }

    public Long getRtId(Status status) {
        Long id = mRtIdMap.get(status.getId());
        if (id != null) {
            Log.d("Justaway", "[getRtId] " + status.getId() + " => " + id);
            return id;
        }
        return null;
    }

    public void doFavorite(Long id) {
        mIsFavMap.put(id, true);
        new FavoriteTask().execute(id);
    }

    public void doDestroyFavorite(Long id) {
        mIsFavMap.remove(id);
        new UnFavoriteTask().execute(id);
    }

    public void doRetweet(Long id) {
        Log.d("Justaway", "[doRetweet] " + id + " => 0 (temp)");
        JustawayApplication.getApplication().setRtId(id, (long) 0);
        new RetweetTask().execute(id);
    }

    public void doDestroyRetweet(Long sourceId) {
        Long retweetId = mRtIdMap.get(sourceId);
        if (retweetId == null) {
            return;
        }
        Log.d("Justaway", "[doDestroyRetweet] " + sourceId + " => " + retweetId);
        mRtIdMap.remove(sourceId);
        if (retweetId > 0) {
            new UnRetweetTask().execute(retweetId);
        }
    }

    public void doDestroyStatus(long id) {
        new DestroyStatusTask().execute(id);
    }


    static final int CONTEXT_MENU_REPLY_ID = 1;
    static final int CONTEXT_MENU_FAV_ID = 2;
    static final int CONTEXT_MENU_FAVRT_ID = 3;
    static final int CONTEXT_MENU_RT_ID = 4;
    static final int CONTEXT_MENU_QT_ID = 5;
    static final int CONTEXT_MENU_LINK_ID = 6;
    static final int CONTEXT_MENU_TOFU_ID = 7;
    static final int CONTEXT_MENU_DM_ID = 8;
    static final int CONTEXT_MENU_RM_DM_ID = 9;
    static final int CONTEXT_MENU_RM_ID = 10;
    static final int CONTEXT_MENU_TALK_ID = 11;
    static final int CONTEXT_MENU_RM_FAV_ID = 12;
    static final int CONTEXT_MENU_RM_RT_ID = 13;
    static final int CONTEXT_MENU_HASH_ID = 14;
    static final int CONTEXT_MENU_AT_ID = 15;
    static final int CONTEXT_MENU_REPLY_ALL_ID = 16;

    private Row selectedRow;

    public void onCreateContextMenuForStatus(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        JustawayApplication application = sApplication;
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        ListView listView = (ListView) view;
        Row row = (Row) listView.getItemAtPosition(info.position);
        selectedRow = row;

        if (row.isDirectMessage()) {
            menu.setHeaderTitle(row.getMessage().getSenderScreenName());
            menu.add(0, CONTEXT_MENU_DM_ID, 0, "返信(DM)");
            menu.add(0, CONTEXT_MENU_RM_DM_ID, 0, "ツイ消し(DM)");
            return;
        }

        /*
         * statusの保持はActivityで行わないとなぜか2タブ目以降の値が保持できない..
         */

        Status status = row.getStatus();
        Status retweet = status.getRetweetedStatus();
        Status source = retweet != null ? retweet : status;

        menu.setHeaderTitle(status.getText());
        menu.add(0, CONTEXT_MENU_REPLY_ID, 0, "リプ");

        UserMentionEntity[] mentions = source.getUserMentionEntities();
        if (mentions.length > 1) {
            menu.add(0, CONTEXT_MENU_REPLY_ALL_ID, 0, "全員にリプ");
        }

        menu.add(0, CONTEXT_MENU_QT_ID, 0, "引用");

        if (application.isFav(status)) {
            menu.add(0, CONTEXT_MENU_RM_FAV_ID, 0, "ふぁぼを解除");
        } else {
            menu.add(0, CONTEXT_MENU_FAV_ID, 0, "ふぁぼ");
        }

        if (status.getUser().getId() == getUser().getId()) {
            if (retweet != null) {
                if (application.getRtId(status) != null) {
                    menu.add(0, CONTEXT_MENU_RM_RT_ID, 0, "公式RTを解除");
                }
            } else {
                menu.add(0, CONTEXT_MENU_RM_ID, 0, "ツイ消し");
            }
        } else if (application.getRtId(status) == null) {
            if (application.isFav(status) == false) {
                menu.add(0, CONTEXT_MENU_FAVRT_ID, 0, "ふぁぼ＆公式RT");
            }
            menu.add(0, CONTEXT_MENU_RT_ID, 0, "公式RT");
        }

        if (source.getInReplyToStatusId() > 0) {
            menu.add(0, CONTEXT_MENU_TALK_ID, 0, "会話を表示");
        }

        // ツイート内のURLへアクセスできるようにメニューに展開する
        URLEntity[] urls = source.getURLEntities();
        for (URLEntity url : urls) {
            menu.add(0, CONTEXT_MENU_LINK_ID, 0, url.getExpandedURL().toString());
        }

        // ツイート内のURL(画像)へアクセスできるようにメニューに展開する
        URLEntity[] medias = source.getMediaEntities();
        for (URLEntity url : medias) {
            menu.add(0, CONTEXT_MENU_LINK_ID, 0, url.getExpandedURL().toString());
        }

        // ツイート内のハッシュタグを検索できるようにメニューに展開する
        HashtagEntity[] hashtags = source.getHashtagEntities();
        for (HashtagEntity hashtag : hashtags) {
            menu.add(0, CONTEXT_MENU_HASH_ID, 0, "#" + hashtag.getText());
        }

        for (UserMentionEntity mention: mentions) {
            menu.add(0, CONTEXT_MENU_AT_ID, 0, "@" + mention.getScreenName());
        }

        menu.add(0, CONTEXT_MENU_TOFU_ID, 0, "TofuBuster");
    }

    public boolean onContextItemSelected(FragmentActivity activity, MenuItem item) {

        JustawayApplication application = getApplication();
        Row row = selectedRow;
        Status status = row.getStatus();
        Status retweet = status != null ? status.getRetweetedStatus() : null;
        Status source = retweet != null ? retweet : status;
        Intent intent;
        String text;

        switch (item.getItemId()) {
            case CONTEXT_MENU_REPLY_ID:
                text = "@" + source.getUser().getScreenName() + " ";
                intent = new Intent(activity, PostActivity.class);
                intent.putExtra("status", text);
                intent.putExtra("selection", text.length());
                intent.putExtra("inReplyToStatusId", status.getId());
                activity.startActivity(intent);
                return true;
            case CONTEXT_MENU_REPLY_ALL_ID:
                UserMentionEntity[] mentions = source.getUserMentionEntities();
                text = "";
                for (UserMentionEntity mention: mentions) {
                    text = text.concat("@" + mention.getScreenName() + " ");
                }
                intent = new Intent(activity, PostActivity.class);
                intent.putExtra("status", text);
                intent.putExtra("selection", text.length());
                intent.putExtra("inReplyToStatusId", status.getId());
                activity.startActivity(intent);
                return true;
            case CONTEXT_MENU_QT_ID:
                intent = new Intent(activity, PostActivity.class);
                intent.putExtra("status", " https://twitter.com/" + status.getUser().getScreenName()
                        + "/status/" + String.valueOf(status.getId()));
                intent.putExtra("inReplyToStatusId", status.getId());
                activity.startActivity(intent);
                return true;
            case CONTEXT_MENU_DM_ID:
                String msg = "D " + row.getMessage().getSenderScreenName() + " ";
                intent = new Intent(activity, PostActivity.class);
                intent.putExtra("status", msg);
                intent.putExtra("selection", msg.length());
                activity.startActivity(intent);
                return true;
            case CONTEXT_MENU_RM_DM_ID:
//                activity.doDestroyDirectMessage(row.getMessage().getId());
                return true;
            case CONTEXT_MENU_RM_ID:
                application.doDestroyStatus(row.getStatus().getId());
                return true;
            case CONTEXT_MENU_RT_ID:
                application.doRetweet(row.getStatus().getId());
                return true;
            case CONTEXT_MENU_RM_RT_ID:
                application.doDestroyRetweet(row.getStatus().getId());
                return true;
            case CONTEXT_MENU_RM_FAV_ID:
                application.doDestroyFavorite(status.getId());
                return true;
            case CONTEXT_MENU_FAV_ID:
                application.doFavorite(status.getId());
                return true;
            case CONTEXT_MENU_FAVRT_ID:
                application.doFavorite(status.getId());
                application.doRetweet(row.getStatus().getId());
                return true;
            case CONTEXT_MENU_TALK_ID:
                TalkFragment dialog = new TalkFragment();
                Bundle args = new Bundle();
                args.putLong("statusId", source.getId());
                dialog.setArguments(args);
                dialog.show(activity.getSupportFragmentManager(), "dialog");
                return true;
            case CONTEXT_MENU_LINK_ID:

                /**
                 * 現在は全てIntentでブラウザなどに飛ばしているが、 画像やツイートは自アプリで参照できるように対応する予定
                 */
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getTitle().toString()));
                activity.startActivity(intent);
                return true;
            case CONTEXT_MENU_HASH_ID:
                intent = new Intent(activity, SearchActivity.class);
                intent.putExtra("query", item.getTitle().toString());
                activity.startActivity(intent);
                return true;
            case CONTEXT_MENU_AT_ID:
                intent = new Intent(activity, ProfileActivity.class);
                intent.putExtra("screenName", item.getTitle().toString().substring(1));
                activity.startActivity(intent);
                return true;
            case CONTEXT_MENU_TOFU_ID:
                try {
                    intent = new Intent("com.product.kanzmrsw.tofubuster.ACTION_SHOW_TEXT");
                    intent.putExtra(Intent.EXTRA_TEXT, status.getText());
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Justaway");
                    intent.putExtra("isCopyEnabled", true);
                    activity.startActivity(intent); // TofuBusterがインストールされていない場合、startActivityで落ちる
                } catch (Exception e) {
                    // 露骨な誘導
                    intent = new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://market.android.com/details?id=com.product.kanzmrsw.tofubuster"));
                    activity.startActivity(intent);
                }
                return true;
            default:
                return true;
        }
    }
}
