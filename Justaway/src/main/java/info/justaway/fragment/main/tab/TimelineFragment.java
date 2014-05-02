package info.justaway.fragment.main.tab;

import android.os.AsyncTask;
import android.view.View;

import info.justaway.JustawayApplication;
import info.justaway.model.Row;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;

/**
 * タイムライン、すべての始まり
 */
public class TimelineFragment extends BaseFragment {

    /**
     * このタブを表す固有のID、ユーザーリストで正数を使うため負数を使う
     */
    public long getTabId() {
        return -1L;
    }

    /**
     * このタブに表示するツイートの定義
     * @param row ストリーミングAPIから受け取った情報（ツイート）
     * @return trueは表示しない、falseは表示する
     */
    @Override
    protected boolean skip(Row row) {
        Status retweet = row.getStatus().getRetweetedStatus();
        return retweet != null && retweet.getUser().getId() == JustawayApplication.getApplication().getUserId();
    }

    @Override
    protected void taskExecute() {
        new HomeTimelineTask().execute();
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
                    mAdapter.extensionAdd(Row.newStatus(status));
                }
                mAutoLoader = true;
                mListView.setVisibility(View.VISIBLE);
            }
        }
    }
}
