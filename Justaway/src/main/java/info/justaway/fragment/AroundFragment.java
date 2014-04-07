package info.justaway.fragment;

import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.List;

import de.greenrobot.event.EventBus;
import info.justaway.JustawayApplication;
import info.justaway.R;
import info.justaway.adapter.TwitterAdapter;
import info.justaway.event.model.DestroyStatusEvent;
import info.justaway.event.action.StatusActionEvent;
import info.justaway.listener.StatusClickListener;
import info.justaway.listener.StatusLongClickListener;
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

        FragmentActivity activity = getActivity();
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

        listView.setOnItemClickListener(new StatusClickListener(activity));

        listView.setOnItemLongClickListener(new StatusLongClickListener(mAdapter, getActivity()));

        Status status = (Status) getArguments().getSerializable("status");
        if (status != null) {
            mAdapter.add(Row.newStatus(status));
            new BeforeStatusTask().execute(status);
        }

        return dialog;
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
    public void onEventMainThread(DestroyStatusEvent event) {
        mAdapter.removeStatus(event.getStatusId());
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
