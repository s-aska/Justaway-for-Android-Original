package info.justaway.listener;

import de.greenrobot.event.EventBus;
import info.justaway.event.UserStreamingOnCleanupEvent;
import info.justaway.event.UserStreamingOnConnectEvent;
import info.justaway.event.UserStreamingOnDisconnectEvent;
import twitter4j.ConnectionLifeCycleListener;

public class JustawayConnectionLifeCycleListener implements ConnectionLifeCycleListener {
    @Override
    public void onConnect() {
        EventBus.getDefault().post(new UserStreamingOnConnectEvent());
    }

    @Override
    public void onDisconnect() {
        EventBus.getDefault().post(new UserStreamingOnDisconnectEvent());
    }

    @Override
    public void onCleanUp() {
        EventBus.getDefault().post(new UserStreamingOnCleanupEvent());
    }
}
