package info.justaway.task;

import info.justaway.JustawayApplication;
import info.justaway.model.Profile;
import twitter4j.Relationship;
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
            User user = JustawayApplication.getApplication().getTwitter().showUser(screenName);
            Relationship relationship = JustawayApplication.getApplication().getTwitter().showFriendship(JustawayApplication.getApplication().getUser().getScreenName(), screenName);
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