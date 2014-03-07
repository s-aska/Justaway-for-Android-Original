package info.justaway.task;

import android.content.Context;

import info.justaway.JustawayApplication;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

public class VerifyCredentialsLoader extends AbstractAsyncTaskLoader<User> {

    public VerifyCredentialsLoader(Context context) {
        super(context);
    }

    @Override
    public User loadInBackground() {
        try {
            JustawayApplication application = JustawayApplication.getApplication();
            Twitter twitter = application.getTwitter();
            return twitter.verifyCredentials();
        } catch (TwitterException e) {
            e.printStackTrace();
            return null;
        }
    }
}