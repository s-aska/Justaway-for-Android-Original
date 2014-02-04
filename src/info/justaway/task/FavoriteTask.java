package info.justaway.task;

import android.os.AsyncTask;

import info.justaway.JustawayApplication;
import info.justaway.R;
import twitter4j.TwitterException;

public class FavoriteTask extends AsyncTask<Void, Void, TwitterException> {

    private long mId;
    private JustawayApplication mApplication;

    public FavoriteTask(long id) {
        mId = id;
        mApplication = JustawayApplication.getApplication();

        /**
         * 先にsetFavしておかないとViewの星が戻ってしまう、
         * 重複エラー以外の理由で失敗し場合（通信エラー等）は戻す
         */
        mApplication.setFav(mId);
    }

    @Override
    protected TwitterException doInBackground(Void... params) {
        try {
            mApplication.getTwitter().createFavorite(mId);
        } catch (TwitterException e) {
            if (e.getErrorCode() != 139) {
                mApplication.removeFav(mId);
            }
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
            JustawayApplication.showToast(R.string.toast_favorite_failure);
        }
    }
}
