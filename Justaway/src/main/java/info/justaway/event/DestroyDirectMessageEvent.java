package info.justaway.event;

public class DestroyDirectMessageEvent {

    private final Long mStatusId;

    public DestroyDirectMessageEvent(final Long statusId) {
        mStatusId = statusId;
    }

    public Long getStatusId() {
        return mStatusId;
    }
}
