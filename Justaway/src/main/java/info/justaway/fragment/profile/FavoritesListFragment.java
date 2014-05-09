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

import de.greenrobot.event.EventBus;
import info.justaway.R;
import info.justaway.adapter.TwitterAdapter;
import info.justaway.event.model.StreamingDestroyStatusEvent;
import info.justaway.event.action.StatusActionEvent;
import info.justaway.listener.StatusClickListener;
import info.justaway.listener.StatusLongClickListener;
import info.justaway.model.Row;
import info.justaway.model.TwitterManager;
import info.justaway.settings.BasicSettings;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.User;

public class FavoritesListFragment extends Fragment {

    private TwitterAdapter mAdapter;
    private ListView mListView;
    private ProgressBar mFooter;
    private Boolean mAutoLoader = false;
    private long mMaxId = 0L;
    private User mUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.list_guruguru, container, false);
        if (v == null) {
            return null;
        }

        mUser = (User) getArguments().getSerializable("user");

        // リストビューの設定
        mListView = (ListView) v.findViewById(R.id.list_view);
        mListView.setVisibility(View.GONE);

        mFooter = (ProgressBar) v.findViewById(R.id.guruguru);

        // Status(ツイート)をViewに描写するアダプター
        mAdapter = new TwitterAdapter(getActivity(), R.layout.row_tweet);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new StatusClickListener(getActivity()));

        mListView.setOnItemLongClickListener(new StatusLongClickListener(mAdapter, getActivity()));

        new FavoritesListTask().execute(mUser.getScreenName());

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
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(StatusActionEvent event) {
        mAdapter.notifyDataSetChanged();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(StreamingDestroyStatusEvent event) {
        mAdapter.removeStatus(event.getStatusId());
    }

    private void additionalReading() {
        if (!mAutoLoader) {
            return;
        }
        mFooter.setVisibility(View.VISIBLE);
        mAutoLoader = false;
        new FavoritesListTask().execute(mUser.getScreenName());
    }

    private class FavoritesListTask extends AsyncTask<String, Void, ResponseList<Status>> {
        @Override
        protected ResponseList<twitter4j.Status> doInBackground(String... params) {
            try {
                Paging paging = new Paging();
                if (mMaxId > 0) {
                    paging.setMaxId(mMaxId - 1);
                    paging.setCount(BasicSettings.getPageCount());
                }
                return TwitterManager.getTwitter().getFavorites(params[0], paging);
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

            for (twitter4j.Status status : statuses) {
                if (mMaxId == 0L || mMaxId > status.getId()) {
                    mMaxId = status.getId();
                }
                mAdapter.add(Row.newStatus(status));
            }
            mAutoLoader = true;
            mListView.setVisibility(View.VISIBLE);
        }
    }
}
