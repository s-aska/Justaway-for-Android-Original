package info.justaway.fragment;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListView;

import info.justaway.MainActivity;
import info.justaway.adapter.TwitterAdapter;
import info.justaway.model.Row;
import info.justaway.task.DirectMessageLoader;
import twitter4j.DirectMessage;
import twitter4j.ResponseList;

public class DirectMessageFragment extends BaseFragment implements
        LoaderManager.LoaderCallbacks<ResponseList<DirectMessage>> {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    /**
     * ページ最上部だと自動的に読み込まれ、スクロールしていると動かないという美しい挙動
     */
    public void add(final Row row) {
        final ListView listView = getListView();
        if (listView == null) {
            return;
        }

        if (!row.isDirectMessage()) {
            return;
        }

        final TwitterAdapter adapter = (TwitterAdapter) listView.getAdapter();
        listView.post(new Runnable() {
            @Override
            public void run() {

                // 表示している要素の位置
                int position = listView.getFirstVisiblePosition();

                // 縦スクロール位置
                View view = listView.getChildAt(0);
                int y = view != null ? view.getTop() : 0;

                // 要素を上に追加（ addだと下に追加されてしまう ）
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
        ListView listView = getListView();
        if (listView == null) {
            return;
        }

        final TwitterAdapter adapter = (TwitterAdapter) listView.getAdapter();
        listView.post(new Runnable() {
            @Override
            public void run() {
                adapter.removeDirectMessage(directMessageId);
            }
        });
    }

    @Override
    public Loader<ResponseList<DirectMessage>> onCreateLoader(int arg0, Bundle arg1) {
        return new DirectMessageLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<ResponseList<DirectMessage>> arg0,
                               ResponseList<DirectMessage> statuses) {
        if (statuses == null) {
            return;
        }
        TwitterAdapter adapter = (TwitterAdapter) getListAdapter();
        adapter.clear();
        for (DirectMessage status : statuses) {
            adapter.add(Row.newDirectMessage(status));
        }
    }

    @Override
    public void onLoaderReset(Loader<ResponseList<DirectMessage>> arg0) {
    }
}
