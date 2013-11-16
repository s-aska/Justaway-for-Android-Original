package info.justaway.task;

import java.util.Collections;
import java.util.Comparator;

import info.justaway.JustawayApplication;
import twitter4j.DirectMessage;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import android.content.Context;

public class DirectMessageLoader extends AbstractAsyncTaskLoader<ResponseList<DirectMessage>> {

    public DirectMessageLoader(Context context) {
        super(context);
    }

    @Override
    public ResponseList<DirectMessage> loadInBackground() {
        try {
            Twitter twitter = JustawayApplication.getApplication().getTwitter();
            // 受信したDM
            ResponseList<DirectMessage> statuses = twitter.getDirectMessages();
            // 送信したDM
            statuses.addAll(twitter.getSentDirectMessages());
            // 日付でソート
            Collections.sort(statuses, new Comparator<DirectMessage>() {

                @Override
                public int compare(DirectMessage arg0, DirectMessage arg1) {
                    return ((DirectMessage) arg1).getCreatedAt().compareTo(
                            ((DirectMessage) arg0).getCreatedAt());
                }
            });
            return statuses;
        } catch (TwitterException e) {
            e.printStackTrace();
            return null;
        }
    }
}
