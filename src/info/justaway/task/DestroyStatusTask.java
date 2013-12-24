package info.justaway.task;

import android.os.AsyncTask;

import info.justaway.JustawayApplication;
import info.justaway.R;

public class DestroyStatusTask extends AsyncTask<Long, Void, Boolean> {

    @Override
    protected Boolean doInBackground(Long... params) {
        try {
            JustawayApplication.getApplication().getTwitter().destroyStatus(params[0]);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            JustawayApplication.showToast(R.string.toast_destroy_status_success);
        } else {
            JustawayApplication.showToast(R.string.toast_destroy_status_failure);
        }
    }
}