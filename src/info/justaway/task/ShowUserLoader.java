package info.justaway.task;

import info.justaway.JustawayApplication;
import info.justaway.model.Profile;
import twitter4j.Relationship;
import twitter4j.TwitterException;
import twitter4j.User;
import android.content.Context;

public class ShowUserLoader extends AbstractAsyncTaskLoader<Profile> {

    private String userId;

    public ShowUserLoader(Context context, String userId) {
        super(context);
        this.userId = userId;
    }

    @Override
    public Profile loadInBackground() {
        try {
            User user = JustawayApplication.getApplication().getTwitter().showUser(userId);
            Relationship relationship = JustawayApplication.getApplication().getTwitter().showFriendship(JustawayApplication.getApplication().getUser().getScreenName(), userId);
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