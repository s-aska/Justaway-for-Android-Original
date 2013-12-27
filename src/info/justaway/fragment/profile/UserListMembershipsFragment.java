package info.justaway.fragment.profile;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.ProgressBar;

import info.justaway.JustawayApplication;
import info.justaway.R;
import info.justaway.adapter.UserListAdapter;
import twitter4j.PagableResponseList;
import twitter4j.User;
import twitter4j.UserList;

/**
 * Created by teshi on 2013/12/21.
 */
public class UserListMembershipsFragment extends Fragment {
    private UserListAdapter mAdapter;
    private long mUserId;
    private long mCursor = -1;
    private ProgressBar mFooter;
    private Boolean mAutoLoader = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.list, container, false);

        User user = (User) getArguments().getSerializable("user");
        mUserId = user.getId();

        // リストビューの設定
        ListView listView = (ListView) v.findViewById(R.id.list_view);

        // コンテキストメニューを使える様にする為の指定、但しデフォルトではロングタップで開く
        registerForContextMenu(listView);

        mFooter = (ProgressBar) v.findViewById(R.id.guruguru);
        mFooter.setVisibility(View.GONE);

        // Status(ツイート)をViewに描写するアダプター
        mAdapter = new UserListAdapter(getActivity(), R.layout.row_user_list);
        listView.setAdapter(mAdapter);

        new FriendsListTask().execute(mUserId);

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // 最後までスクロールされたかどうかの判定
                if (totalItemCount == firstVisibleItem + visibleItemCount) {
                    additionalReading();
                }
            }
        });
        return v;
    }

    private void additionalReading() {
        if (!mAutoLoader) {
            return;
        }
        mFooter.setVisibility(View.VISIBLE);
        new FriendsListTask().execute(mUserId);
    }

    private class FriendsListTask extends AsyncTask<Long, Void, PagableResponseList<UserList>> {
        @Override
        protected PagableResponseList<UserList> doInBackground(Long... params) {
            try {
                PagableResponseList<UserList> userLists = JustawayApplication.getApplication().getTwitter().getUserListMemberships(params[0], mCursor);
                mCursor = userLists.getNextCursor();
                return userLists;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(PagableResponseList<UserList> userLists) {
            mFooter.setVisibility(View.GONE);
            if (userLists == null) {
                return;
            }
            for (UserList userlist : userLists) {
                mAdapter.add(userlist);
            }
            if (userLists.hasNext()) {
                mAutoLoader = true;
            }
        }
    }
}
