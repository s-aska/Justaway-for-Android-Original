package info.justaway.task;

import android.content.Context;

import java.util.ArrayList;

import info.justaway.JustawayApplication;
import twitter4j.ResponseList;
import twitter4j.TwitterException;
import twitter4j.UserList;

public class RegisterUserListsLoader extends AbstractAsyncTaskLoader<ArrayList<ResponseList<UserList>>> {

    private long mUserId;

    public RegisterUserListsLoader(Context context, long userId) {
        super(context);
        this.mUserId = userId;
    }

    @Override
    public ArrayList<ResponseList<UserList>> loadInBackground() {
        try {
            JustawayApplication application = JustawayApplication.getApplication();
            ArrayList<ResponseList<UserList>> responseLists = new ArrayList<ResponseList<UserList>>();
            responseLists.add(application.getTwitter().getUserListsOwnerships(application.getUserId(), 200, -1));
            responseLists.add(application.getTwitter().getUserListMemberships(mUserId, -1, true));

            return responseLists;
        } catch (TwitterException e) {
            e.printStackTrace();
            return null;
        }
    }
}
