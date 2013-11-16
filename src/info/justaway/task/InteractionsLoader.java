package info.justaway.task;

import info.justaway.JustawayApplication;

import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import android.content.Context;

public class InteractionsLoader extends AbstractAsyncTaskLoader<ResponseList<Status>> {

    public InteractionsLoader(Context context) {
        super(context);
    }

    @Override
    public ResponseList<Status> loadInBackground() {
        try {
            Twitter twitter = JustawayApplication.getApplication().getTwitter();
            return twitter.getMentionsTimeline();
        } catch (TwitterException e) {
            e.printStackTrace();
            return null;
        }
    }
}
