package info.justaway.task;

import info.justaway.JustawayApplication;
import android.os.AsyncTask;
import android.util.Log;

public class RetweetTask extends AsyncTask<Long, Void, twitter4j.Status> {

    @Override
    protected twitter4j.Status doInBackground(Long... params) {
        try {
            return JustawayApplication.getApplication().getTwitter().retweetStatus(params[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(twitter4j.Status status) {
        if (status != null) {
            Log.d("Justaway", "[onPostExecute]" + status.getRetweetedStatus().getId() + " => " + status.getId());
            JustawayApplication.getApplication().setRtId(status.getRetweetedStatus().getId(), status.getId());
            JustawayApplication.showToast("RTに成功しました>゜))彡");
        } else {
            JustawayApplication.showToast("RTに失敗しました＞＜");
        }
    }
}