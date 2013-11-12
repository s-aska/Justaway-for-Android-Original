package info.justaway.model;

import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.User;

public class UserListStatusesWithMembers {
    private ResponseList<Status> statuses;
    private ResponseList<User> members;

    public UserListStatusesWithMembers(ResponseList<Status> statues, ResponseList<User> members) {
        super();
        this.statuses = statues;
        this.members = members;
    }

    public ResponseList<Status> getStatues() {
        return statuses;
    }

    public void setStatues(ResponseList<Status> statues) {
        this.statuses = statues;
    }

    public ResponseList<User> getMembers() {
        return members;
    }

    public void setMembers(ResponseList<User> members) {
        this.members = members;
    }
}
