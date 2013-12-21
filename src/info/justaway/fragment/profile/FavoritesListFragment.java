package info.justaway.fragment.profile;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import info.justaway.JustawayApplication;
import info.justaway.R;
import info.justaway.adapter.TwitterAdapter;
import info.justaway.model.Row;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.User;

/**
 * Created by teshi on 2013/12/21.
 */
public class FavoritesListFragment extends Fragment {
    private TwitterAdapter adapter;
    private ListView listView;
    private ProgressBar mFooter;
    private Context context;
    private int currentPage = 1;
    private int nextPage = 1;
    private User user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.list, container, false);

        JustawayApplication application = JustawayApplication.getApplication();
        context = getActivity();

        user = (User) getArguments().getSerializable("user");

        // リストビューの設定
        listView = (ListView) v.findViewById(R.id.listView);

        // コンテキストメニューを使える様にする為の指定、但しデフォルトではロングタップで開く
        registerForContextMenu(listView);

        mFooter = (ProgressBar) v.findViewById(R.id.guruguru);
        mFooter.setVisibility(View.GONE);

        // Status(ツイート)をViewに描写するアダプター
        adapter = new TwitterAdapter(context, R.layout.row_tweet);
        listView.setAdapter(adapter);

        // シングルタップでコンテキストメニューを開くための指定
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.showContextMenu();
            }
        });
        new FavoritesListTask().execute(user.getScreenName().toString());

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
        return v;
    }

    private void additionalReading() {
        // 次のページあるのか確認
        if (currentPage != nextPage) {
            mFooter.setVisibility(View.VISIBLE);
            currentPage++;
            new FavoritesListTask().execute(user.getScreenName().toString());
        }
        return;
    }

    private class FavoritesListTask extends AsyncTask<String, Void, ResponseList<Status>> {
        @Override
        protected ResponseList<twitter4j.Status> doInBackground(String... params) {
            try {
                ResponseList<twitter4j.Status> statuses = JustawayApplication.getApplication().getTwitter().getFavorites(params[0], new Paging(nextPage));
                return statuses;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ResponseList<twitter4j.Status> statuses) {
            for (twitter4j.Status status : statuses) {
                adapter.add(Row.newStatus(status));
            }
            mFooter.setVisibility(View.GONE);
            nextPage++;
        }
    }
}
