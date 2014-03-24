package info.justaway;

import android.content.Context;

import info.justaway.task.AbstractAsyncTaskLoader;
import twitter4j.ResponseList;
import twitter4j.TwitterException;
import twitter4j.UserList;

public class RegisterUserListsLoader extends AbstractAsyncTaskLoader<ResponseList<UserList>> {

    public RegisterUserListsLoader(Context context) {
        super(context);
    }

    @Override
    public ResponseList<UserList> loadInBackground() {
        try {
            ResponseList<UserList> userLists = ;
            for (UserList userList : JustawayApplication.getApplication().getTwitter()
                    .getUserListsOwnerships(JustawayApplication.getApplication().getUserId()))
            return userLists;
        } catch (TwitterException e) {
            e.printStackTrace();
            return null;
        }
    }
}
