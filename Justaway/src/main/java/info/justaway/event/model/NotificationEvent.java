package info.justaway.event.model;

import info.justaway.model.Row;

public class NotificationEvent {

    private final Row mRow;

    public NotificationEvent(final Row row) {
        mRow = row;
    }

    public Row getRow() {
        return mRow;
    }
}
