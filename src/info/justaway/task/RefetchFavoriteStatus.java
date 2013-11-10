package info.justaway.task;

import info.justaway.JustawayApplication;
import info.justaway.fragment.BaseFragment;
import info.justaway.model.Row;
import android.os.AsyncTask;

public class RefetchFavoriteStatus extends AsyncTask<Row, Void, twitter4j.Status> {

    private BaseFragment fragment;
    private Row row;
    // TODO: use http://cdn.api.twitter.com/1/urls/count.json

    public RefetchFavoriteStatus(BaseFragment fragment) {
        super();
        this.fragment = fragment;
    }

    @Override
    protected twitter4j.Status doInBackground(Row... params) {
        row = params[0];
        try {
            return JustawayApplication.getApplication().getTwitter().showStatus(row.getStatus().getId());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(twitter4j.Status status) {
        if (status != null) {
            row.setStatus(status);
            fragment.add(row);
            JustawayApplication.showToast(row.getSource().getScreenName() + " fav "
                    + row.getStatus().getText());
        }
    }
}