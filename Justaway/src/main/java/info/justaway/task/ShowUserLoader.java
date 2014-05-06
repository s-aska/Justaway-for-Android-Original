package info.justaway.task;

import android.content.Context;

import info.justaway.model.AccessTokenManager;
import info.justaway.model.Profile;
import info.justaway.model.TwitterManager;
import twitter4j.Relationship;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

public class ShowUserLoader extends AbstractAsyncTaskLoader<Profile> {

    private String mScreenName;
    private long mUserId;

    public ShowUserLoader(Context context, String screenName) {
        super(context);
        this.mScreenName = screenName;
    }

    public ShowUserLoader(Context context, long userId) {
        super(context);
        this.mUserId = userId;
    }

    @Override
    public Profile loadInBackground() {
        try {
            Twitter twitter = TwitterManager.getTwitter();
            User user;
            Relationship relationship;
            if (mScreenName != null) {
                user = twitter.showUser(mScreenName);
                relationship = twitter.showFriendship(AccessTokenManager.getScreenName(), mScreenName);
            } else {
                user = twitter.showUser(mUserId);
                relationship = twitter.showFriendship(AccessTokenManager.getUserId(), mUserId);
            }
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