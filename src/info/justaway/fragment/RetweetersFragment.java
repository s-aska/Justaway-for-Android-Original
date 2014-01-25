package info.justaway.fragment;

import android.app.Activity;
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
import android.widget.ProgressBar;

import info.justaway.JustawayApplication;
import info.justaway.R;
import info.justaway.adapter.UserAdapter;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;

/**
 * リツイートを表示
 *
 * @author aska
 */
public class RetweetersFragment extends DialogFragment {

    private ProgressBar mProgressBar;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Activity activity = getActivity();
        Dialog dialog = new Dialog(activity);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        dialog.setContentView(R.layout.fragment_retweeters);

        ListView listView = (ListView) dialog.findViewById(R.id.list);
        mProgressBar = (ProgressBar) dialog.findViewById(R.id.guruguru);

        // コンテキストメニューを使える様にする為の指定、但しデフォルトではロングタップで開く
        registerForContextMenu(listView);

        // Status(ツイート)をViewに描写するアダプター
        UserAdapter adapter = new UserAdapter(activity, R.layout.row_user);
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
            new RetweetsTask(adapter).execute(statusId);
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

    private class RetweetsTask extends AsyncTask<Long, Void, ResponseList<Status>> {

        private UserAdapter adapter;

        public RetweetsTask(UserAdapter adapter) {
            super();
            this.adapter = adapter;
        }

        @Override
        protected ResponseList<twitter4j.Status> doInBackground(Long... params) {
            try {
                Twitter twitter = JustawayApplication.getApplication().getTwitter();
                return twitter.getRetweets(params[0]);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ResponseList<twitter4j.Status> statuses) {
            mProgressBar.setVisibility(View.GONE);
            if (statuses != null) {
                for (twitter4j.Status status : statuses) {
                    adapter.add(status.getUser());
                }
                adapter.notifyDataSetChanged();
            } else {
                JustawayApplication.showToast(R.string.toast_load_data_failure);
            }
        }
    }
}
