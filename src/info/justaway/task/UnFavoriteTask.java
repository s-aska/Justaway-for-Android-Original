package info.justaway.task;

import android.os.AsyncTask;

import info.justaway.JustawayApplication;
import info.justaway.R;
import twitter4j.TwitterException;

public class UnFavoriteTask extends AsyncTask<Void, Void, TwitterException> {

    private long mId;
    private JustawayApplication mApplication;

    public UnFavoriteTask(long id) {
        mId = id;
        mApplication = JustawayApplication.getApplication();

        /**
         * 先にremoveFavしておかないとViewの星が戻ってしまう、
         * 重複エラー以外の理由で失敗し場合（通信エラー等）は戻す
         */
        mApplication.removeFav(mId);
    }

    @Override
    protected TwitterException doInBackground(Void... params) {
        try {
            mApplication.getTwitter().destroyFavorite(mId);
        } catch (TwitterException e) {
            if (e.getErrorCode() != 34) {
                mApplication.setFav(mId);
            }
            return e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(TwitterException e) {
        if (e == null) {
            JustawayApplication.showToast(R.string.toast_destroy_favorite_success);
        } else if (e.getErrorCode() == 34) {
            JustawayApplication.showToast(R.string.toast_destroy_favorite_already);
        } else {
            JustawayApplication.showToast(R.string.toast_destroy_favorite_failure);
        }
    }
}
