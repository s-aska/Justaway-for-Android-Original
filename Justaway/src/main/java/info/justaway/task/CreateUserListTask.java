package info.justaway.task;

import android.os.AsyncTask;

import info.justaway.model.TwitterManager;
import twitter4j.TwitterException;


public class CreateUserListTask extends AsyncTask<Void, Void, TwitterException> {

    private String mListName;
    private boolean mPrivacy;
    private String mListDescription;

    public CreateUserListTask(String listName, boolean privacy, String listDescription) {
        mListName = listName;
        mPrivacy = privacy;
        mListDescription = listDescription;
    }

    @Override
    protected TwitterException doInBackground(Void... params) {
        try {
            TwitterManager.getTwitter().createUserList(mListName, mPrivacy, mListDescription);
            return null;
        } catch (TwitterException e) {
            e.printStackTrace();
            return e;
        }
    }
}