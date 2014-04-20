package info.justaway.fragment.main.tab;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.ProgressBar;

import de.greenrobot.event.EventBus;
import info.justaway.JustawayApplication;
import info.justaway.R;
import info.justaway.adapter.TwitterAdapter;
import info.justaway.event.NewRecordEvent;
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
    private long mUserListId;
    private LongSparseArray<Boolean> mMembers = new LongSparseArray<Boolean>();

    public long getTabId() {
        return mUserListId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        mFooter = (ProgressBar) v.findViewById(R.id.guruguru);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mUserListId = getArguments().getLong("userListId");
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

        if (mMaxId == 0L) {
            new UserListStatusesTask().execute();
        }
    }

    @Override
    public void reload() {
        mReload = true;
        clear();
        getPullToRefreshLayout().setRefreshing(true);
        new UserListStatusesTask().execute();
    }

    @Override
    public void clear() {
        mMaxId = 0L;
        TwitterAdapter adapter = getListAdapter();
        if (adapter != null) {
            adapter.clear();
        }
    }

    @Override
    public void onRefreshStarted(View view) {
        reload();
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
                if (position != 0 || y != 0) {
                    listView.setSelectionFromTop(position + 1, y);
                    EventBus.getDefault().post(new NewRecordEvent(mUserListId, false));
                } else {
                    EventBus.getDefault().post(new NewRecordEvent(mUserListId, true));
                }
            }
        });
    }

    private class UserListStatusesTask extends AsyncTask<Void, Void, ResponseList<Status>> {
        @Override
        protected ResponseList<twitter4j.Status> doInBackground(Void... params) {
            try {
                JustawayApplication application = JustawayApplication.getApplication();
                Twitter twitter = application.getTwitter();
                Paging paging = new Paging();
                if (mMaxId > 0) {
                    paging.setMaxId(mMaxId - 1);
                    paging.setCount(application.getPageCount());
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
                mReload = false;
                getPullToRefreshLayout().setRefreshComplete();
                getListView().setVisibility(View.VISIBLE);
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
                getPullToRefreshLayout().setRefreshComplete();
            } else {
                for (twitter4j.Status status : statuses) {
                    if (mMaxId == 0L || mMaxId > status.getId()) {
                        mMaxId = status.getId();
                    }

                    // 最初のツイートに登場ユーザーをStreaming APIからの取り込み対象にすることでAPI節約!!!
                    mMembers.append(status.getUser().getId(), true);

                    adapter.extensionAdd(Row.newStatus(status));
                }
                mAutoLoader = true;
                getListView().setVisibility(View.VISIBLE);
            }
        }
    }
}
