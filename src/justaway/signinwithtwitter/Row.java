package justaway.signinwithtwitter;

import twitter4j.Status;
import twitter4j.User;

public class Row {

    private final static int TYPE_STATUS = 0;
    private final static int TYPE_FAVORITE = 1;

    private Status status;
    private User source;
    private User target;
    private int type;

    public Row() {
        super();
    }

    public static Row newStatus(Status status) {
        Row row = new Row();
        row.setStatus(status);
        row.setType(TYPE_STATUS);
        return row;
    }

    public static Row newFavorite(User source, User target, Status status) {
        Row row = new Row();
        row.setStatus(status);
        row.setTarget(target);
        row.setSource(source);
        row.setType(TYPE_FAVORITE);
        return row;
    }

    public boolean isStatus() {
        return type == TYPE_STATUS ? true : false;
    }

    public boolean isFavorite() {
        return type == TYPE_FAVORITE ? true : false;
    }

    public Status getStatus() {
        return status;
    }
    public void setStatus(Status status) {
        this.status = status;
    }
    public User getSource() {
        return source;
    }
    public void setSource(User source) {
        this.source = source;
    }
    public User getTarget() {
        return target;
    }
    public void setTarget(User target) {
        this.target = target;
    }
    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }
}
