package info.justaway;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import info.justaway.fragment.TalkFragment;
import info.justaway.model.Row;
import twitter4j.HashtagEntity;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;

public class BaseActivity extends FragmentActivity {

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

    /**
     * コンテキストメニュー表示時の選択したツイートをセットしている Streaming API対応で勝手に画面がスクロールされる為、
     * positionから取得されるitemが変わってしまい、どこかに保存する必要があった
     */
    private Row mSelectedRow;
    private Long mInReplyToStatusId;

    public Long getInReplyToStatusId() {
        return mInReplyToStatusId;
    }

    public void setInReplyToStatusId(Long inReplyToStatusId) {
        this.mInReplyToStatusId = inReplyToStatusId;
    }

    public void onCreateContextMenuForStatus(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        ListView listView = (ListView) view;
        Row row = (Row) listView.getItemAtPosition(info.position);
        if (row == null) {
            return;
        }
        mSelectedRow = row;

        if (row.isDirectMessage()) {
            menu.setHeaderTitle(row.getMessage().getSenderScreenName());
            menu.add(0, CONTEXT_MENU_DM_ID, 0, "返信(DM)");
            menu.add(0, CONTEXT_MENU_RM_DM_ID, 0, "ツイ消し(DM)");
            return;
        }
        Status status = row.getStatus();
        Status retweet = status.getRetweetedStatus();
        Status source = retweet != null ? retweet : status;

        JustawayApplication application = JustawayApplication.getApplication();

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

        if (status.getUser().getId() == application.getUserId()) {
            if (retweet != null) {
                if (application.getRtId(status) != null) {
                    menu.add(0, CONTEXT_MENU_RM_RT_ID, 0, "公式RTを解除");
                }
            } else {
                menu.add(0, CONTEXT_MENU_RM_ID, 0, "ツイ消し");
            }
        } else if (application.getRtId(status) == null) {
            if (!application.isFav(status)) {
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

        menu.add(0, CONTEXT_MENU_TOFU_ID, 0, "TofuBuster");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        JustawayApplication application = JustawayApplication.getApplication();
        Row row = mSelectedRow;
        Status status = row.getStatus();
        if (status == null) {
            return false;
        }
        Status retweet = status.getRetweetedStatus();
        Status source = retweet != null ? retweet : status;
        Intent intent;
        String text;
        EditText editStatus = null;
        View singleLineTweet = findViewById(R.id.quick_tweet_layout);
        if (singleLineTweet != null && singleLineTweet.getVisibility() == View.VISIBLE) {
            editStatus = (EditText) findViewById(R.id.quick_tweet_edit);
        }

        switch (item.getItemId()) {
            case CONTEXT_MENU_REPLY_ID:
                text = "@" + source.getUser().getScreenName() + " ";
                if (editStatus != null) {
                    editStatus.requestFocus();
                    editStatus.setText(text);
                    editStatus.setSelection(text.length());
                    setInReplyToStatusId(status.getId());
                    application.showKeyboard(editStatus);
                    return true;
                }
                intent = new Intent(this, PostActivity.class);
                intent.putExtra("status", text);
                intent.putExtra("selection", text.length());
                intent.putExtra("inReplyToStatusId", status.getId());
                startActivity(intent);
                return true;
            case CONTEXT_MENU_REPLY_ALL_ID:
                text = "@" + source.getUser().getScreenName() + " ";
                UserMentionEntity[] mentions = source.getUserMentionEntities();
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
                    setInReplyToStatusId(status.getId());
                    application.showKeyboard(editStatus);
                    return true;
                }
                intent = new Intent(this, PostActivity.class);
                intent.putExtra("status", text);
                intent.putExtra("selection", text.length());
                intent.putExtra("inReplyToStatusId", status.getId());
                startActivity(intent);
                return true;
            case CONTEXT_MENU_QT_ID:
                text = " https://twitter.com/" + status.getUser().getScreenName()
                        + "/status/" + String.valueOf(status.getId());
                if (editStatus != null) {
                    editStatus.requestFocus();
                    editStatus.setText(text);
                    setInReplyToStatusId(status.getId());
                    application.showKeyboard(editStatus);
                    return true;
                }
                intent = new Intent(this, PostActivity.class);
                intent.putExtra("status", text);
                intent.putExtra("inReplyToStatusId", status.getId());
                startActivity(intent);
                return true;
            case CONTEXT_MENU_DM_ID:
                text = "D " + row.getMessage().getSenderScreenName() + " ";
                if (editStatus != null) {
                    editStatus.requestFocus();
                    editStatus.setText(text);
                    editStatus.setSelection(text.length());
                    application.showKeyboard(editStatus);
                    return true;
                }
                intent = new Intent(this, PostActivity.class);
                intent.putExtra("status", text);
                intent.putExtra("selection", text.length());
                startActivity(intent);
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
                dialog.show(getSupportFragmentManager(), "dialog");
                return true;
            case CONTEXT_MENU_LINK_ID:

                /**
                 * 現在は全てIntentでブラウザなどに飛ばしているが、 画像やツイートは自アプリで参照できるように対応する予定
                 */
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getTitle().toString()));
                startActivity(intent);
                return true;
            case CONTEXT_MENU_HASH_ID:
                intent = new Intent(this, SearchActivity.class);
                intent.putExtra("query", item.getTitle().toString());
                startActivity(intent);
                return true;
            case CONTEXT_MENU_AT_ID:
                intent = new Intent(this, ProfileActivity.class);
                intent.putExtra("screenName", item.getTitle().toString().substring(1));
                startActivity(intent);
                return true;
            case CONTEXT_MENU_TOFU_ID:
                try {
                    intent = new Intent("com.product.kanzmrsw.tofubuster.ACTION_SHOW_TEXT");
                    intent.putExtra(Intent.EXTRA_TEXT, status.getText());
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Justaway");
                    intent.putExtra("isCopyEnabled", true);
                    startActivity(intent); // TofuBusterがインストールされていない場合、startActivityで落ちる
                } catch (Exception e) {
                    // 露骨な誘導
                    intent = new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://market.android.com/details?id=com.product.kanzmrsw.tofubuster"));
                    startActivity(intent);
                }
                return true;
            default:
                return true;
        }
    }
}
