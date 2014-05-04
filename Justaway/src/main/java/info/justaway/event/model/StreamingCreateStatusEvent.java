package info.justaway.event.model;

import info.justaway.model.Row;

public class StreamingCreateStatusEvent {
    private final Row row;

    public StreamingCreateStatusEvent(Row row) {
        this.row = row;
    }

    public Row getRow() {
        return row;
    }
}
