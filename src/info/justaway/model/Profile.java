package info.justaway.model;

import twitter4j.Relationship;
import twitter4j.User;

public class Profile {

    private User user;
    private Relationship relationship;

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
}
