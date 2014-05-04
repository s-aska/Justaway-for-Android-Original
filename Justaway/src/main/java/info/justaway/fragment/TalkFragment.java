package info.justaway.fragment;

import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;

import de.greenrobot.event.EventBus;
import info.justaway.JustawayApplication;
import info.justaway.R;
import info.justaway.adapter.TwitterAdapter;
import info.justaway.event.model.StreamingDestroyStatusEvent;
import info.justaway.event.action.StatusActionEvent;
import info.justaway.listener.StatusClickListener;
import info.justaway.listener.StatusLongClickListener;
import info.justaway.model.Row;
import twitter4j.Status;
import twitter4j.Twitter;

/**
 * 会話を表示
 *
 * @author aska
 */
public class TalkFragment extends DialogFragment {

    private Twitter mTwitter;
    private TwitterAdapter mAdapter;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        FragmentActivity activity = getActivity();
        Dialog dialog = new Dialog(activity);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        dialog.setContentView(R.layout.list);

        ListView listView = (ListView) dialog.findViewById(R.id.list);

        // Status(ツイート)をViewに描写するアダプター
        mAdapter = new TwitterAdapter(activity, R.layout.row_tweet);
        listView.setAdapter(mAdapter);

        // タップ操作の登録など
        listView.setOnItemClickListener(new StatusClickListener(activity));

        listView.setOnItemLongClickListener(new StatusLongClickListener(mAdapter, getActivity()));

        Status status = (Status) getArguments().getSerializable("status");
        if (status != null) {
            mTwitter = JustawayApplication.getApplication().getTwitter();
            mAdapter.add(Row.newStatus(status));
            mAdapter.notifyDataSetChanged();
            new LoadTalk().execute(status.getInReplyToStatusId());
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
    public void onEventMainThread(StreamingDestroyStatusEvent event) {
        mAdapter.removeStatus(event.getStatusId());
    }

    private class LoadTalk extends AsyncTask<Long, Void, twitter4j.Status> {

        public LoadTalk() {
            super();
        }

        @Override
        protected twitter4j.Status doInBackground(Long... params) {
            try {
                return mTwitter.showStatus(params[0]);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(twitter4j.Status status) {
            if (status != null) {
                mAdapter.add(Row.newStatus(status));
                mAdapter.notifyDataSetChanged();
                Long inReplyToStatusId = status.getInReplyToStatusId();
                if (inReplyToStatusId > 0) {
                    new LoadTalk().execute(inReplyToStatusId);
                }
            }
        }
    }
}
