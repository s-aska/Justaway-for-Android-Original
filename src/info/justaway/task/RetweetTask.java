package info.justaway.task;

import info.justaway.JustawayApplication;
import info.justaway.model.Row;
import android.os.AsyncTask;

public class RetweetTask extends AsyncTask<Row, Void, twitter4j.Status> {

    private Row mRow;

    @Override
    protected twitter4j.Status doInBackground(Row... params) {
        mRow = params[0];
        try {
            return JustawayApplication.getApplication().getTwitter().retweetStatus(mRow.getStatus().getId());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(twitter4j.Status status) {
        if (status != null) {
            mRow.setStatus(status);
            JustawayApplication.showToast("RTに成功しました>゜))彡");
        } else {
            JustawayApplication.showToast("RTに失敗しました＞＜");
        }
    }
}