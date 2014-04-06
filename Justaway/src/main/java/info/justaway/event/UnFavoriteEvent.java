package info.justaway.event;

import twitter4j.Status;
import twitter4j.User;

public class UnFavoriteEvent {

    private final User mUser;
    private final Status mStatus;

    public UnFavoriteEvent(final User user, final Status status) {
        mUser = user;
        mStatus = status;
    }

    public User getUser() {
        return mUser;
    }

    public Status getStatus() {
        return mStatus;
    }
}
