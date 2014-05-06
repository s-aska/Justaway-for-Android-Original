package info.justaway.task;

import android.os.AsyncTask;

import info.justaway.JustawayApplication;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;

public class SendDirectMessageTask extends AsyncTask<String, Void, TwitterException> {

    private JustawayApplication mApplication;
    private AccessToken mAccessToken;

    public SendDirectMessageTask(AccessToken accessToken) {
        mAccessToken = accessToken;
        mApplication = JustawayApplication.getApplication();
    }

    @Override
    protected TwitterException doInBackground(String... params) {
        try {
            String[] s = params[0].split(" ", 3);
            if (mAccessToken == null) {
                mApplication.getTwitter().sendDirectMessage(getOrEmpty(s, 1), getOrEmpty(s, 2));
            } else {
                // ツイート画面から来たとき
                Twitter twitter = mApplication.getTwitterManager().getTwitterInstance();
                twitter.setOAuthAccessToken(mAccessToken);
                twitter.sendDirectMessage(getOrEmpty(s, 1), getOrEmpty(s, 2));
            }
        } catch (TwitterException e) {
            return e;
        }
        return null;
    }

    private static String getOrEmpty(String[] array, int index) {
        if (index <= 0 || array.length <= index) {
            return "";
        }
        return array[index];
    }
}