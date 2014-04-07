package info.justaway.task;

import android.os.AsyncTask;

import de.greenrobot.event.EventBus;
import info.justaway.JustawayApplication;
import info.justaway.R;
import info.justaway.event.action.StatusActionEvent;
import twitter4j.TwitterException;

public class RetweetTask extends AsyncTask<Void, Void, TwitterException> {

    private long mStatusId;
    private JustawayApplication mApplication;
    private static final int ERROR_CODE_DUPLICATE = 37;

    public RetweetTask(long statusId) {
        mStatusId = statusId;
        mApplication = JustawayApplication.getApplication();
        mApplication.setRtId(statusId, (long) 0);
        EventBus.getDefault().post(new StatusActionEvent());
    }

    @Override
    protected TwitterException doInBackground(Void... params) {
        try {
            twitter4j.Status status = JustawayApplication.getApplication().getTwitter().retweetStatus(mStatusId);
            mApplication.setRtId(status.getRetweetedStatus().getId(), status.getId());
            return null;
        } catch (TwitterException e) {
            mApplication.setRtId(mStatusId, null);
            e.printStackTrace();
            return e;
        }
    }

    @Override
    protected void onPostExecute(TwitterException e) {
        if (e == null) {
            JustawayApplication.showToast(R.string.toast_retweet_success);
        } else if (e.getErrorCode() == ERROR_CODE_DUPLICATE) {
            JustawayApplication.showToast(R.string.toast_retweet_already);
        } else {
            EventBus.getDefault().post(new StatusActionEvent());
            JustawayApplication.showToast(R.string.toast_retweet_failure);
        }
    }
}