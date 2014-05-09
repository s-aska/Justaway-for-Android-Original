package info.justaway.event.model;

import info.justaway.model.Row;

public class StreamingCreateFavoriteEvent {
    private final Row row;

    public StreamingCreateFavoriteEvent(Row row) {
        this.row = row;
    }

    public Row getRow() {
        return row;
    }
}
