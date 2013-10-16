package info.justaway.fragment;

import info.justaway.JustawayApplication;
import info.justaway.MainActivity;
import info.justaway.R;
import info.justaway.adapter.TwitterAdapter;
import info.justaway.model.Row;
import twitter4j.Twitter;

import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;

/**
 * 会話を表示
 * 
 * @author aska
 */
public class TalkFragment extends DialogFragment {

    private ListView listView;

    public ListView getListView() {
        return listView;
    }

    public void setListView(ListView listView) {
        this.listView = listView;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        MainActivity activity = (MainActivity) getActivity();
        Dialog dialog = new Dialog(activity);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        dialog.setContentView(R.layout.fragment_talk);

        setListView((ListView) dialog.findViewById(R.id.list));

        TwitterAdapter adapter = new TwitterAdapter(activity, R.layout.row_tweet);
        getListView().setAdapter(adapter);

        Long statusId = getArguments().getLong("statusId");
        if (statusId > 0) {
            Twitter twitter = JustawayApplication.getApplication().getTwitter();
            new LoadTalk(twitter, adapter).execute(statusId);
        } else {
            JustawayApplication.showToast("statusIdがありません");
        }

        return dialog;
    }

    private class LoadTalk extends AsyncTask<Long, Void, twitter4j.Status> {

        private Twitter twitter;
        private TwitterAdapter adapter;

        public LoadTalk(Twitter twitter, TwitterAdapter adapter) {
            super();
            this.twitter = twitter;
            this.adapter = adapter;
        }

        @Override
        protected twitter4j.Status doInBackground(Long... params) {
            try {
                return twitter.showStatus(params[0]);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(twitter4j.Status status) {
            if (status != null) {
                adapter.add(Row.newStatus(status));
                adapter.notifyDataSetChanged();
                Long inReplyToStatusId = status.getInReplyToStatusId();
                if (inReplyToStatusId > 0) {
                    new LoadTalk(twitter, adapter).execute(inReplyToStatusId);
                }
            }
        }
    }
}
