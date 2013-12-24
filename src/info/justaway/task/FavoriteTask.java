package info.justaway.task;

import info.justaway.JustawayApplication;

import android.os.AsyncTask;

public class FavoriteTask extends AsyncTask<Long, Void, Boolean> {

    @Override
    protected Boolean doInBackground(Long... params) {
        try {
            JustawayApplication application = JustawayApplication.getApplication();
            application.getTwitter().createFavorite(params[0]);
            application.setFav(params[0]);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            JustawayApplication.showToast("ふぁぼに成功しました>゜))彡");
        } else {
            JustawayApplication.showToast("ふぁぼに失敗しました＞＜");
        }
    }
}
