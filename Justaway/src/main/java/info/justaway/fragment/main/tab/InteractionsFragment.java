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
import info.justaway.event.model.CreateFavoriteEvent;
import info.justaway.event.model.CreateStatusEvent;
import info.justaway.model.Row;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.UserMentionEntity;

/**
 * 将来「つながり」タブ予定のタブ、現在はリプしか表示されない
 */
public class InteractionsFragment extends BaseFragment {

    private Boolean mAutoLoader = false;
    private Boolean mReload = false;
    private long mMaxId = 0L;
    private ProgressBar mFooter;

    public long getTabId() {
        return -2L;
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
            new MentionsTimelineTask().execute();
        }
    }

    @Override
    public void reload() {
        mReload = true;
        clear();
        getPullToRefreshLayout().setRefreshing(true);
        new MentionsTimelineTask().execute();
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
        new MentionsTimelineTask().execute();
    }

    @Override
    protected boolean skip(Row row) {
        if (row.isFavorite()) {
            return false;
        }
        if (row.isStatus()) {

            JustawayApplication application = JustawayApplication.getApplication();

            Status status = row.getStatus();

            long userId = application.getUserId();
            Status retweet = status.getRetweetedStatus();
            if (retweet != null) {

                // retweeted for me
                if (retweet.getUser().getId() == userId) {
                    return false;
                }
            } else {

                /**
                 * 自分を@に含むRTが通知欄を破壊するのを防ぐ為、mentioned判定は非RT時のみ行う
                 */

                // mentioned for me
                if (status.getInReplyToUserId() == userId) {
                    return false;
                }

                // mentioned for me
                UserMentionEntity[] mentions = status.getUserMentionEntities();
                for (UserMentionEntity mention : mentions) {
                    if (mention.getId() == userId) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public void onEventMainThread(CreateStatusEvent event) {
        add(event.getRow());
    }

    public void onEventMainThread(CreateFavoriteEvent event) {
        add(event.getRow());
    }

    private class MentionsTimelineTask extends AsyncTask<Void, Void, ResponseList<Status>> {
        @Override
        protected ResponseList<twitter4j.Status> doInBackground(Void... params) {
            try {
                JustawayApplication application = JustawayApplication.getApplication();
                Paging paging = new Paging();
                if (mMaxId > 0) {
                    paging.setMaxId(mMaxId - 1);
                    paging.setCount(application.getPageCount());
                }
                return application.getTwitter().getMentionsTimeline(paging);
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
                    adapter.extensionAdd(Row.newStatus(status));
                }
                mAutoLoader = true;
                getListView().setVisibility(View.VISIBLE);
            }
        }
    }
}
