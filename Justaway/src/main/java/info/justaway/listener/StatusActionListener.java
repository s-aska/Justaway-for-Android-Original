package info.justaway.listener;

import info.justaway.MainActivity;
import info.justaway.adapter.TwitterAdapter;

/**
 * ふぁぼ、RT、ツイ消しした時にAdapterとかMainActivityのnotifyDataSetChangedを叩きまくりたいよね
 */
public class StatusActionListener {

    private MainActivity mMainActivity;
    private TwitterAdapter mTwitterAdapter;

    public StatusActionListener(TwitterAdapter twitterAdapter) {
        mTwitterAdapter = twitterAdapter;
    }

    public StatusActionListener(MainActivity mainActivity) {
        mMainActivity = mainActivity;
    }

    public void notifyDataSetChanged() {
        if (mMainActivity != null) {
            mMainActivity.notifyDataSetChanged();
        }
        if (mTwitterAdapter != null) {
            mTwitterAdapter.notifyDataSetChanged();
        }
    }

    public void removeStatus(long statusId) {
        if (mTwitterAdapter != null) {
            mTwitterAdapter.removeStatus(statusId);
        }
        // MainActivityはStreamingAPIを受信する為処理しない
    }
}
