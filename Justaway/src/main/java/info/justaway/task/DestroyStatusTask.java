package info.justaway.task;

import android.os.AsyncTask;

import de.greenrobot.event.EventBus;
import info.justaway.JustawayApplication;
import info.justaway.R;
import info.justaway.event.DestroyStatusEvent;

public class DestroyStatusTask extends AsyncTask<Long, Void, Boolean> {

    private long mStatusId;

    public DestroyStatusTask(long statusId) {
        this.mStatusId = statusId;
    }

    @Override
    protected Boolean doInBackground(Long... params) {
        try {
            JustawayApplication.getApplication().getTwitter().destroyStatus(mStatusId);
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
            EventBus.getDefault().post(new DestroyStatusEvent(mStatusId));
        } else {
            JustawayApplication.showToast(R.string.toast_destroy_status_failure);
        }
    }
}