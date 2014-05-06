package info.justaway.task;

import android.os.AsyncTask;

import info.justaway.JustawayApplication;
import twitter4j.ResponseList;

public class LoadFavoritesTask extends AsyncTask<Long, Void, Boolean> {

    @Override
    protected Boolean doInBackground(Long... params) {
        try {
            JustawayApplication application = JustawayApplication.getApplication();
            ResponseList<twitter4j.Status> favorites = application.getTwitter().getFavorites(application.getUserId());
            for (twitter4j.Status status : favorites) {
                application.getFavRetweetManager().setFav(status.getId());
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}