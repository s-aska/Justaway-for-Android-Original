package info.justaway.adapter;

import android.os.AsyncTask;
import android.os.Handler;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import info.justaway.JustawayApplication;
import info.justaway.event.model.CreateFavoriteEvent;
import info.justaway.event.model.CreateStatusEvent;
import info.justaway.event.model.DestroyDirectMessageEvent;
import info.justaway.event.model.DestroyStatusEvent;
import info.justaway.event.model.NotificationEvent;
import info.justaway.event.model.UnFavoriteEvent;
import info.justaway.model.Row;
import twitter4j.DirectMessage;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.User;
import twitter4j.UserStreamAdapter;

public class MyUserStreamAdapter extends UserStreamAdapter {

    private boolean mStopped;
    private boolean mPause;
    private ArrayList<CreateStatusEvent> mCreateStatusEvents = new ArrayList<CreateStatusEvent>();
    private ArrayList<DestroyStatusEvent> mDestroyStatusEvents = new ArrayList<DestroyStatusEvent>();
    private ArrayList<CreateFavoriteEvent> mCreateFavoriteEvents = new ArrayList<CreateFavoriteEvent>();
    private ArrayList<UnFavoriteEvent> mUnFavoriteEvents = new ArrayList<UnFavoriteEvent>();
    private ArrayList<DestroyDirectMessageEvent> mDestroyDirectMessageEvents = new ArrayList<DestroyDirectMessageEvent>();

    public void stop() {
        mStopped = true;
    }

    public void start() {
        mStopped = false;
    }

    public void pause() {
        mPause = true;
    }

    public void resume() {
        mPause = false;
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                for (CreateStatusEvent event : mCreateStatusEvents) {
                    EventBus.getDefault().post(event);
                }
                for (DestroyStatusEvent event : mDestroyStatusEvents) {
                    EventBus.getDefault().post(event);
                }
                for (CreateFavoriteEvent event : mCreateFavoriteEvents) {
                    EventBus.getDefault().post(event);
                }
                for (UnFavoriteEvent event : mUnFavoriteEvents) {
                    EventBus.getDefault().post(event);
                }
                for (DestroyDirectMessageEvent event : mDestroyDirectMessageEvents) {
                    EventBus.getDefault().post(event);
                }
                mCreateStatusEvents.clear();
                mDestroyStatusEvents.clear();
                mCreateFavoriteEvents.clear();
                mUnFavoriteEvents.clear();
                mDestroyDirectMessageEvents.clear();
            }
        });
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
        long userId = JustawayApplication.getApplication().getUserId();
        Status retweet = status.getRetweetedStatus();
        if (status.getInReplyToUserId() == userId || (retweet != null && retweet.getUser().getId() == userId)) {
            EventBus.getDefault().post(new NotificationEvent(row));
        }
        if (mPause) {
            mCreateStatusEvents.add(new CreateStatusEvent(row));
        } else {
            EventBus.getDefault().post(new CreateStatusEvent(row));
        }
    }

    @Override
    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
        if (mStopped) {
            return;
        }
        if (mPause) {
            mDestroyStatusEvents.add(new DestroyStatusEvent(statusDeletionNotice.getStatusId()));
        } else {
            EventBus.getDefault().post(new DestroyStatusEvent(statusDeletionNotice.getStatusId()));
        }
    }

    @Override
    public void onFavorite(User source, User target, Status status) {
        if (mStopped) {
            return;
        }
        // 自分の fav を反映
        JustawayApplication application = JustawayApplication.getApplication();
        if (source.getId() == application.getUserId()) {
            application.setFav(status.getId());
            return;
        }
        Row row = Row.newFavorite(source, target, status);
        EventBus.getDefault().post(new NotificationEvent(row));
        new AsyncTask<Row, Void, twitter4j.Status>(){
            private Row mRow;
            @Override
            protected twitter4j.Status doInBackground(Row... params) {
                mRow = params[0];
                try {
                    return JustawayApplication.getApplication().getTwitter().showStatus(mRow.getStatus().getId());
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(twitter4j.Status status) {
                if (status != null) {
                    mRow.setStatus(status);
                }
                if (mPause) {
                    mCreateFavoriteEvents.add(new CreateFavoriteEvent(mRow));
                } else {
                    EventBus.getDefault().post(new CreateFavoriteEvent(mRow));
                }
                JustawayApplication.showToast(mRow.getSource().getScreenName() + " fav "
                        + mRow.getStatus().getText());
            }
        }.execute(row);
    }

    @Override
    public void onUnfavorite(User arg0, User arg1, Status arg2) {
        if (mStopped) {
            return;
        }
        // 自分の unfav を反映
        JustawayApplication application = JustawayApplication.getApplication();
        if (arg0.getId() == application.getUserId()) {
            application.removeFav(arg2.getId());
            return;
        }
        if (mPause) {
            mUnFavoriteEvents.add(new UnFavoriteEvent(arg0, arg2));
        } else {
            EventBus.getDefault().post(new UnFavoriteEvent(arg0, arg2));
        }
    }

    @Override
    public void onDirectMessage(DirectMessage directMessage) {
        if (mStopped) {
            return;
        }
        Row row = Row.newDirectMessage(directMessage);
        if (JustawayApplication.isMute(row)) {
            return;
        }
        EventBus.getDefault().post(new NotificationEvent(row));
        if (mPause) {
            mCreateStatusEvents.add(new CreateStatusEvent(row));
        } else {
            EventBus.getDefault().post(new CreateStatusEvent(row));
        }
    }

    @Override
    public void onDeletionNotice(long directMessageId, long userId) {
        if (mStopped) {
            return;
        }
        if (mPause) {
            mDestroyDirectMessageEvents.add(new DestroyDirectMessageEvent(directMessageId));
        } else {
            EventBus.getDefault().post(new DestroyDirectMessageEvent(directMessageId));
        }
    }
}
