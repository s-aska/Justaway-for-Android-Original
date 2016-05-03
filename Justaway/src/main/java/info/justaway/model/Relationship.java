package info.justaway.model;

import android.os.AsyncTask;
import android.support.v4.util.LongSparseArray;

import java.util.ArrayList;

import twitter4j.IDs;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;

public class Relationship {
    private static final LongSparseArray<Boolean> mIsBlockMap = new LongSparseArray<>();
    private static final LongSparseArray<Boolean> mIsOfficialMuteMap = new LongSparseArray<>();
    private static final LongSparseArray<Boolean> mIsNoRetweetMap = new LongSparseArray<>();
    private static final LongSparseArray<Boolean> mMyIDMap = new LongSparseArray<>();

    public static void init() {
        ArrayList<AccessToken> accessTokens = AccessTokenManager.getAccessTokens();
        if (accessTokens == null || accessTokens.size() == 0) {
            return;
        }
        for (AccessToken accessToken : accessTokens) {
            Twitter twitter = TwitterManager.getTwitterInstance();
            twitter.setOAuthAccessToken(accessToken);
            mMyIDMap.put(accessToken.getUserId(), true);
            loadBlock(twitter);
            loadOfficialMute(twitter);
            loadNoRetweet(twitter);
        }
    }

    public static boolean isMe(long userId) {
        return mMyIDMap.get(userId, false);
    }

    public static boolean isBlock(long userId) {
        return mIsBlockMap.get(userId, false);
    }

    public static boolean isOfficialMute(long userId) {
        return mIsOfficialMuteMap.get(userId, false);
    }

    public static boolean isNoRetweet(long userId) {
        return mIsNoRetweetMap.get(userId, false);
    }

    public static void setBlock(long userId) {
        mIsBlockMap.put(userId, true);
    }

    public static void setOfficialMute(long userId) {
        mIsOfficialMuteMap.put(userId, true);
    }

    public static void setNoRetweet(long userId) {
        mIsNoRetweetMap.put(userId, true);
    }

    public static void removeBlock(long userId) {
        mIsBlockMap.remove(userId);
    }

    public static void removeOfficialMute(long userId) {
        mIsOfficialMuteMap.remove(userId);
    }

    public static void removeNoRetweet(long userId) {
        mIsNoRetweetMap.remove(userId);
    }

    public static void loadBlock(final Twitter twitter) {
        new AsyncTask<Void, Void, IDs>() {

            @Override
            protected IDs doInBackground(Void... voids) {
                try {
                    return twitter.getBlocksIDs();
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(IDs iDs) {
                if (iDs == null) {
                    return;
                }
                for (long id : iDs.getIDs()) {
                    mIsBlockMap.put(id, true);
                }
            }
        }.execute();
    }

    public static void loadOfficialMute(final Twitter twitter) {
        new AsyncTask<Void, Void, IDs>() {

            @Override
            protected IDs doInBackground(Void... voids) {
                try {
                    return twitter.getMutesIDs(-1L);
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(IDs iDs) {
                if (iDs == null) {
                    return;
                }
                for (long id : iDs.getIDs()) {
                    mIsOfficialMuteMap.put(id, true);
                }
            }
        }.execute();
    }

    public static void loadNoRetweet(final Twitter twitter) {
        new AsyncTask<Void, Void, IDs>() {

            @Override
            protected IDs doInBackground(Void... voids) {
                try {
                    return twitter.getNoRetweetsFriendships();
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(IDs iDs) {
                if (iDs == null) {
                    return;
                }
                for (long id : iDs.getIDs()) {
                    mIsNoRetweetMap.put(id, true);
                }
            }
        }.execute();
    }

    public static boolean isVisible(final twitter4j.Status status) {
        if (Relationship.isMe(status.getUser().getId())) {
            return true;
        }
        if (Relationship.isBlock(status.getUser().getId())) {
            return false;
        }
        if (Relationship.isOfficialMute(status.getUser().getId())) {
            return false;
        }
        Status retweetedStatus = status.getRetweetedStatus();
        if (retweetedStatus != null) {
            if (Relationship.isNoRetweet(status.getUser().getId())) {
                return false;
            }
            if (Relationship.isBlock(retweetedStatus.getUser().getId())) {
                return false;
            }
            if (Relationship.isOfficialMute(retweetedStatus.getUser().getId())) {
                return false;
            }
        }
        Status quotedStatus = status.getQuotedStatus();
        if (quotedStatus != null) {
            if (Relationship.isBlock(quotedStatus.getUser().getId())) {
                return false;
            }
            if (Relationship.isOfficialMute(quotedStatus.getUser().getId())) {
                return false;
            }
        }
        return true;
    }
}
