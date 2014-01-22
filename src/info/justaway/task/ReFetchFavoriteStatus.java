package info.justaway.task;

import android.os.AsyncTask;

import info.justaway.JustawayApplication;
import info.justaway.fragment.main.BaseFragment;
import info.justaway.model.Row;

public class ReFetchFavoriteStatus extends AsyncTask<Row, Void, twitter4j.Status> {

    private BaseFragment mFragment;
    private Row mRow;
    // TODO: use http://cdn.api.twitter.com/1/urls/count.json

    public ReFetchFavoriteStatus(BaseFragment fragment) {
        super();
        this.mFragment = fragment;
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
            mFragment.add(mRow);
            JustawayApplication.showToast(mRow.getSource().getScreenName() + " fav "
                    + mRow.getStatus().getText());
        }
    }
}