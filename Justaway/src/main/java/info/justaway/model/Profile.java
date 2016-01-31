package info.justaway.model;

import twitter4j.Relationship;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.User;

public class Profile {

    private User user;
    private Relationship relationship;
    private ResponseList<Status> statuses;
    private String error;

    public ResponseList<Status> getStatuses() {
        return statuses;
    }

    public void setStatuses(ResponseList<Status> statuses) {
        this.statuses = statuses;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Relationship getRelationship() {
        return relationship;
    }

    public void setRelationship(Relationship relationship) {
        this.relationship = relationship;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }
}
