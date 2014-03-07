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
import info.justaway.adapter.TwitterAdapter;
import info.justaway.listener.StatusActionListener;
import info.justaway.listener.StatusClickListener;
import info.justaway.listener.StatusLongClickListener;
import info.justaway.model.Row;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.User;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;


/**
 * ユーザーのタイムライン
 */
public class UserTimelineFragment extends Fragment implements
        OnRefreshListener {

    private TwitterAdapter mAdapter;
    private ListView mListView;
    private ProgressBar mFooter;
    private User mUser;
    private Boolean mAutoLoader = false;
    private Boolean mReload = false;
    private long mMaxId = 0L;
    private PullToRefreshLayout mPullToRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.pull_to_refresh_list, container, false);
        if (v == null) {
            return null;
        }

        mUser = (User) getArguments().getSerializable("user");

        mPullToRefreshLayout = (PullToRefreshLayout) v.findViewById(R.id.ptr_layout);

        // Now setup the PullToRefreshLayout
        ActionBarPullToRefresh.from(getActivity())
                .theseChildrenArePullable(R.id.list_view)
                .listener(this)
                .setup(mPullToRefreshLayout);

        // リストビューの設定
        mListView = (ListView) v.findViewById(R.id.list_view);
        mListView.setVisibility(View.GONE);

        mFooter = (ProgressBar) v.findViewById(R.id.guruguru);

        // Status(ツイート)をViewに描写するアダプター
        mAdapter = new TwitterAdapter(getActivity(), R.layout.row_tweet);
        mListView.setAdapter(mAdapter);

        // ツイートに関するアクション（ふぁぼ / RT / ツイ消し）のリスナー
        mAdapter.setStatusActionListener(new StatusActionListener(mAdapter));

        mListView.setOnItemClickListener(new StatusClickListener(getActivity()));

        mListView.setOnItemLongClickListener(new StatusLongClickListener(mAdapter, getActivity()));

        new UserTimelineTask().execute(mUser.getScreenName());

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

    @Override
    public void onRefreshStarted(View view) {
        mReload = true;
        mMaxId = 0;
        new UserTimelineTask().execute(mUser.getScreenName());
    }

    private void additionalReading() {
        if (!mAutoLoader || mReload) {
            return;
        }
        mFooter.setVisibility(View.VISIBLE);
        mAutoLoader = false;
        new UserTimelineTask().execute(mUser.getScreenName());
    }

    private class UserTimelineTask extends AsyncTask<String, Void, ResponseList<Status>> {
        @Override
        protected ResponseList<twitter4j.Status> doInBackground(String... params) {
            try {
                JustawayApplication application = JustawayApplication.getApplication();
                Paging paging = new Paging();
                if (mMaxId > 0) {
                    paging.setMaxId(mMaxId - 1);
                    paging.setCount(application.getPageCount());
                }
                return application.getTwitter().getUserTimeline(params[0], paging);
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

            if (mReload) {
                mAdapter.clear();
                for (twitter4j.Status status : statuses) {
                    if (mMaxId == 0L || mMaxId > status.getId()) {
                        mMaxId = status.getId();
                    }
                    mAdapter.add(Row.newStatus(status));
                }
                mReload = false;
                mPullToRefreshLayout.setRefreshComplete();
                return;
            }

            for (twitter4j.Status status : statuses) {
                if (mMaxId == 0L || mMaxId > status.getId()) {
                    mMaxId = status.getId();
                }
                mAdapter.add(Row.newStatus(status));
            }
            mAutoLoader = true;
            mPullToRefreshLayout.setRefreshComplete();
            mListView.setVisibility(View.VISIBLE);
        }
    }
}
