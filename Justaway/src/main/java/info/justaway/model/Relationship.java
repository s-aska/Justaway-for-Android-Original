package info.justaway.model;

import android.os.AsyncTask;
import android.support.v4.util.LongSparseArray;

import java.util.ArrayList;

import twitter4j.IDs;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;

public class Relationship {
    private static final LongSparseArray<Boolean> mIsBlockMap = new LongSparseArray<>();
    private static final LongSparseArray<Boolean> mIsOfficialMuteMap = new LongSparseArray<>();
    private static final LongSparseArray<Boolean> mIsNoRetweetMap = new LongSparseArray<>();

    public static void init() {
        ArrayList<AccessToken> accessTokens = AccessTokenManager.getAccessTokens();
        if (accessTokens == null || accessTokens.size() == 0) {
            return;
        }
        for (AccessToken accessToken : accessTokens) {
            Twitter twitter = TwitterManager.getTwitterInstance();
            twitter.setOAuthAccessToken(accessToken);
            loadBlock(twitter);
            loadOfficialMute(twitter);
            loadNoRetweet(twitter);
        }
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
}
