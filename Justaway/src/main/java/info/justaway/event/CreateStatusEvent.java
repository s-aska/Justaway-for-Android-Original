package info.justaway.event;

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
