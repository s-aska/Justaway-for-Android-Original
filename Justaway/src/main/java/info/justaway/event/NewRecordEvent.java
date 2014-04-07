package info.justaway.event;

public class NewRecordEvent {
    private final long mTabId;
    private final boolean mAutoScroll;

    public NewRecordEvent(final long tabId, final boolean autoScroll) {
        mTabId = tabId;
        mAutoScroll = autoScroll;
    }

    public long getTabId() {
        return mTabId;
    }
    public boolean getAutoScroll() {
        return mAutoScroll;
    }
}
