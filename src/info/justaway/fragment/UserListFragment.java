package info.justaway.fragment;

import twitter4j.ResponseList;
import twitter4j.Status;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import info.justaway.JustawayApplication;
import info.justaway.adapter.TwitterAdapter;
import info.justaway.model.Row;
import info.justaway.task.UserListStatusesLoader;

public class UserListFragment extends BaseFragment implements
        LoaderManager.LoaderCallbacks<ResponseList<Status>> {

    private static int userListId;

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
    public void add(Row row) {
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
            }
        } else {
            JustawayApplication.showToast("UserListStatusesの取得に失敗しました＞＜");
        }
    }

    @Override
    public void onLoaderReset(Loader<ResponseList<Status>> arg0) {
    }
}
