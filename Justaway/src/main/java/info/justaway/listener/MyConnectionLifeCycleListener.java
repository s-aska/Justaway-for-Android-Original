package info.justaway.listener;

import de.greenrobot.event.EventBus;
import info.justaway.event.connection.CleanupEvent;
import info.justaway.event.connection.ConnectEvent;
import info.justaway.event.connection.DisconnectEvent;
import twitter4j.ConnectionLifeCycleListener;

public class MyConnectionLifeCycleListener implements ConnectionLifeCycleListener {
    @Override
    public void onConnect() {
        EventBus.getDefault().post(new ConnectEvent());
    }

    @Override
    public void onDisconnect() {
        EventBus.getDefault().post(new DisconnectEvent());
    }

    @Override
    public void onCleanUp() {
        EventBus.getDefault().post(new CleanupEvent());
    }
}
