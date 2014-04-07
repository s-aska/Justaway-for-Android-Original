package info.justaway.adapter;

import de.greenrobot.event.EventBus;
import info.justaway.JustawayApplication;
import info.justaway.event.model.CreateStatusEvent;
import info.justaway.event.model.DestroyDirectMessageEvent;
import info.justaway.event.model.DestroyStatusEvent;
import info.justaway.event.model.UnFavoriteEvent;
import info.justaway.model.Row;
import info.justaway.task.ReFetchFavoriteStatus;
import twitter4j.DirectMessage;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.User;
import twitter4j.UserStreamAdapter;

public class MyUserStreamAdapter extends UserStreamAdapter {
    private boolean mStopped;

    public void stop() {
        mStopped = true;
    }

    public void start() {
        mStopped = false;
    }

    @Override
    public void onStatus(Status status) {
        if (mStopped) {
            return;
        }
        Row row = Row.newStatus(status);
        if (JustawayApplication.isMute(row)) {
            return;
        }
        EventBus.getDefault().post(new CreateStatusEvent(row));
    }

    @Override
    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
        EventBus.getDefault().post(new DestroyStatusEvent(statusDeletionNotice.getStatusId()));
    }

    @Override
    public void onFavorite(User source, User target, Status status) {
        // 自分の fav を反映
        JustawayApplication application = JustawayApplication.getApplication();
        if (source.getId() == application.getUserId()) {
            application.setFav(status.getId());
            return;
        }
        Row row = Row.newFavorite(source, target, status);
        new ReFetchFavoriteStatus().execute(row);
    }

    @Override
    public void onUnfavorite(User arg0, User arg1, Status arg2) {
        // 自分の unfav を反映
        JustawayApplication application = JustawayApplication.getApplication();
        if (arg0.getId() == application.getUserId()) {
            application.removeFav(arg2.getId());
            return;
        }
        EventBus.getDefault().post(new UnFavoriteEvent(arg0, arg2));
    }

    @Override
    public void onDirectMessage(DirectMessage directMessage) {
        Row row = Row.newDirectMessage(directMessage);
        if (JustawayApplication.isMute(row)) {
            return;
        }
        EventBus.getDefault().post(new CreateStatusEvent(row));
    }

    @Override
    public void onDeletionNotice(long directMessageId, long userId) {
        EventBus.getDefault().post(new DestroyDirectMessageEvent(directMessageId));
    }
}
