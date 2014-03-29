package info.justaway;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;

import de.greenrobot.event.EventBus;
import info.justaway.adapter.TwitterAdapter;
import info.justaway.event.AlertDialogEvent;
import info.justaway.event.DestroyStatusEvent;
import info.justaway.event.StatusActionEvent;
import info.justaway.listener.StatusClickListener;
import info.justaway.listener.StatusLongClickListener;
import info.justaway.model.Row;
import twitter4j.Status;
import twitter4j.Twitter;

/**
 * ツイート表示用のアクティビティ
 */
public class StatusActivity extends FragmentActivity {

    private ProgressDialog mProgressDialog;
    private TwitterAdapter mAdapter;
    private Twitter mTwitter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);

        // インテント経由での起動をサポート
        Intent intent = getIntent();
        long statusId;
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            if (uri == null || uri.getPath() == null) {
                return;
            }
            if (uri.getPath().contains("photo")) {
                Intent scaleImage = new Intent(this, ScaleImageActivity.class);
                scaleImage.putExtra("url", uri.toString());
                startActivity(scaleImage);
                finish();
                return;
            } else {
                statusId = Long.parseLong(uri.getLastPathSegment());
            }
        } else {
            statusId = intent.getLongExtra("id", -1L);
        }

        setContentView(R.layout.activity_status);

        ListView listView = (ListView) findViewById(R.id.list);

        // コンテキストメニューを使える様にする為の指定、但しデフォルトではロングタップで開く
        registerForContextMenu(listView);

        // Status(ツイート)をViewに描写するアダプター
        mAdapter = new TwitterAdapter(this, R.layout.row_tweet);
        listView.setAdapter(mAdapter);

        listView.setOnItemClickListener(new StatusClickListener(this));

        listView.setOnItemLongClickListener(new StatusLongClickListener(mAdapter, this));

        if (statusId > 0) {
            mTwitter = JustawayApplication.getApplication().getTwitter();
            showProgressDialog(getString(R.string.progress_loading));
            new LoadTask().execute(statusId);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(AlertDialogEvent event) {
        event.getDialogFragment().show(getSupportFragmentManager(), "dialog");
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(StatusActionEvent event) {
        mAdapter.notifyDataSetChanged();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(DestroyStatusEvent event) {
        mAdapter.removeStatus(event.getStatusId());
    }

    private void showProgressDialog(String message) {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(message);
        mProgressDialog.show();
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
    }

    private class LoadTask extends AsyncTask<Long, Void, Status> {

        public LoadTask() {
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
            dismissProgressDialog();
            if (status != null) {
                mAdapter.add(Row.newStatus(status));
                mAdapter.notifyDataSetChanged();
                Long inReplyToStatusId = status.getInReplyToStatusId();
                if (inReplyToStatusId > 0) {
                    new LoadTask().execute(inReplyToStatusId);
                }
            } else {
                JustawayApplication.showToast(R.string.toast_load_data_failure);
            }
        }
    }
}
