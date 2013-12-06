package info.justaway.fragment;

import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

import info.justaway.JustawayApplication;
import info.justaway.MainActivity;
import info.justaway.R;
import info.justaway.adapter.TwitterAdapter;
import info.justaway.model.Row;
import twitter4j.Twitter;

/**
 * 会話を表示
 *
 * @author aska
 */
public class TalkFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        MainActivity activity = (MainActivity) getActivity();
        Dialog dialog = new Dialog(activity);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        dialog.setContentView(R.layout.fragment_talk);

        ListView listView = (ListView) dialog.findViewById(R.id.list);

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
            Twitter twitter = JustawayApplication.getApplication().getTwitter();
            new LoadTalk(twitter, adapter).execute(statusId);
        } else {
            JustawayApplication.showToast("statusIdがありません");
        }

        return dialog;
    }

    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        JustawayApplication application = JustawayApplication.getApplication();
        application.onCreateContextMenuForStatus(menu, view, menuInfo);

        // DialogFragment内でContextMenuを使うにはこれが必要
        MenuItem.OnMenuItemClickListener listener = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                onContextItemSelected(item);
                return true;
            }
        };

        for (int i = 0, n = menu.size(); i < n; i++)
            menu.getItem(i).setOnMenuItemClickListener(listener);
    }

    public boolean onContextItemSelected(MenuItem item) {
        JustawayApplication application = JustawayApplication.getApplication();
        return application.onContextItemSelected(getActivity(), item);
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
