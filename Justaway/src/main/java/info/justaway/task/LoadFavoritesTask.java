package info.justaway.task;

import android.os.AsyncTask;

import info.justaway.JustawayApplication;
import info.justaway.model.AccessTokenManager;
import info.justaway.model.FavRetweetManager;
import info.justaway.model.TwitterManager;
import twitter4j.ResponseList;

public class LoadFavoritesTask extends AsyncTask<Long, Void, Boolean> {

    @Override
    protected Boolean doInBackground(Long... params) {
        try {
            ResponseList<twitter4j.Status> favorites = TwitterManager.getTwitter().getFavorites(AccessTokenManager.getUserId());
            for (twitter4j.Status status : favorites) {
                FavRetweetManager.setFav(status.getId());
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}