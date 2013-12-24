package info.justaway.task;

import info.justaway.JustawayApplication;

import android.os.AsyncTask;

public class FollowTask extends AsyncTask<Long, Void, Boolean> {

    @Override
    protected Boolean doInBackground(Long... params) {
        try {
            JustawayApplication.getApplication().getTwitter().createFriendship(params[0]);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            JustawayApplication.showToast("フォローしました");
        }
    }
}