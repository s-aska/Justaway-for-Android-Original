package info.justaway.task;

import android.os.AsyncTask;

import info.justaway.JustawayApplication;
import info.justaway.model.AccessTokenManager;
import info.justaway.model.TwitterManager;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.UserList;

/**
 * アプリケーションのメンバ変数にユーザーリストを読み込む
 */
public class LoadUserListsTask extends AsyncTask<Void, Void, ResponseList<UserList>> {
    @Override
    protected ResponseList<UserList> doInBackground(Void... params) {
        try {
            Twitter twitter = TwitterManager.getTwitter();
            return twitter.getUserLists(AccessTokenManager.getUserId());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(ResponseList<UserList> userLists) {
        if (userLists != null) {
            JustawayApplication.getApplication().setUserLists(userLists);
        }
    }
}
