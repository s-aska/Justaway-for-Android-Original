package info.justaway.fragment.main.tab;

import android.os.AsyncTask;
import android.os.Bundle;
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

/**
 * タイムライン、すべての始まり
 */
public class TimelineFragment extends BaseFragment {

    private Boolean mAutoLoader = false;
    private Boolean mReload = false;
    private long mMaxId = 0L;
    private ProgressBar mFooter;

    public long getTabId() {
        return -1L;
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
        if (mMaxId == 0L) {
            mMaxId = -1L;
            new HomeTimelineTask().execute();
        }
    }

    @Override
    public void reload() {
        mReload = true;
        clear();
        getPullToRefreshLayout().setRefreshing(true);
        new HomeTimelineTask().execute();
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
        new HomeTimelineTask().execute();
    }

    @Override
    protected boolean skip(Row row) {
        Status retweet = row.getStatus().getRetweetedStatus();
        return retweet != null && retweet.getUser().getId() == JustawayApplication.getApplication().getUserId();
    }

    private class HomeTimelineTask extends AsyncTask<Void, Void, ResponseList<Status>> {
        @Override
        protected ResponseList<twitter4j.Status> doInBackground(Void... params) {
            try {
                JustawayApplication application = JustawayApplication.getApplication();
                Paging paging = new Paging();
                if (mMaxId > 0) {
                    paging.setMaxId(mMaxId - 1);
                    paging.setCount(application.getPageCount());
                }
                return application.getTwitter().getHomeTimeline(paging);
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
                    adapter.extensionAdd(Row.newStatus(status));
                }
                mAutoLoader = true;
                getListView().setVisibility(View.VISIBLE);
            }
        }
    }
}
