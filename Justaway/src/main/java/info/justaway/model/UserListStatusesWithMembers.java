package info.justaway.model;

import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.User;

public class UserListStatusesWithMembers {
    private ResponseList<Status> mStatuses;
    private ResponseList<User> mMembers;

    public UserListStatusesWithMembers(ResponseList<Status> statues, ResponseList<User> members) {
        super();
        this.mStatuses = statues;
        this.mMembers = members;
    }

    public ResponseList<Status> getStatues() {
        return mStatuses;
    }

    public ResponseList<User> getMembers() {
        return mMembers;
    }
}
