package info.justaway.task;

import info.justaway.JustawayApplication;
import info.justaway.model.Row;
import android.os.AsyncTask;
import android.util.Log;

public class UnRetweetTask extends AsyncTask<Row, Void, twitter4j.Status> {

    private Row mRow;

    @Override
    protected twitter4j.Status doInBackground(Row... params) {
        mRow = params[0];
        try {
            if (mRow.unRetweetId == null) {
                Log.e("Justaway", "Row has's unRetweetId.");
                return null;
            }
            return JustawayApplication.getApplication().getTwitter().destroyStatus(mRow.unRetweetId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(twitter4j.Status status) {
        if (status != null) {
            mRow.setFlag(status);
            JustawayApplication.showToast("公式RT解除に成功しました>゜))彡");
        } else {
            JustawayApplication.showToast("公式RT解除に失敗しました＞＜");
        }
    }
}