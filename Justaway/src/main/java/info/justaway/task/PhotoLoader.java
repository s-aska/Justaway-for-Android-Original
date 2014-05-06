package info.justaway.task;

import android.content.Context;

import info.justaway.model.TwitterManager;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.TwitterException;

public class PhotoLoader extends AbstractAsyncTaskLoader<String> {

    private long mStatusId;
    private int mIndex;

    public PhotoLoader(Context context, long statusId, int index) {
        super(context);
        mStatusId = statusId;
        mIndex = index;
    }

    @Override
    public String loadInBackground() {
        try {
            Status status = TwitterManager.getTwitter().showStatus(mStatusId);
            MediaEntity[] mediaEntities = status.getMediaEntities();
            if (mediaEntities.length < mIndex) {
                return null;
            }
            return mediaEntities[mIndex - 1].getMediaURL();
        } catch (TwitterException e) {
            e.printStackTrace();
            return null;
        }
    }
}