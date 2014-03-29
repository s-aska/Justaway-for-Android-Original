package info.justaway.task;

import android.os.AsyncTask;

import de.greenrobot.event.EventBus;
import info.justaway.JustawayApplication;
import info.justaway.R;
import info.justaway.event.StatusActionEvent;
import twitter4j.TwitterException;

public class FavoriteTask extends AsyncTask<Void, Void, TwitterException> {

    private long mStatusId;
    private JustawayApplication mApplication;

    public FavoriteTask(long statusId) {
        mStatusId = statusId;
        mApplication = JustawayApplication.getApplication();

        /**
         * 先にsetFavしておかないとViewの星が戻ってしまう、
         * 重複エラー以外の理由で失敗し場合（通信エラー等）は戻す
         */
        mApplication.setFav(mStatusId);
        EventBus.getDefault().post(new StatusActionEvent());
    }

    @Override
    protected TwitterException doInBackground(Void... params) {
        try {
            mApplication.getTwitter().createFavorite(mStatusId);
        } catch (TwitterException e) {
            return e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(TwitterException e) {
        if (e == null) {
            JustawayApplication.showToast(R.string.toast_favorite_success);
        } else if (e.getErrorCode() == 139) {
            JustawayApplication.showToast(R.string.toast_favorite_already);
        } else {
            mApplication.removeFav(mStatusId);
            EventBus.getDefault().post(new StatusActionEvent());
            JustawayApplication.showToast(R.string.toast_favorite_failure);
        }
    }
}
