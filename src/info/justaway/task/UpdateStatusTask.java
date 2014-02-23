package info.justaway.task;

import android.os.AsyncTask;

import info.justaway.JustawayApplication;
import info.justaway.settings.PostStockSettings;
import twitter4j.HashtagEntity;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;

public class UpdateStatusTask extends AsyncTask<StatusUpdate, Void, TwitterException> {

    private JustawayApplication mApplication;
    private AccessToken mAccessToken;

    public UpdateStatusTask(AccessToken accessToken) {
        mAccessToken = accessToken;
        mApplication = JustawayApplication.getApplication();
    }

    @Override
    protected TwitterException doInBackground(StatusUpdate... params) {
        StatusUpdate statusUpdate = params[0];
        try {
            twitter4j.Status status;
            if (mAccessToken == null) {
                status = mApplication.getTwitter().updateStatus(statusUpdate);
            } else {
                // ツイート画面から来たとき
                Twitter twitter = mApplication.getTwitterInstance();
                twitter.setOAuthAccessToken(mAccessToken);
                status = twitter.updateStatus(statusUpdate);
            }
            PostStockSettings postStockSettings = new PostStockSettings();
            for (HashtagEntity hashtagEntity : status.getHashtagEntities()) {
                postStockSettings.addHashtag("#".concat(hashtagEntity.getText()));
            }
        } catch (TwitterException e) {
            return e;
        }
        return null;
    }
}