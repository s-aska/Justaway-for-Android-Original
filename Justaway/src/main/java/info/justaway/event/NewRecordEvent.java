package info.justaway.event;

public class NewRecordEvent {
    private final long mTabId;
    private final String mSearchWord;
    private final boolean mAutoScroll;

    public NewRecordEvent(final long tabId, final  String searchWord, final boolean autoScroll) {
        mTabId = tabId;
        mSearchWord = searchWord;
        mAutoScroll = autoScroll;
    }

    public long getTabId() {
        return mTabId;
    }
    public String getSearchWord() {
        return mSearchWord;
    }
    public boolean getAutoScroll() {
        return mAutoScroll;
    }
}
