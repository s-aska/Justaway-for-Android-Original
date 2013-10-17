package info.justaway.task;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;
import android.content.Context;
import info.justaway.JustawayApplication;

public class UserListStatusesLoader extends AbstractAsyncTaskLoader<ResponseList<Status>> {

    private int userListId;
    
    public UserListStatusesLoader(Context context, int userListId) {
        super(context);
        this.userListId = userListId;
    }

    @Override
    public ResponseList<Status> loadInBackground() {
        try {
            return JustawayApplication.getApplication().getTwitter().getUserListStatuses(userListId, new Paging(1, 40));
        } catch (TwitterException e) {
            e.printStackTrace();
            return null;
        }
    }
}
