package info.justaway.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.List;

import info.justaway.JustawayApplication;
import info.justaway.R;
import info.justaway.adapter.TwitterAdapter;
import info.justaway.model.Row;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;

/**
 * 会話を表示
 *
 * @author aska
 */
public class AroundFragment extends DialogFragment {

    private ProgressBar mProgressBarTop;
    private ProgressBar mProgressBarBottom;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Activity activity = getActivity();
        Dialog dialog = new Dialog(activity);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        dialog.setContentView(R.layout.fragment_around);

        ListView listView = (ListView) dialog.findViewById(R.id.list);
        mProgressBarTop = (ProgressBar) dialog.findViewById(R.id.guruguru_top);
        mProgressBarBottom = (ProgressBar) dialog.findViewById(R.id.guruguru_bottom);

        // コンテキストメニューを使える様にする為の指定、但しデフォルトではロングタップで開く
        registerForContextMenu(listView);

        // Status(ツイート)をViewに描写するアダプター
        TwitterAdapter adapter = new TwitterAdapter(activity, R.layout.row_tweet);
        listView.setAdapter(adapter);

        // シングルタップでコンテキストメニューを開くための指定
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.showContextMenu();
            }
        });

        Long statusId = getArguments().getLong("statusId");
        if (statusId > 0) {
            new BeforeStatusTask(adapter).execute(statusId);
        }

        return dialog;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        JustawayApplication.getApplication().onCreateContextMenu(getActivity(), menu, view, menuInfo);

        // DialogFragment内でContextMenuを使うにはこれが必要
        MenuItem.OnMenuItemClickListener listener = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                JustawayApplication.getApplication().onContextItemSelected(item);
                return true;
            }
        };

        for (int i = 0, n = menu.size(); i < n; i++)
            menu.getItem(i).setOnMenuItemClickListener(listener);
    }

    private class BeforeStatusTask extends AsyncTask<Long, Void, ResponseList<Status>> {

        private TwitterAdapter adapter;

        public BeforeStatusTask(TwitterAdapter adapter) {
            super();
            this.adapter = adapter;
        }

        @Override
        protected ResponseList<twitter4j.Status> doInBackground(Long... params) {
            try {
                Twitter twitter = JustawayApplication.getApplication().getTwitter();
                twitter4j.Status status = twitter.showStatus(params[0]);
                Paging paging = new Paging();
                paging.setCount(4);
                paging.setMaxId(params[0]);
                return twitter.getUserTimeline(status.getUser().getScreenName(), paging);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ResponseList<twitter4j.Status> statuses) {
            mProgressBarBottom.setVisibility(View.GONE);
            if (statuses != null) {
                for (twitter4j.Status status : statuses) {
                    adapter.add(Row.newStatus(status));
                }
                adapter.notifyDataSetChanged();
                new AfterStatusTask(adapter).execute(statuses.get(0));
            } else {
                JustawayApplication.showToast(R.string.toast_load_data_failure);
            }
        }
    }

    private class AfterStatusTask extends AsyncTask<twitter4j.Status, Void, List<Status>> {

        private TwitterAdapter adapter;

        public AfterStatusTask(TwitterAdapter adapter) {
            super();
            this.adapter = adapter;
        }

        @Override
        protected List<twitter4j.Status> doInBackground(twitter4j.Status... params) {
            try {
                Twitter twitter = JustawayApplication.getApplication().getTwitter();
                twitter4j.Status status = params[0];
                Paging paging = new Paging();
                paging.setCount(200);
                paging.setSinceId(status.getId() - 1);
                for (int page = 1; page < 5; page++) {
                    paging.setPage(page);
                    ResponseList<twitter4j.Status> statuses = twitter.getUserTimeline(status.getUser().getScreenName(), paging);
                    int index = 0;
                    for (twitter4j.Status row : statuses) {
                        if (row.getId() == status.getId()) {
                            if (index > 0) {
                                return statuses.subList(Math.max(0, index - 3), index);
                            }
                        }
                        index++;
                    }
                }
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<twitter4j.Status> statuses) {
            mProgressBarTop.setVisibility(View.GONE);
            if (statuses != null) {
                for (twitter4j.Status status : statuses) {
                    adapter.insert(Row.newStatus(status), 0);
                }
                adapter.notifyDataSetChanged();
            } else {
                JustawayApplication.showToast(R.string.toast_load_data_failure);
            }
        }
    }
}
