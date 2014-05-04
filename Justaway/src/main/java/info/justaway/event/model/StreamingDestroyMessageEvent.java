package info.justaway.event.model;

public class StreamingDestroyMessageEvent {

    private final Long mStatusId;

    public StreamingDestroyMessageEvent(final Long statusId) {
        mStatusId = statusId;
    }

    public Long getStatusId() {
        return mStatusId;
    }
}
