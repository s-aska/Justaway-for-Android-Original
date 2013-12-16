package info.justaway.task;

import info.justaway.JustawayApplication;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

import android.content.Context;

public class VerifyCredentialsLoader extends AbstractAsyncTaskLoader<User> {

    public VerifyCredentialsLoader(Context context) {
        super(context);
    }

    @Override
    public User loadInBackground() {
        try {
            JustawayApplication application = JustawayApplication.getApplication();
            Twitter twitter = application.getTwitter();
            User user = twitter.verifyCredentials();
            application.setUserId(user.getId());
            application.setScreenName(user.getScreenName());
//            ResponseList<Status> favorites = twitter.getFavorites(user.getId());
//            for (Status status : favorites) {
//                application.setFav(status.getId());
//            }
            return user;
        } catch (TwitterException e) {
            e.printStackTrace();
            return null;
        }
    }
}