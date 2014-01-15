package info.justaway.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import info.justaway.JustawayApplication;
import info.justaway.MainActivity;
import info.justaway.R;
import info.justaway.adapter.TwitterAdapter;
import info.justaway.model.Row;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;

/**
 * 将来「つながり」タブ予定のタブ、現在はリプしか表示されない
 */
public class InteractionsFragment extends BaseFragment {

    private Boolean mAutoLoader = false;
    private Boolean mReload = false;
    private long mMaxId = 0L;
    private ProgressBar mFooter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        mFooter = (ProgressBar) v.findViewById(R.id.guruguru);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
        PullToRefreshListView pullToRefreshListView = getPullToRefreshListView();
        pullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                mReload = true;
                mMaxId = 0L;
                new MentionsTimelineTask().execute();
            }
        });

        new MentionsTimelineTask().execute();
    }

    private void additionalReading() {
        if (!mAutoLoader || mReload) {
            return;
        }
        mFooter.setVisibility(View.VISIBLE);
        mAutoLoader = false;
        new MentionsTimelineTask().execute();
    }

    private Boolean skip(Row row) {
        if (row.isFavorite()) {
            return false;
        }
        if (row.isStatus()) {
            JustawayApplication application = JustawayApplication.getApplication();
            Status status = row.getStatus();
            // mentioned for me
            if (status.getInReplyToUserId() == application.getUserId()) {
                return false;
            }
            // retweeted for me
            Status retweet = status.getRetweetedStatus();
            if (retweet != null && retweet.getUser().getId() == application.getUserId()) {
                return false;
            }
        }
        return true;
    }

    /**
     * ページ最上部だと自動的に読み込まれ、スクロールしていると動かないという美しい挙動
     */
    public void add(final Row row) {
        final ListView listView = getListView();
        if (listView == null) {
            return;
        }

        if (skip(row)) {
            return;
        }

        listView.post(new Runnable() {
            @Override
            public void run() {

                // 表示している要素の位置
                int position = listView.getFirstVisiblePosition() -1;

                // 縦スクロール位置
                View view = listView.getChildAt(0);
                int y = view != null ? view.getTop() : 0;

                // 要素を上に追加（ addだと下に追加されてしまう ）
                HeaderViewListAdapter headerViewListAdapter = (HeaderViewListAdapter) listView.getAdapter();
                TwitterAdapter adapter = (TwitterAdapter) headerViewListAdapter.getWrappedAdapter();
                adapter.insert(row, 0);

                // 少しでもスクロールさせている時は画面を動かさない様にスクロー位置を復元する
                MainActivity activity = (MainActivity) getActivity();
                if (activity == null) {
                    return;
                }
                if (position != 0 || y != 0) {
                    listView.setSelectionFromTop(position + 2, y);
                    activity.onNewInteractions(false);
                } else {
                    activity.onNewInteractions(true);
                }
            }
        });
    }

    private class MentionsTimelineTask extends AsyncTask<Void, Void, ResponseList<Status>> {
        @Override
        protected ResponseList<twitter4j.Status> doInBackground(Void... params) {
            try {
                Paging paging = new Paging();
                if (mMaxId > 0) {
                    paging.setMaxId(mMaxId - 1);
                }
                return JustawayApplication.getApplication().getTwitter().getMentionsTimeline(paging);
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
                com.handmark.pulltorefresh.library.PullToRefreshListView pullToRefreshListView = getPullToRefreshListView();
                pullToRefreshListView.onRefreshComplete();
                return;
            }
            for (twitter4j.Status status : statuses) {
                if (mMaxId == 0L || mMaxId > status.getId()) {
                    mMaxId = status.getId();
                }
                adapter.extensionAdd(Row.newStatus(status));
            }
            mAutoLoader = true;
            getListView().setVisibility(View.VISIBLE);
        }
    }
}
