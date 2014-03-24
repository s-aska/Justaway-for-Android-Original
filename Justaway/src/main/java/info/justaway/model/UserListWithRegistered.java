package info.justaway.model;

import twitter4j.UserList;

public class UserListWithRegistered {

    private UserList userList;
    private boolean registered;

    public UserList getUserList() {
        return userList;
    }

    public void setUserList(UserList userList) {
        this.userList = userList;
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }
}