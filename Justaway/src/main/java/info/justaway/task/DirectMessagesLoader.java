package info.justaway.task;

import android.content.Context;

import java.util.Collections;
import java.util.Comparator;

import info.justaway.model.TwitterManager;
import twitter4j.DirectMessage;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class DirectMessagesLoader extends AbstractAsyncTaskLoader<ResponseList<DirectMessage>> {

    public DirectMessagesLoader(Context context) {
        super(context);
    }

    @Override
    public ResponseList<DirectMessage> loadInBackground() {
        try {
            Twitter twitter = TwitterManager.getTwitter();
            // 受信したDM
            ResponseList<DirectMessage> statuses = twitter.getDirectMessages();
            // 送信したDM
            statuses.addAll(twitter.getSentDirectMessages());
            // 日付でソート
            Collections.sort(statuses, new Comparator<DirectMessage>() {

                @Override
                public int compare(DirectMessage arg0, DirectMessage arg1) {
                    return arg1.getCreatedAt().compareTo(
                            arg0.getCreatedAt());
                }
            });
            return statuses;
        } catch (TwitterException e) {
            e.printStackTrace();
            return null;
        }
    }
}
