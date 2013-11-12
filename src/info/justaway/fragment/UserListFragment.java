package info.justaway.fragment;

import twitter4j.ResponseList;
import twitter4j.Status;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.util.LongSparseArray;
import android.view.View;
import android.widget.ListView;
import info.justaway.JustawayApplication;
import info.justaway.MainActivity;
import info.justaway.adapter.TwitterAdapter;
import info.justaway.model.Row;
import info.justaway.task.UserListStatusesLoader;

public class UserListFragment extends BaseFragment implements
        LoaderManager.LoaderCallbacks<ResponseList<Status>> {

    private int userListId;
    private LongSparseArray<Boolean> members = new LongSparseArray<Boolean>();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        userListId = getArguments().getInt("userListId");
        getLoaderManager().initLoader(0, null, this);
    }

    public void reload() {
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void add(final Row row) {
        final ListView listView = getListView();
        if (listView == null) {
            return;
        }

        if (members.get(row.getStatus().getUser().getId()) == null) {
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
                    activity.onNewListStatus(userListId, false);
                } else {
                    activity.onNewListStatus(userListId, true);
                }
            }
        });
    }

    @Override
    public Loader<ResponseList<Status>> onCreateLoader(int arg0, Bundle args) {
        return new UserListStatusesLoader(getActivity(), userListId);
    }

    @Override
    public void onLoadFinished(Loader<ResponseList<Status>> arg0, ResponseList<Status> statuses) {
        if (statuses != null) {
            TwitterAdapter adapter = (TwitterAdapter) getListAdapter();
            adapter.clear();
            for (twitter4j.Status status : statuses) {
                adapter.add(Row.newStatus(status));
                members.append(status.getUser().getId(), true);
            }
        } else {
            JustawayApplication.showToast("UserListStatusesの取得に失敗しました＞＜");
        }
    }

    @Override
    public void onLoaderReset(Loader<ResponseList<Status>> arg0) {
    }
}
