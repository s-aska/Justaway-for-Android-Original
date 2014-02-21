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

import info.justaway.JustawayApplication;
import info.justaway.R;
import info.justaway.adapter.TwitterAdapter;
import info.justaway.fragment.dialog.StatusMenuFragment;
import info.justaway.model.Row;
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

        Activity activity = getActivity();
        Dialog dialog = new Dialog(activity);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        dialog.setContentView(R.layout.list);

        ListView listView = (ListView) dialog.findViewById(R.id.list);

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

        Long statusId = getArguments().getLong("statusId");
        if (statusId > 0) {
            mTwitter = JustawayApplication.getApplication().getTwitter();
            new LoadTalk().execute(statusId);
        }

        return dialog;
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
