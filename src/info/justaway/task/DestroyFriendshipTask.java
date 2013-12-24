package info.justaway.task;

import info.justaway.JustawayApplication;
import info.justaway.R;

import android.os.AsyncTask;

public class DestroyFriendshipTask extends AsyncTask<Long, Void, Boolean> {

    @Override
    protected Boolean doInBackground(Long... params) {
        try {
            JustawayApplication.getApplication().getTwitter().destroyFriendship(params[0]);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            JustawayApplication.showToast(R.string.toast_destroy_friendship_success);
        }
    }
}