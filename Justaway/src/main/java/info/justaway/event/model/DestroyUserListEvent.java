package info.justaway.event.model;

public class DestroyUserListEvent {

    private final Long mUserListId;

    public DestroyUserListEvent(final Long userListId) {
        mUserListId = userListId;
    }

    public Long getUserListId() {
        return mUserListId;
    }
}
