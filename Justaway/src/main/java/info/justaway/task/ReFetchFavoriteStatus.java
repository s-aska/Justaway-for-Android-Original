package info.justaway.task;

import android.os.AsyncTask;

import de.greenrobot.event.EventBus;
import info.justaway.JustawayApplication;
import info.justaway.event.CreateFavoriteEvent;
import info.justaway.model.Row;

public class ReFetchFavoriteStatus extends AsyncTask<Row, Void, twitter4j.Status> {

    private Row mRow;
    // TODO: use http://cdn.api.twitter.com/1/urls/count.json

    public ReFetchFavoriteStatus() {
        super();
    }

    @Override
    protected twitter4j.Status doInBackground(Row... params) {
        mRow = params[0];
        try {
            return JustawayApplication.getApplication().getTwitter().showStatus(mRow.getStatus().getId());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(twitter4j.Status status) {
        if (status != null) {
            mRow.setStatus(status);
            EventBus.getDefault().post(new CreateFavoriteEvent(mRow));
            JustawayApplication.showToast(mRow.getSource().getScreenName() + " fav "
                    + mRow.getStatus().getText());
        }
    }
}