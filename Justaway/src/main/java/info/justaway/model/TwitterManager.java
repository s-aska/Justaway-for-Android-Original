package info.justaway.model;

import android.os.AsyncTask;
import android.os.Handler;

import de.greenrobot.event.EventBus;
import info.justaway.JustawayApplication;
import info.justaway.R;
import info.justaway.adapter.MyUserStreamAdapter;
import info.justaway.event.action.AccountChangeEvent;
import info.justaway.event.connection.StreamingConnectionEvent;
import twitter4j.ConnectionLifeCycleListener;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterManager {
    /**
     * Twitterインスタンス管理
     */
    private Twitter mTwitter;
    private JustawayApplication mApplication;

    public TwitterManager() {
        mApplication = JustawayApplication.getApplication();
    }

    public void switchAccessToken(final AccessToken accessToken) {
        mApplication.getAccessTokenManager().setAccessToken(accessToken);
        if (mApplication.getBasicSettings().getStreamingMode()) {
            JustawayApplication.showToast(R.string.toast_destroy_streaming);
            mUserStreamAdapter.stop();
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    mTwitterStream.cleanUp();
                    mTwitterStream.shutdown();
                    return null;
                }

                @Override
                protected void onPostExecute(Void status) {
                    mTwitterStream.setOAuthAccessToken(accessToken);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            JustawayApplication.showToast(R.string.toast_create_streaming);
                            mUserStreamAdapter.start();
                            mTwitterStream.user();
                        }
                    }, 5000);
                }
            }.execute();
        }
        EventBus.getDefault().post(new AccountChangeEvent());
    }

    private String getConsumerKey() {
        return mApplication.getString(R.string.twitter_consumer_key);
    }

    private String getConsumerSecret() {
        return mApplication.getString(R.string.twitter_consumer_secret);
    }

    public Twitter getTwitter() {
        if (mTwitter != null) {
            return mTwitter;
        }
        Twitter twitter = getTwitterInstance();

        AccessToken token = mApplication.getAccessTokenManager().getAccessToken();
        if (token != null) {
            twitter.setOAuthAccessToken(token);
            // アクセストークンまである時だけキャッシュしておく
            this.mTwitter = twitter;
        }
        return twitter;
    }

    public Twitter getTwitterInstance() {

        TwitterFactory factory = new TwitterFactory();
        Twitter twitter = factory.getInstance();
        twitter.setOAuthConsumer(getConsumerKey(), getConsumerSecret());

        return twitter;
    }

    public TwitterStream getTwitterStream() {
        AccessToken token = mApplication.getAccessTokenManager().getAccessToken();
        if (token == null) {
            return null;
        }
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        twitter4j.conf.Configuration conf = configurationBuilder.setOAuthConsumerKey(getConsumerKey())
                .setOAuthConsumerSecret(getConsumerSecret()).setOAuthAccessToken(token.getToken())
                .setOAuthAccessTokenSecret(token.getTokenSecret()).build();
        return new TwitterStreamFactory(conf).getInstance();
    }

    private TwitterStream mTwitterStream;
    private boolean mTwitterStreamConnected;
    private MyUserStreamAdapter mUserStreamAdapter;

    public boolean getTwitterStreamConnected() {
        return mTwitterStreamConnected;
    }

    public void startStreaming() {
        if (mTwitterStream != null) {
            if (!mTwitterStreamConnected) {
                mUserStreamAdapter.start();
                mTwitterStream.setOAuthAccessToken(mApplication.getAccessTokenManager().getAccessToken());
                mTwitterStream.user();
            }
            return;
        }
        mTwitterStream = getTwitterStream();
        mUserStreamAdapter = new MyUserStreamAdapter();
        mTwitterStream.addListener(mUserStreamAdapter);
        mTwitterStream.addConnectionLifeCycleListener(new MyConnectionLifeCycleListener());
        mTwitterStream.user();
        mApplication.getBasicSettings().resetNotification();
    }

    public void stopStreaming() {
        if (mTwitterStream == null) {
            return;
        }
        mApplication.getBasicSettings().setStreamingMode(false);
        mUserStreamAdapter.stop();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                mTwitterStream.cleanUp();
                mTwitterStream.shutdown();
                return null;
            }

            @Override
            protected void onPostExecute(Void status) {

            }
        }.execute();
    }

    public void pauseStreaming() {
        if (mUserStreamAdapter != null) {
            mUserStreamAdapter.pause();
        }
    }

    public void resumeStreaming() {
        if (mUserStreamAdapter != null) {
            mUserStreamAdapter.resume();
        }
    }

    public class MyConnectionLifeCycleListener implements ConnectionLifeCycleListener {
        @Override
        public void onConnect() {
            mTwitterStreamConnected = true;
            EventBus.getDefault().post(StreamingConnectionEvent.onConnect());
        }

        @Override
        public void onDisconnect() {
            mTwitterStreamConnected = false;
            EventBus.getDefault().post(StreamingConnectionEvent.onDisconnect());
        }

        @Override
        public void onCleanUp() {
            mTwitterStreamConnected = false;
            EventBus.getDefault().post(StreamingConnectionEvent.onCleanUp());
        }
    }
}
