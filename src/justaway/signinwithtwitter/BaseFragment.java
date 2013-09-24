package justaway.signinwithtwitter;

import twitter4j.Status;
import twitter4j.URLEntity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;

/**
 * タブのベースクラス
 */
public class BaseFragment extends ListFragment {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MainActivity activity = (MainActivity) getActivity();

        ListView listView = getListView();

        // コンテキストメニューを使える様にする為の指定、但しデフォルトではロングタップで開く
        registerForContextMenu(listView);

        // Status(ツイート)をViewに描写するアダプター
        TwitterAdapter adapter = new TwitterAdapter(activity,
                R.layout.tweet_row);
        setListAdapter(adapter);

        /**
         * シングルタップでコンテキストメニューを開くための指定
         */
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                view.showContextMenu();
            }
        });
    }

    public void goToTop() {
        ListView listView = getListView();
        if (listView == null) {
            getActivity().finish();
            return;
        }
        listView.setSelection(0);
    }

    public Boolean isTop() {
        ListView listView = getListView();
        if (listView == null) {
            return false;
        }
        return listView.getFirstVisiblePosition() == 0 ? true : false;
    }

    /**
     * UserStreamでonStatusを受信した時の挙動
     * 
     * @param status
     */
    public void onStatus(Status status) {

    };

    static final int CONTEXT_MENU_REPLY_ID = 1;
    static final int CONTEXT_MENU_FAV_ID = 2;
    static final int CONTEXT_MENU_FAVRT_ID = 3;
    static final int CONTEXT_MENU_RT_ID = 4;
    static final int CONTEXT_MENU_QT_ID = 5;
    static final int CONTEXT_MENU_LINK_ID = 6;
    static final int CONTEXT_MENU_TOFU_ID = 7;

    public void onCreateContextMenu(ContextMenu menu, View view,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);

        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        ListView listView = (ListView) view;
        Status status = (Status) listView.getItemAtPosition(info.position);

        /*
         * statusの保持はActivityで行わないとなぜか2タブ目以降の値が保持できない..
         */
        MainActivity activity = (MainActivity) getActivity();
        activity.setSelectedStatus(status);

        Status retweet = status.getRetweetedStatus();

        menu.setHeaderTitle(status.getText());
        menu.add(0, CONTEXT_MENU_REPLY_ID, 0, "リプ");
        menu.add(0, CONTEXT_MENU_QT_ID, 0, "引用");
        menu.add(0, CONTEXT_MENU_FAV_ID, 0, "ふぁぼ");
        menu.add(0, CONTEXT_MENU_FAVRT_ID, 0, "ふぁぼ＆公式RT");
        menu.add(0, CONTEXT_MENU_RT_ID, 0, "公式RT");

        // ツイート内のURLへアクセスできるようにメニューに展開する
        URLEntity[] urls = retweet != null ? retweet.getURLEntities() : status
                .getURLEntities();
        for (URLEntity url : urls) {
            menu.add(0, CONTEXT_MENU_LINK_ID, 0, url.getExpandedURL()
                    .toString());
        }

        // ツイート内のURL(画像)へアクセスできるようにメニューに展開する
        URLEntity[] medias = retweet != null ? retweet.getMediaEntities()
                : status.getMediaEntities();
        for (URLEntity url : medias) {
            menu.add(0, CONTEXT_MENU_LINK_ID, 0, url.getExpandedURL()
                    .toString());
        }

        menu.add(0, CONTEXT_MENU_TOFU_ID, 0, "TofuBuster");
    }

    public boolean onContextItemSelected(MenuItem item) {

        MainActivity activity = (MainActivity) getActivity();
        Status status = activity.getSelectedStatus();
        Intent intent;

        switch (item.getItemId()) {
        case CONTEXT_MENU_REPLY_ID:
            intent = new Intent(activity, PostActivity.class);
            String text = "@" + status.getUser().getScreenName() + " ";
            intent.putExtra("status", text);
            intent.putExtra("selection", text.length());
            intent.putExtra("inReplyToStatusId", status.getId());
            startActivity(intent);
            return true;
        case CONTEXT_MENU_QT_ID:
            intent = new Intent(activity, PostActivity.class);
            intent.putExtra("status",
                    " https://twitter.com/" + status.getUser().getScreenName()
                            + "/status/" + String.valueOf(status.getId()));
            intent.putExtra("inReplyToStatusId", status.getId());
            startActivity(intent);
            return true;
        case CONTEXT_MENU_RT_ID:
            activity.doRetweet(status.getId());
            return true;
        case CONTEXT_MENU_FAV_ID:
            activity.doFavorite(status.getId());
            return true;
        case CONTEXT_MENU_FAVRT_ID:
            activity.doFavorite(status.getId());
            activity.doRetweet(status.getId());
            return true;
        case CONTEXT_MENU_LINK_ID:

            /**
             * 現在は全てIntentでブラウザなどに飛ばしているが、 画像やツイートは自アプリで参照できるように対応する予定
             */
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getTitle()
                    .toString()));
            startActivity(intent);
            return true;
        case CONTEXT_MENU_TOFU_ID:
            try {
                intent = new Intent(
                        "com.product.kanzmrsw.tofubuster.ACTION_SHOW_TEXT");
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
            return super.onContextItemSelected(item);
        }
    }
}
