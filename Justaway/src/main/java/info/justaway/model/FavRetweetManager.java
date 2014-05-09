package info.justaway.model;

import android.support.v4.util.LongSparseArray;

/**
 * どのツイートをふぁぼ又はRTしているかを管理する
 */
public class FavRetweetManager {
    private static final LongSparseArray<Boolean> mIsFavMap = new LongSparseArray<Boolean>();
    private static final LongSparseArray<Long> mRtIdMap = new LongSparseArray<Long>();

    public static void setFav(Long id) {
        mIsFavMap.put(id, true);
    }

    public static void removeFav(Long id) {
        mIsFavMap.remove(id);
    }

    public static Boolean isFav(twitter4j.Status status) {
        if (mIsFavMap.get(status.getId(), false)) {
            return true;
        }
        twitter4j.Status retweet = status.getRetweetedStatus();
        return retweet != null && (mIsFavMap.get(retweet.getId(), false));
    }

    public static void setRtId(Long sourceId, Long retweetId) {
        if (retweetId != null) {
            mRtIdMap.put(sourceId, retweetId);
        } else {
            mRtIdMap.remove(sourceId);
        }
    }

    public static Long getRtId(twitter4j.Status status) {
        Long id = mRtIdMap.get(status.getId());
        if (id != null) {
            return id;
        }
        twitter4j.Status retweet = status.getRetweetedStatus();
        if (retweet != null) {
            return mRtIdMap.get(retweet.getId());
        }
        return null;
    }

    public static Long getRtId(long id) {
        return mRtIdMap.get(id);
    }
}
