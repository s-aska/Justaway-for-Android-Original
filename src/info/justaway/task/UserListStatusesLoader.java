package info.justaway.task;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import android.content.Context;
import info.justaway.JustawayApplication;
import info.justaway.model.UserListStatusesWithMembers;

public class UserListStatusesLoader extends AbstractAsyncTaskLoader<UserListStatusesWithMembers> {

    private int userListId;

    public UserListStatusesLoader(Context context, int userListId) {
        super(context);
        this.userListId = userListId;
    }

    @Override
    public UserListStatusesWithMembers loadInBackground() {
        try {
            Twitter twitter = JustawayApplication.getApplication().getTwitter();
            ResponseList<Status> statuses = twitter.getUserListStatuses(userListId,
                    new Paging(1, 40));
            ResponseList<User> members = twitter.getUserListMembers(userListId, 0);
            return new UserListStatusesWithMembers(statuses, members);
        } catch (TwitterException e) {
            e.printStackTrace();
            return null;
        }
    }
}
