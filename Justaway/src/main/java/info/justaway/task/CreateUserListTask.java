package info.justaway.task;

import android.os.AsyncTask;

import info.justaway.model.TwitterManager;


public class CreateUserListTask extends AsyncTask<Void, Void, Boolean> {

    private String mListName;
    private boolean mPrivacy;
    private String mListDescription;

    public CreateUserListTask(String listName, boolean privacy, String listDescription) {
        mListName = listName;
        mPrivacy = privacy;
        mListDescription = listDescription;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            TwitterManager.getTwitter().createUserList(mListName, mPrivacy, mListDescription);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}