package info.justaway;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import info.justaway.adapter.FriendListAdapter;
import twitter4j.PagableResponseList;
import twitter4j.User;

public class UserListActivity extends FragmentActivity {

    private FriendListAdapter mAdapter;
    private int mListId;
    private long mCursor = -1;
    private ListView mListView;
    private ProgressBar mFooter;
    private Boolean mAutoLoader = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        Context mContext = this;

        mFooter = (ProgressBar) findViewById(R.id.guruguru);
        mFooter.setVisibility(View.GONE);
        mListView = (ListView) findViewById(R.id.list_view);
        mListView.setVisibility(View.GONE);

        // コンテキストメニューを使える様にする為の指定、但しデフォルトではロングタップで開く
        registerForContextMenu(mListView);

        // Status(ツイート)をViewに描写するアダプター
        mAdapter = new FriendListAdapter(mContext, R.layout.row_user);
        mListView.setAdapter(mAdapter);

        // シングルタップでコンテキストメニューを開くための指定
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.showContextMenu();
            }
        });

        Intent intent = getIntent();
        mListId = intent.getIntExtra("listId", 0);

        new UserListMembersTask().execute(mListId);

        setTitle(intent.getStringExtra("listName"));

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
