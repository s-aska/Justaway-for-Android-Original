package info.justaway.task;

import android.os.AsyncTask;

import de.greenrobot.event.EventBus;
import info.justaway.R;
import info.justaway.event.model.StreamingDestroyStatusEvent;
import info.justaway.model.TwitterManager;
import info.justaway.util.MessageUtil;

public class DestroyStatusTask extends AsyncTask<Long, Void, Boolean> {

    private long mStatusId;

    public DestroyStatusTask(long statusId) {
        this.mStatusId = statusId;
    }

    @Override
    protected Boolean doInBackground(Long... params) {
        try {
            TwitterManager.getTwitter().destroyStatus(mStatusId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            MessageUtil.showToast(R.string.toast_destroy_status_success);
            EventBus.getDefault().post(new StreamingDestroyStatusEvent(mStatusId));
        } else {
            MessageUtil.showToast(R.string.toast_destroy_status_failure);
        }
    }
}