package info.justaway.task;

import info.justaway.JustawayApplication;

import android.os.AsyncTask;

public class UnRetweetTask extends AsyncTask<Long, Void, twitter4j.Status> {

    @Override
    protected twitter4j.Status doInBackground(Long... params) {
        try {
            return JustawayApplication.getApplication().getTwitter().destroyStatus(params[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(twitter4j.Status status) {
        if (status != null) {
            JustawayApplication.showToast("公式RT解除に成功しました>゜))彡");
        } else {
            JustawayApplication.showToast("公式RT解除に失敗しました＞＜");
        }
    }
}