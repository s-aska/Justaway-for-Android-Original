package info.justaway.task;

import info.justaway.JustawayApplication;
import android.os.AsyncTask;

public class DestroyStatusTask extends AsyncTask<Long, Void, Boolean> {

    @Override
    protected Boolean doInBackground(Long... params) {
        try {
            JustawayApplication.getApplication().getTwitter().destroyStatus(params[0]);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success == true) {
            JustawayApplication.showToast("ツイ消しに成功しました>゜))彡");
        } else {
            JustawayApplication.showToast("ツイ消しに失敗しました＞＜");
        }
    }
}