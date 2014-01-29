package info.justaway.task;

import android.os.AsyncTask;

import info.justaway.JustawayApplication;

public class DestroyBlockTask extends AsyncTask<Long, Void, Boolean> {

    @Override
    protected Boolean doInBackground(Long... params) {
        try {
            JustawayApplication.getApplication().getTwitter().destroyBlock(params[0]);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
