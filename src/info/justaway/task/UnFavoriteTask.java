package info.justaway.task;

import android.os.AsyncTask;

import info.justaway.JustawayApplication;
import info.justaway.R;
import twitter4j.TwitterException;

public class UnFavoriteTask extends AsyncTask<Long, Void, TwitterException> {

    @Override
    protected TwitterException doInBackground(Long... params) {
        JustawayApplication application = JustawayApplication.getApplication();
        try {
            application.getTwitter().destroyFavorite(params[0]);
        } catch (TwitterException e) {
            if (e.getErrorCode() == 34) {
                application.removeFav(params[0]);
            }
            return e;
        }
        application.removeFav(params[0]);
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
