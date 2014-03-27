package info.justaway.task;

import android.os.AsyncTask;

import info.justaway.JustawayApplication;
import info.justaway.R;
import info.justaway.listener.StatusActionListener;

public class DestroyStatusTask extends AsyncTask<Long, Void, Boolean> {

    private StatusActionListener mStatusActionListener;
    private long mStatusId;

    public DestroyStatusTask(long statusId) {
        this.mStatusId = statusId;
    }

    public DestroyStatusTask setStatusActionListener(StatusActionListener statusActionListener) {
        this.mStatusActionListener = statusActionListener;
        return this;
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
            if (mStatusActionListener != null) {
                mStatusActionListener.onRemoveStatus(mStatusId);
            }
        } else {
            JustawayApplication.showToast(R.string.toast_destroy_status_failure);
        }
    }
}