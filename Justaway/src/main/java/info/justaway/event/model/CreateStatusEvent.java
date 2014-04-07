package info.justaway.event.model;

import info.justaway.model.Row;

public class CreateStatusEvent {
    private final Row row;

    public CreateStatusEvent(Row row) {
        this.row = row;
    }

    public Row getRow() {
        return row;
    }
}
