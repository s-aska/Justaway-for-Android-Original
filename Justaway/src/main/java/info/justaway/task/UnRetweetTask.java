package info.justaway.task;

import android.os.AsyncTask;

import de.greenrobot.event.EventBus;
import info.justaway.JustawayApplication;
import info.justaway.R;
import info.justaway.event.action.StatusActionEvent;
import twitter4j.TwitterException;

public class UnRetweetTask extends AsyncTask<Void, Void, TwitterException> {

    private long mRetweetedStatusId;
    private long mStatusId;
    private JustawayApplication mApplication;
    private static final int ERROR_CODE_DUPLICATE = 34;

    public UnRetweetTask(long retweetedStatusId, long statusId) {
        mRetweetedStatusId = retweetedStatusId;
        mStatusId = statusId;
        mApplication = JustawayApplication.getApplication();
        if (mRetweetedStatusId > 0) {
            mApplication.getFavRetweetManager().setRtId(mRetweetedStatusId, null);
            EventBus.getDefault().post(new StatusActionEvent());
        }
    }

    @Override
    protected TwitterException doInBackground(Void... params) {
        try {
            JustawayApplication.getApplication().getTwitter().destroyStatus(mStatusId);
            return null;
        } catch (TwitterException e) {
            e.printStackTrace();
            return e;
        }
    }

    @Override
    protected void onPostExecute(TwitterException e) {
        if (e == null) {
            JustawayApplication.showToast(R.string.toast_destroy_retweet_success);
        } else if (e.getErrorCode() == ERROR_CODE_DUPLICATE) {
            JustawayApplication.showToast(R.string.toast_destroy_retweet_already);
        } else {
            if (mRetweetedStatusId > 0) {
                mApplication.getFavRetweetManager().setRtId(mRetweetedStatusId, mStatusId);
                EventBus.getDefault().post(new StatusActionEvent());
            }
            JustawayApplication.showToast(R.string.toast_destroy_retweet_failure);
        }
    }
}