package info.justaway.task;

import info.justaway.JustawayApplication;

import android.os.AsyncTask;

public class DestroyDirectMessageTask extends AsyncTask<Long, Void, Boolean> {

    @Override
    protected Boolean doInBackground(Long... params) {
        try {
            JustawayApplication.getApplication().getTwitter().destroyDirectMessage(params[0]);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success == true) {
            JustawayApplication.showToast("DM削除に成功しました>゜))彡");
        } else {
            JustawayApplication.showToast("DM削除に失敗しました＞＜");
        }
    }
}
