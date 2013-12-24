package info.justaway.fragment;

import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.User;

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
import info.justaway.model.UserListStatusesWithMembers;
import info.justaway.task.UserListStatusesLoader;

public class UserListFragment extends BaseFragment implements
        LoaderManager.LoaderCallbacks<UserListStatusesWithMembers> {

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
    public Loader<UserListStatusesWithMembers> onCreateLoader(int arg0, Bundle args) {
        return new UserListStatusesLoader(getActivity(), userListId);
    }

    @Override
    public void onLoadFinished(Loader<UserListStatusesWithMembers> arg0,
                               UserListStatusesWithMembers response) {
        if (response == null) {
            return;
        }
        ResponseList<Status> statuses = response.getStatues();
        if (statuses == null) {
            return;
        }
        TwitterAdapter adapter = (TwitterAdapter) getListAdapter();
        adapter.clear();
        for (twitter4j.Status status : statuses) {
            adapter.add(Row.newStatus(status));
            // 最初のツイートに登場ユーザーをStreaming APIからの取り込み対象にすることでAPI節約!!!
            members.append(status.getUser().getId(), true);
        }
        // Listメンバー取り込み(API Limitが厳しい為、20件迄)
        ResponseList<User> listMembers = response.getMembers();
        if (listMembers != null) {
            for (User user : listMembers) {
                members.append(user.getId(), true);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<UserListStatusesWithMembers> arg0) {
    }
}
