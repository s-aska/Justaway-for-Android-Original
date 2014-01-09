package info.justaway.fragment.list;


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
import info.justaway.adapter.FriendListAdapter;
import twitter4j.PagableResponseList;
import twitter4j.User;

public class UserMemberFragment extends Fragment {
    private FriendListAdapter mAdapter;
    private int mListId;
    private long mCursor = -1;
    private ListView mListView;
    private ProgressBar mFooter;
    private Boolean mAutoLoader = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.list, container, false);
        if (v == null) {
            return null;
        }

        mListId = getArguments().getInt("listId");

        // リストビューの設定
        mListView = (ListView) v.findViewById(R.id.list_view);
        mListView.setVisibility(View.GONE);

        // コンテキストメニューを使える様にする為の指定、但しデフォルトではロングタップで開く
        registerForContextMenu(mListView);

        mFooter = (ProgressBar) v.findViewById(R.id.guruguru);

        mAdapter = new FriendListAdapter(getActivity(), R.layout.row_user);
        mListView.setAdapter(mAdapter);

        new UserListMembersTask().execute(mListId);

        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {

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
        mAutoLoader = false;
        new UserListMembersTask().execute(mListId);
    }


    private class UserListMembersTask extends AsyncTask<Integer, Void, PagableResponseList<User>> {
        @Override
        protected PagableResponseList<User> doInBackground(Integer... params) {
            try {
                PagableResponseList<User> userListsMembers = JustawayApplication.getApplication().getTwitter().getUserListMembers(params[0], mCursor);
                mCursor = userListsMembers.getNextCursor();
                return userListsMembers;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(PagableResponseList<User> userListsMembers) {
            mFooter.setVisibility(View.GONE);
            if (userListsMembers == null) {
                return;
            }
            for (User user : userListsMembers) {
                mAdapter.add(user);
            }
            if (userListsMembers.hasNext()) {
                mAutoLoader = true;
            }
            mListView.setVisibility(View.VISIBLE);
        }
    }
}
