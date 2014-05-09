package info.justaway.task;

import android.os.AsyncTask;

import info.justaway.model.TwitterManager;

public class DestroyBlockTask extends AsyncTask<Long, Void, Boolean> {

    @Override
    protected Boolean doInBackground(Long... params) {
        try {
            TwitterManager.getTwitter().destroyBlock(params[0]);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
