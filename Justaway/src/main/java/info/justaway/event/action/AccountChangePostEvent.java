package info.justaway.event.action;

public class AccountChangePostEvent {
    private final long mTabId;

    public AccountChangePostEvent(long tabId) {
        mTabId = tabId;
    }

    public long getTabId() {
        return mTabId;
    }
}
