package info.justaway.event;

import twitter4j.Status;

public class StatusActionEvent {

    private final Status mStatus;

    public StatusActionEvent(final Status status) {
        mStatus = status;
    }

    public Status getStatus() {
        return mStatus;
    }
}
