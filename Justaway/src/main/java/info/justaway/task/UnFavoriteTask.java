package info.justaway.task;

import android.os.AsyncTask;

import de.greenrobot.event.EventBus;
import info.justaway.JustawayApplication;
import info.justaway.R;
import info.justaway.event.action.StatusActionEvent;
import twitter4j.TwitterException;

public class UnFavoriteTask extends AsyncTask<Void, Void, TwitterException> {

    private static final int ERROR_CODE_DUPLICATE = 34;

    private long mStatusId;
    private JustawayApplication mApplication;

    public UnFavoriteTask(long statusId) {
        mStatusId = statusId;
        mApplication = JustawayApplication.getApplication();

        /**
         * 先にremoveFavしておかないとViewの星が戻ってしまう、
         * 重複エラー以外の理由で失敗し場合（通信エラー等）は戻す
         */
        mApplication.removeFav(mStatusId);
        EventBus.getDefault().post(new StatusActionEvent());
    }

    @Override
    protected TwitterException doInBackground(Void... params) {
        try {
            mApplication.getTwitter().destroyFavorite(mStatusId);
        } catch (TwitterException e) {
            return e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(TwitterException e) {
        if (e == null) {
            JustawayApplication.showToast(R.string.toast_destroy_favorite_success);
        } else if (e.getErrorCode() == ERROR_CODE_DUPLICATE) {
            JustawayApplication.showToast(R.string.toast_destroy_favorite_already);
        } else {
            mApplication.setFav(mStatusId);
            EventBus.getDefault().post(new StatusActionEvent());
            JustawayApplication.showToast(R.string.toast_destroy_favorite_failure);
        }
    }
}
