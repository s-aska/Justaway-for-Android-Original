package info.justaway.event.connection;

public class StreamingConnectionEvent {

    /**
     * 接続、クリーンナップ、切断
     */
    public static enum Status {
        STREAMING_CONNECT,
        STREAMING_CLEANUP,
        STREAMING_DISCONNECT
    }

    private Status mStatus;

    public StreamingConnectionEvent(Status status) {
        mStatus = status;
    }

    public static StreamingConnectionEvent onConnect() {
        return new StreamingConnectionEvent(Status.STREAMING_CONNECT);
    }

    public static StreamingConnectionEvent onCleanUp() {
        return new StreamingConnectionEvent(Status.STREAMING_CLEANUP);
    }

    public static StreamingConnectionEvent onDisconnect() {
        return new StreamingConnectionEvent(Status.STREAMING_DISCONNECT);
    }

    public Status getStatus() {
        return mStatus;
    }
}
