package info.justaway.task;

import android.content.Context;

import info.justaway.JustawayApplication;
import twitter4j.ResponseList;
import twitter4j.TwitterException;
import twitter4j.UserList;

public class UserListsLoader extends AbstractAsyncTaskLoader<ResponseList<UserList>> {

    public UserListsLoader(Context context) {
        super(context);
    }

    @Override
    public ResponseList<UserList> loadInBackground() {
        try {
            return JustawayApplication.getApplication().getTwitter()
                    .getUserLists(JustawayApplication.getApplication().getUserId());
        } catch (TwitterException e) {
            e.printStackTrace();
            return null;
        }
    }
}
