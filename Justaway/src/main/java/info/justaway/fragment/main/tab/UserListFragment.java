package info.justaway.fragment.main.tab;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import info.justaway.JustawayApplication;
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
        if (mMaxId == 0L) {
            mMaxId = -1L;
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

    @Override
    protected void additionalReading() {
        if (!mAutoLoader || mReload) {
            return;
        }
        mFooter.setVisibility(View.VISIBLE);
        mAutoLoader = false;
        new UserListStatusesTask().execute();
    }

    @Override
    protected boolean skip(Row row) {
        return mMembers.get(row.getStatus().getUser().getId()) == null;
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
                    if (mMaxId <= 0L || mMaxId > status.getId()) {
                        mMaxId = status.getId();
                    }
                    adapter.add(Row.newStatus(status));
                }
                mReload = false;
                getPullToRefreshLayout().setRefreshComplete();
            } else {
                for (twitter4j.Status status : statuses) {
                    if (mMaxId <= 0L || mMaxId > status.getId()) {
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
