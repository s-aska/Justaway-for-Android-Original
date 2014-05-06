package info.justaway.model;

import twitter4j.ResponseList;
import twitter4j.UserList;

public class UserListCache {
    private static ResponseList<UserList> sUserLists;

    public static ResponseList<UserList> getUserLists() {
        return sUserLists;
    }

    public static void setUserLists(ResponseList<UserList> userLists) {
        sUserLists = userLists;
    }

    public static UserList getUserList(long id) {
        if (sUserLists == null) {
            return null;
        }
        for (UserList userList : sUserLists) {
            if (userList.getId() == id) {
                return userList;
            }
        }
        return null;
    }
}
