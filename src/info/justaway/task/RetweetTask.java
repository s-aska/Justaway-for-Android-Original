package info.justaway.task;

import android.os.AsyncTask;

import info.justaway.JustawayApplication;
import info.justaway.R;

public class RetweetTask extends AsyncTask<Long, Void, twitter4j.Status> {

    @Override
    protected twitter4j.Status doInBackground(Long... params) {
        try {
            return JustawayApplication.getApplication().getTwitter().retweetStatus(params[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(twitter4j.Status status) {
        if (status != null) {
            JustawayApplication.getApplication().setRtId(status.getRetweetedStatus().getId(), status.getId());
            JustawayApplication.showToast(R.string.toast_retweet_success);
        } else {
            JustawayApplication.showToast(R.string.toast_retweet_failure);
        }
    }
}