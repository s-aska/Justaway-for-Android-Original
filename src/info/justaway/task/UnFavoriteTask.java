package info.justaway.task;

import info.justaway.JustawayApplication;
import android.os.AsyncTask;

public class UnFavoriteTask extends AsyncTask<Long, Void, Boolean> {

    @Override
    protected Boolean doInBackground(Long... params) {
        try {
            JustawayApplication.getApplication().getTwitter().destroyFavorite(params[0]);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success == true) {
            JustawayApplication.showToast("あんふぁぼに成功しました>゜))彡");
        } else {
            JustawayApplication.showToast("あんふぁぼに失敗しました＞＜");
        }
    }
}
