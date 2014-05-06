package info.justaway.task;

import android.content.Context;

import info.justaway.model.TwitterManager;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;

public class TimelineLoader extends AbstractAsyncTaskLoader<ResponseList<Status>> {

    public TimelineLoader(Context context) {
        super(context);
    }

    @Override
    public ResponseList<Status> loadInBackground() {
        try {
            return TwitterManager.getTwitter().getHomeTimeline();
        } catch (TwitterException e) {
            e.printStackTrace();
            return null;
        }
    }
}
