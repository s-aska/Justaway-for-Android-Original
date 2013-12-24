package info.justaway.task;

import info.justaway.JustawayApplication;
import info.justaway.model.Profile;
import twitter4j.Relationship;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

import android.content.Context;

public class ShowUserLoader extends AbstractAsyncTaskLoader<Profile> {

    private String screenName;

    public ShowUserLoader(Context context, String screenName) {
        super(context);
        this.screenName = screenName;
    }

    @Override
    public Profile loadInBackground() {
        try {
            Twitter twitter = JustawayApplication.getApplication().getTwitter();
            User user = twitter.showUser(screenName);
            Relationship relationship = twitter.showFriendship(JustawayApplication.getApplication().getScreenName(), screenName);
            Profile profile = new Profile();
            profile.setRelationship(relationship);
            profile.setUser(user);
            return profile;
        } catch (TwitterException e) {
            e.printStackTrace();
            return null;
        }
    }
}