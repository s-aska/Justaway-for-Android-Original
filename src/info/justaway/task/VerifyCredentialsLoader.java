package info.justaway.task;

import info.justaway.JustawayApplication;
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
            return JustawayApplication.getApplication().getTwitter().verifyCredentials();
        } catch (TwitterException e) {
            e.printStackTrace();
            return null;
        }
    }
}