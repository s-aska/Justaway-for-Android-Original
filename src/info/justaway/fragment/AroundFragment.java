package info.justaway.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
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
import info.justaway.fragment.dialog.StatusMenuFragment;
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
    private TwitterAdapter mAdapter;

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
        mAdapter = new TwitterAdapter(activity, R.layout.row_tweet);
        listView.setAdapter(mAdapter);

        // シングルタップでコンテキストメニューを開くための指定
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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

        Status status = (Status) getArguments().getSerializable("status");
        if (status != null) {
            mAdapter.add(Row.newStatus(status));
            new BeforeStatusTask().execute(status);
        }

        return dialog;
    }

    private class BeforeStatusTask extends AsyncTask<Status, Void, ResponseList<Status>> {

        public BeforeStatusTask() {
            super();
        }

        @Override
        protected ResponseList<twitter4j.Status> doInBackground(twitter4j.Status... params) {
            try {
                Twitter twitter = JustawayApplication.getApplication().getTwitter();
                twitter4j.Status status = params[0];
                Paging paging = new Paging();
                paging.setCount(3);
                paging.setMaxId(status.getId() - 1);
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
                if (statuses.size() > 0) {
                    for (twitter4j.Status status : statuses) {
                        mAdapter.add(Row.newStatus(status));
                    }
                    mAdapter.notifyDataSetChanged();
                    new AfterStatusTask().execute(statuses.get(0));
                }
            } else {
                JustawayApplication.showToast(R.string.toast_load_data_failure);
            }
        }
    }

    private class AfterStatusTask extends AsyncTask<twitter4j.Status, Void, List<Status>> {

        public AfterStatusTask() {
            super();
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
                                return statuses.subList(Math.max(0, index - 4), index - 1);
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
                int i = 0;
                for (twitter4j.Status status : statuses) {
                    mAdapter.insert(Row.newStatus(status), i);
                    i++;
                }
                mAdapter.notifyDataSetChanged();
            }
        }
    }
}
