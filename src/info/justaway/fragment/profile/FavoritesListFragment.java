package info.justaway.fragment.profile;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import info.justaway.JustawayApplication;
import info.justaway.R;
import info.justaway.adapter.TwitterAdapter;
import info.justaway.fragment.dialog.StatusMenuFragment;
import info.justaway.model.Row;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.User;

/**
 * Created by teshi on 2013/12/21.
 */
public class FavoritesListFragment extends Fragment {

    private TwitterAdapter mAdapter;
    private ListView mListView;
    private ProgressBar mFooter;
    private Boolean mAutoLoader = false;
    private int mPage = 1;
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

        // シングルタップでコンテキストメニューを開くための指定
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                StatusMenuFragment statusMenuFragment = new StatusMenuFragment();
                Bundle args = new Bundle();
                Row row = mAdapter.getItem(position);
                args.putSerializable("status", row.getStatus());
                statusMenuFragment.setArguments(args);
                statusMenuFragment.setCallback(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                });
                statusMenuFragment.show(getActivity().getSupportFragmentManager(), "dialog");
            }
        });
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
                ResponseList<twitter4j.Status> statuses = JustawayApplication.getApplication().getTwitter().getFavorites(params[0], new Paging(mPage));
                return statuses;
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
                mAdapter.add(Row.newStatus(status));
            }
            mPage++;
            mAutoLoader = true;
            mListView.setVisibility(View.VISIBLE);
        }
    }
}
