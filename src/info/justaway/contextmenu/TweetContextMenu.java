package info.justaway.contextmenu;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import java.util.List;

import info.justaway.JustawayApplication;
import info.justaway.MainActivity;
import info.justaway.PostActivity;
import info.justaway.ProfileActivity;
import info.justaway.R;
import info.justaway.SearchActivity;
import info.justaway.fragment.AroundFragment;
import info.justaway.fragment.RetweetersFragment;
import info.justaway.fragment.TalkFragment;
import info.justaway.model.Row;
import info.justaway.plugin.TwiccaPlugin;
import twitter4j.HashtagEntity;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;

/**
 * ツイート
 */
public class TweetContextMenu {

    private Row mRow;
    private FragmentActivity mActivity;

    static final int CLOSED_MENU_DELAY = 800;

    static final int CONTEXT_MENU_REPLY_ID = 1;
    static final int CONTEXT_MENU_FAV_ID = 2;
    static final int CONTEXT_MENU_FAVRT_ID = 3;
    static final int CONTEXT_MENU_RT_ID = 4;
    static final int CONTEXT_MENU_QT_ID = 5;
    static final int CONTEXT_MENU_LINK_ID = 6;
    static final int CONTEXT_MENU_DM_ID = 8;
    static final int CONTEXT_MENU_RM_DM_ID = 9;
    static final int CONTEXT_MENU_RM_ID = 10;
    static final int CONTEXT_MENU_TALK_ID = 11;
    static final int CONTEXT_MENU_RM_FAV_ID = 12;
    static final int CONTEXT_MENU_RM_RT_ID = 13;
    static final int CONTEXT_MENU_HASH_ID = 14;
    static final int CONTEXT_MENU_AT_ID = 15;
    static final int CONTEXT_MENU_REPLY_ALL_ID = 16;
    static final int CONTEXT_MENU_SHARE_TEXT_ID = 17;
    static final int CONTEXT_MENU_SHARE_URL_ID = 18;
    static final int CONTEXT_MENU_AROUND_ID = 19;
    static final int CONTEXT_MENU_RETWEETERS_ID = 20;
    static final int CONTEXT_MENU_TWICCA_SHOW_TEXT_BASE_ID = 100;

    private List<ResolveInfo> mTwiccaPlugins;

    public TweetContextMenu(FragmentActivity activity, ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        ListView listView = (ListView) view;
        Row row = (Row) listView.getItemAtPosition(info.position);
        if (row == null) {
            return;
        }
        mRow = row;
        mActivity = activity;

        // ダイレクトメッセージの場合
        if (row.isDirectMessage()) {
            menu.setHeaderTitle(row.getMessage().getSenderScreenName());
            menu.add(0, CONTEXT_MENU_DM_ID, 0, R.string.context_menu_reply_direct_message);
            menu.add(0, CONTEXT_MENU_RM_DM_ID, 0, R.string.context_menu_destroy_direct_message);
            return;
        }

        twitter4j.Status status = row.getStatus();
        twitter4j.Status retweet = status.getRetweetedStatus();
        twitter4j.Status source = retweet != null ? retweet : status;
        Boolean isPublic = !source.getUser().isProtected();

        JustawayApplication application = JustawayApplication.getApplication();

        menu.setHeaderTitle(status.getText());
        menu.add(0, CONTEXT_MENU_REPLY_ID, 0, R.string.context_menu_reply);

        UserMentionEntity[] mentions = source.getUserMentionEntities();
        if (mentions.length > 1 || (mentions.length == 1 && !mentions[0].getScreenName().equals(application.getScreenName()))) {
            menu.add(0, CONTEXT_MENU_REPLY_ALL_ID, 0, R.string.context_menu_reply_all);
        }

        if (isPublic) {
            menu.add(0, CONTEXT_MENU_QT_ID, 0, R.string.context_menu_qt);
        }

        if (application.isFav(status)) {
            menu.add(0, CONTEXT_MENU_RM_FAV_ID, 0, R.string.context_menu_destroy_favorite);
        } else {
            menu.add(0, CONTEXT_MENU_FAV_ID, 0, R.string.context_menu_create_favorite);
        }

        if (status.getUser().getId() == application.getUserId()) {
            if (retweet != null) {
                if (application.getRtId(status) != null) {
                    menu.add(0, CONTEXT_MENU_RM_RT_ID, 0, R.string.context_menu_destroy_retweet);
                }
            } else {
                menu.add(0, CONTEXT_MENU_RM_ID, 0, R.string.context_menu_destroy_status);
            }
        } else if (application.getRtId(status) == null) {
            if (isPublic) {
                if (!application.isFav(status)) {
                    menu.add(0, CONTEXT_MENU_FAVRT_ID, 0, R.string.context_menu_favorite_and_retweet);
                }
                menu.add(0, CONTEXT_MENU_RT_ID, 0, R.string.context_menu_retweet);
            }
        }

        if (source.getRetweetCount() > 0) {
            menu.add(0, CONTEXT_MENU_RETWEETERS_ID, 0, R.string.context_menu_show_retweeters);
        }
        if (source.getInReplyToStatusId() > 0) {
            menu.add(0, CONTEXT_MENU_TALK_ID, 0, R.string.context_menu_talk);
        }
        menu.add(0, CONTEXT_MENU_AROUND_ID, 0, R.string.context_menu_show_around);

        // ツイート内のURLへアクセスできるようにメニューに展開する
        URLEntity[] urls = source.getURLEntities();
        for (URLEntity url : urls) {
            menu.add(0, CONTEXT_MENU_LINK_ID, 0, url.getExpandedURL());
        }

        // ツイート内のURL(画像)へアクセスできるようにメニューに展開する
        URLEntity[] medias = source.getMediaEntities();
        for (URLEntity url : medias) {
            menu.add(0, CONTEXT_MENU_LINK_ID, 0, url.getExpandedURL());
        }

        // ツイート内のハッシュタグを検索できるようにメニューに展開する
        HashtagEntity[] hashtags = source.getHashtagEntities();
        for (HashtagEntity hashtag : hashtags) {
            menu.add(0, CONTEXT_MENU_HASH_ID, 0, "#" + hashtag.getText());
        }

        for (UserMentionEntity mention : mentions) {
            menu.add(0, CONTEXT_MENU_AT_ID, 0, "@" + mention.getScreenName());
        }

        if (!isPublic) {
            return;
        }

        // twiccaプラグイン実装 IDは被らないように100~にしてる　
        if (mTwiccaPlugins == null) {
            mTwiccaPlugins = TwiccaPlugin.getResolveInfo(mActivity.getPackageManager(), TwiccaPlugin.TWICCA_ACTION_SHOW_TWEET);
        }
        if (!mTwiccaPlugins.isEmpty()) {
            PackageManager pm = mActivity.getPackageManager();
            int i = 0;
            for (ResolveInfo resolveInfo : mTwiccaPlugins) {
                if (pm == null || resolveInfo.activityInfo == null) {
                    continue;
                }
                menu.add(0, CONTEXT_MENU_TWICCA_SHOW_TEXT_BASE_ID + i, 0, resolveInfo.activityInfo.loadLabel(pm));
                i++;
            }
        }

        menu.add(0, CONTEXT_MENU_SHARE_TEXT_ID, 0, R.string.context_menu_share_text);
        menu.add(0, CONTEXT_MENU_SHARE_URL_ID, 0, R.string.context_menu_share_url);
    }

    public boolean onContextItemSelected(MenuItem item) {
        JustawayApplication application = JustawayApplication.getApplication();
        Row row = mRow;
        Status status = row.getStatus();
        Status retweet = status != null ? status.getRetweetedStatus() : null;
        Status source = retweet != null ? retweet : status;
        Intent intent;
        String text;
        EditText editStatus = null;
        View singleLineTweet = mActivity.findViewById(R.id.quick_tweet_layout);
        if (singleLineTweet != null && singleLineTweet.getVisibility() == View.VISIBLE) {
            editStatus = (EditText) mActivity.findViewById(R.id.quick_tweet_edit);
        }

        switch (item.getItemId()) {
            case CONTEXT_MENU_DM_ID:
                text = "D " + row.getMessage().getSenderScreenName() + " ";
                if (editStatus != null) {
                    editStatus.requestFocus();
                    editStatus.setText(text);
                    editStatus.setSelection(text.length());
                    application.showKeyboard(editStatus, CLOSED_MENU_DELAY);
                    return true;
                }
                intent = new Intent(mActivity, PostActivity.class);
                intent.putExtra("status", text);
                intent.putExtra("selection", text.length());
                mActivity.startActivity(intent);
                return true;
            case CONTEXT_MENU_RM_DM_ID:
                MainActivity mainActivity = (MainActivity) mActivity;
                mainActivity.doDestroyDirectMessage(row.getMessage().getId());
                return true;
        }

        if (status == null) {
            return true;
        }

        int itemId = item.getItemId();
        UserMentionEntity[] mentions = source.getUserMentionEntities();
        switch (itemId) {
            case CONTEXT_MENU_REPLY_ID:
                if (source.getUser().getId() == application.getUserId() && mentions.length == 1) {
                    text = "@" + mentions[0].getScreenName() + " ";
                } else {
                    text = "@" + source.getUser().getScreenName() + " ";
                }
                if (editStatus != null) {
                    editStatus.requestFocus();
                    editStatus.setText(text);
                    editStatus.setSelection(text.length());
                    ((MainActivity) mActivity).setInReplyToStatusId(status.getId());
                    application.showKeyboard(editStatus, CLOSED_MENU_DELAY);
                    return true;
                }
                intent = new Intent(mActivity, PostActivity.class);
                intent.putExtra("status", text);
                intent.putExtra("selection", text.length());
                intent.putExtra("inReplyToStatusId", status.getId());
                mActivity.startActivity(intent);
                return true;
            case CONTEXT_MENU_REPLY_ALL_ID:
                if (source.getUser().getId() == application.getUserId()) {
                    text = "";
                } else {
                    text = "@" + source.getUser().getScreenName() + " ";
                }
                for (UserMentionEntity mention : mentions) {
                    if (source.getUser().getScreenName().equals(mention.getScreenName())) {
                        continue;
                    }
                    if (application.getScreenName().equals(mention.getScreenName())) {
                        continue;
                    }
                    text = text.concat("@" + mention.getScreenName() + " ");
                }
                if (editStatus != null) {
                    editStatus.requestFocus();
                    editStatus.setText(text);
                    editStatus.setSelection(text.length());
                    ((MainActivity) mActivity).setInReplyToStatusId(status.getId());
                    application.showKeyboard(editStatus, CLOSED_MENU_DELAY);
                    return true;
                }
                intent = new Intent(mActivity, PostActivity.class);
                intent.putExtra("status", text);
                intent.putExtra("selection", text.length());
                intent.putExtra("inReplyToStatusId", status.getId());
                mActivity.startActivity(intent);
                return true;
            case CONTEXT_MENU_QT_ID:
                text = " https://twitter.com/" + source.getUser().getScreenName()
                        + "/status/" + String.valueOf(source.getId());
                if (editStatus != null) {
                    editStatus.requestFocus();
                    editStatus.setText(text);
                    ((MainActivity) mActivity).setInReplyToStatusId(source.getId());
                    application.showKeyboard(editStatus, CLOSED_MENU_DELAY);
                    return true;
                }
                intent = new Intent(mActivity, PostActivity.class);
                intent.putExtra("status", text);
                intent.putExtra("inReplyToStatusId", source.getId());
                mActivity.startActivity(intent);
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
            case CONTEXT_MENU_RETWEETERS_ID:
                RetweetersFragment retweetersFragment = new RetweetersFragment();
                Bundle retweetsArgs = new Bundle();
                retweetsArgs.putLong("statusId", source.getId());
                retweetersFragment.setArguments(retweetsArgs);
                retweetersFragment.show(mActivity.getSupportFragmentManager(), "dialog");
                return true;
            case CONTEXT_MENU_TALK_ID:
                TalkFragment dialog = new TalkFragment();
                Bundle args = new Bundle();
                args.putLong("statusId", source.getId());
                dialog.setArguments(args);
                dialog.show(mActivity.getSupportFragmentManager(), "dialog");
                return true;
            case CONTEXT_MENU_AROUND_ID:
                AroundFragment aroundFragment = new AroundFragment();
                Bundle aroundArgs = new Bundle();
                aroundArgs.putSerializable("status", source);
                aroundFragment.setArguments(aroundArgs);
                aroundFragment.show(mActivity.getSupportFragmentManager(), "dialog");
                return true;
            case CONTEXT_MENU_LINK_ID:

                /**
                 * 現在は全てIntentでブラウザなどに飛ばしているが、 画像やツイートは自アプリで参照できるように対応する予定
                 */
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getTitle().toString()));
                mActivity.startActivity(intent);
                return true;
            case CONTEXT_MENU_HASH_ID:
                intent = new Intent(mActivity, SearchActivity.class);
                intent.putExtra("query", item.getTitle().toString());
                mActivity.startActivity(intent);
                return true;
            case CONTEXT_MENU_AT_ID:
                intent = new Intent(mActivity, ProfileActivity.class);
                intent.putExtra("screenName", item.getTitle().toString().substring(1));
                mActivity.startActivity(intent);
                return true;
            case CONTEXT_MENU_SHARE_TEXT_ID:
                intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, status.getText());
                mActivity.startActivity(intent);
                return true;
            case CONTEXT_MENU_SHARE_URL_ID:
                intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, "https://twitter.com/" + source.getUser().getScreenName()
                        + "/status/" + String.valueOf(source.getId()));
                mActivity.startActivity(intent);
                return true;
            default:
                if (itemId >= CONTEXT_MENU_TWICCA_SHOW_TEXT_BASE_ID) {
                    ResolveInfo resolveInfo = mTwiccaPlugins.get(itemId - CONTEXT_MENU_TWICCA_SHOW_TEXT_BASE_ID);
                    if (resolveInfo.activityInfo == null) {
                        return true;
                    }
                    intent = TwiccaPlugin.createIntentShowTweet(status, resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
                    mActivity.startActivity(intent);
                }
                return true;
        }
    }
}
