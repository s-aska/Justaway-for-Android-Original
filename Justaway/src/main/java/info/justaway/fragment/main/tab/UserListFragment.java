package info.justaway.fragment.main.tab;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.util.LongSparseArray;
import android.view.View;

import info.justaway.JustawayApplication;
import info.justaway.model.Row;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.User;

public class UserListFragment extends BaseFragment {

    /**
     * このタブを表す固有のID
     */
    private long mUserListId = 0L;

    private LongSparseArray<Boolean> mMembers = new LongSparseArray<Boolean>();

    public long getTabId() {
        return mUserListId;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (mUserListId == 0L) {
            mUserListId = getArguments().getLong("userListId");
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected boolean isSkip(Row row) {
        return mMembers.get(row.getStatus().getUser().getId()) == null;
    }

    @Override
    protected void taskExecute() {
        new UserListStatusesTask().execute();
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
                    paging.setCount(application.getBasicSettings().getPageCount());
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
                mReloading = false;
                mPullToRefreshLayout.setRefreshComplete();
                mListView.setVisibility(View.VISIBLE);
                return;
            }
            if (mReloading) {
                mAdapter.clear();
                for (twitter4j.Status status : statuses) {
                    if (mMaxId <= 0L || mMaxId > status.getId()) {
                        mMaxId = status.getId();
                    }
                    mAdapter.add(Row.newStatus(status));
                }
                mReloading = false;
                mPullToRefreshLayout.setRefreshComplete();
            } else {
                for (twitter4j.Status status : statuses) {
                    if (mMaxId <= 0L || mMaxId > status.getId()) {
                        mMaxId = status.getId();
                    }

                    // 最初のツイートに登場ユーザーをStreaming APIからの取り込み対象にすることでAPI節約!!!
                    mMembers.append(status.getUser().getId(), true);

                    mAdapter.extensionAdd(Row.newStatus(status));
                }
                mAutoLoader = true;
                mListView.setVisibility(View.VISIBLE);
            }
        }
    }
}
