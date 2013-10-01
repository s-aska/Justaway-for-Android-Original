package info.justaway.fragment;

import info.justaway.JustawayApplication;
import info.justaway.MainActivity;
import info.justaway.adapter.TwitterAdapter;
import info.justaway.model.Row;

import java.util.Collections;
import java.util.Comparator;

import twitter4j.DirectMessage;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class DirectMessageFragment extends BaseFragment {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        /**
         * Streamingだけだと淋しいので、初期化時にMeationsTimelineを読み込む
         */
        new LoadDirectMessages().execute();
    }

    /**
     * ページ最上部だと自動的に読み込まれ、スクロールしていると動かないという美しい挙動
     */
    public void add(final Row row) {
        final ListView listView = getListView();
        if (listView == null) {
            return;
        }

        listView.post(new Runnable() {
            @Override
            public void run() {

                // 表示している要素の位置
                int position = listView.getFirstVisiblePosition();

                // 縦スクロール位置
                View view = listView.getChildAt(0);
                int y = view != null ? view.getTop() : 0;

                // 要素を上に追加（ addだと下に追加されてしまう ）
                TwitterAdapter adapter = (TwitterAdapter) listView.getAdapter();
                adapter.insert(row, 0);

                // 少しでもスクロールさせている時は画面を動かさない様にスクロー位置を復元する
                MainActivity activity = (MainActivity) getActivity();
                if (position != 0 || y != 0) {
                    listView.setSelectionFromTop(position + 1, y);
                    activity.onNewDirectMessage(false);
                } else {
                    activity.onNewDirectMessage(true);
                }
            }
        });
    }

    public void remove(final long directMessageId) {
        final ListView listView = getListView();
        if (listView == null) {
            return;
        }

        listView.post(new Runnable() {
            @Override
            public void run() {

                TwitterAdapter adapter = (TwitterAdapter) listView.getAdapter();
                adapter.removeDirectMessage(directMessageId);
            }
        });
    }

    private class LoadDirectMessages extends AsyncTask<String, Void, ResponseList<DirectMessage>> {

        @Override
        protected ResponseList<DirectMessage> doInBackground(String... params) {
            try {
                Twitter twitter = JustawayApplication.getApplication().getTwitter();
                ResponseList<DirectMessage> statuses = twitter.getDirectMessages();
                statuses.addAll(twitter.getSentDirectMessages());
                Collections.sort(statuses, new Comparator<DirectMessage>() {

                    @Override
                    public int compare(DirectMessage arg0, DirectMessage arg1) {
                        return ((DirectMessage) arg1).getCreatedAt().compareTo(
                                ((DirectMessage) arg0).getCreatedAt());
                    }
                });
                return statuses;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ResponseList<DirectMessage> statuses) {
            if (statuses != null) {
                ListView listView = getListView();
                TwitterAdapter adapter = (TwitterAdapter) listView.getAdapter();
                adapter.clear();
                for (DirectMessage status : statuses) {
                    adapter.add(Row.newDirectMessage(status));
                }
            } else {
                JustawayApplication.showToast("DirectMessagesの取得に失敗しました＞＜");
            }
        }
    }
}
