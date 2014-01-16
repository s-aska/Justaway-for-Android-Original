package info.justaway.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.ProgressBar;

import info.justaway.JustawayApplication;
import info.justaway.MainActivity;
import info.justaway.R;
import info.justaway.adapter.TwitterAdapter;
import info.justaway.model.Row;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.User;

public class UserListFragment extends BaseFragment {

    private Boolean mAutoLoader = false;
    private Boolean mReload = false;
    private long mMaxId = 0L;
    private ProgressBar mFooter;
    private int mUserListId;
    private LongSparseArray<Boolean> mMembers = new LongSparseArray<Boolean>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        mFooter = (ProgressBar) v.findViewById(R.id.guruguru);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mUserListId = getArguments().getInt("userListId");
        ListView listView = getListView();
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
//        PullToRefreshListView pullToRefreshListView = getPullToRefreshListView();
//        pullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
//            @Override
//            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
//                mReload = true;
//                mMaxId = 0L;
//                new UserListStatusesTask().execute();
//            }
//        });

        new UserListStatusesTask().execute();
    }

    private void additionalReading() {
        if (!mAutoLoader || mReload) {
            return;
        }
        mFooter.setVisibility(View.VISIBLE);
        mAutoLoader = false;
        new UserListStatusesTask().execute();
    }

    @Override
    public void add(final Row row) {
        final ListView listView = getListView();
        if (listView == null) {
            return;
        }

        if (mMembers.get(row.getStatus().getUser().getId()) == null) {
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
                if (activity == null) {
                    return;
                }
                if (position != 0 || y != 0) {
                    listView.setSelectionFromTop(position + 1, y);
                    activity.onNewListStatus(mUserListId, false);
                } else {
                    activity.onNewListStatus(mUserListId, true);
                }
            }
        });
    }

    @Override
    public void onRefreshStarted(View view) {

    }

    private class UserListStatusesTask extends AsyncTask<Void, Void, ResponseList<Status>> {
        @Override
        protected ResponseList<twitter4j.Status> doInBackground(Void... params) {
            try {
                Twitter twitter = JustawayApplication.getApplication().getTwitter();
                Paging paging = new Paging();
                if (mMaxId > 0) {
                    paging.setMaxId(mMaxId - 1);
                } else {
                    ResponseList<User> members = twitter.getUserListMembers(mUserListId, 0);
                    for (User user : members) {
                        mMembers.append(user.getId(), true);
                    }
                }
                return twitter.getUserListStatuses(mUserListId, paging);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ResponseList<twitter4j.Status> statuses) {
            mFooter.setVisibility(View.GONE);
            if (statuses == null || statuses.size() == 0) {
                return;
            }
            TwitterAdapter adapter = getListAdapter();
            if (mReload) {
                adapter.clear();
                for (twitter4j.Status status : statuses) {
                    if (mMaxId == 0L || mMaxId > status.getId()) {
                        mMaxId = status.getId();
                    }
                    adapter.add(Row.newStatus(status));
                }
                mReload = false;
//                com.handmark.pulltorefresh.library.PullToRefreshListView pullToRefreshListView = getPullToRefreshListView();
//                pullToRefreshListView.onRefreshComplete();
                return;
            }
            for (twitter4j.Status status : statuses) {
                if (mMaxId == 0L || mMaxId > status.getId()) {
                    mMaxId = status.getId();
                }
                adapter.extensionAdd(Row.newStatus(status));

                // 最初のツイートに登場ユーザーをStreaming APIからの取り込み対象にすることでAPI節約!!!
                mMembers.append(status.getUser().getId(), true);
            }
            mAutoLoader = true;
            getListView().setVisibility(View.VISIBLE);
        }
    }
}
