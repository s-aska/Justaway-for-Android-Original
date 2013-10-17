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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle args = new Bundle();
        int position = getArguments().getInt("position");
        int id = JustawayApplication.getApplication().getLists().get(position);
        args.putInt("userListId", id);
        getLoaderManager().initLoader(0, args, this);
    }

    @Override
    public void add(Row row) {
        // TODO Auto-generated method stub

    }

    @Override
    public Loader<ResponseList<Status>> onCreateLoader(int arg0, Bundle args) {
        return new UserListStatusesLoader(getActivity(), args.getInt("userListId"));
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
