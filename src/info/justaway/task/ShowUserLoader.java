package info.justaway.task;

import info.justaway.JustawayApplication;
import twitter4j.TwitterException;
import twitter4j.User;
import android.content.Context;

public class ShowUserLoader extends AbstractAsyncTaskLoader<User> {

    private Long userId;

    public ShowUserLoader(Context context, Long userId) {
        super(context);
        this.userId = userId;
    }

    @Override
    public User loadInBackground() {
        try {
            return JustawayApplication.getApplication().getTwitter().showUser(userId);
        } catch (TwitterException e) {
            e.printStackTrace();
            return null;
        }
    }
}