package info.justaway.task;

import info.justaway.JustawayApplication;
import info.justaway.R;

import android.os.AsyncTask;

public class DestroyDirectMessageTask extends AsyncTask<Long, Void, Boolean> {

    @Override
    protected Boolean doInBackground(Long... params) {
        try {
            JustawayApplication.getApplication().getTwitter().destroyDirectMessage(params[0]);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            JustawayApplication.showToast(R.string.toast_destroy_direct_message_success);
        } else {
            JustawayApplication.showToast(R.string.toast_destroy_direct_message_failure);
        }
    }
}
