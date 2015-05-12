package info.justaway.task;

import android.os.AsyncTask;

import java.io.File;
import java.util.ArrayList;

import info.justaway.model.ImageResizer;
import info.justaway.model.TwitterManager;
import info.justaway.settings.PostStockSettings;
import twitter4j.HashtagEntity;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.UploadedMedia;
import twitter4j.auth.AccessToken;

public class UpdateStatusTask extends AsyncTask<StatusUpdate, Void, TwitterException> {

    private AccessToken mAccessToken;
    private ArrayList<File> mImagePathList;

    public UpdateStatusTask(AccessToken accessToken, ArrayList<File> imagePathList) {
        mAccessToken = accessToken;
        mImagePathList = imagePathList;
    }

    @Override
    protected TwitterException doInBackground(StatusUpdate... params) {
        StatusUpdate statusUpdate = params[0];
        long maxFileSize = 3145728; // 3MB
        try {
            twitter4j.Status status;
            if (mAccessToken == null) {
                status = TwitterManager.getTwitter().updateStatus(statusUpdate);
            } else {
                // ツイート画面から来たとき
                Twitter twitter = TwitterManager.getTwitterInstance();
                twitter.setOAuthAccessToken(mAccessToken);

                if (!mImagePathList.isEmpty()) {
                    long[] mediaIds = new long[mImagePathList.size()];
                    for (int i = 0; i < mImagePathList.size(); i++) {
                        File imageFile = ImageResizer.compress(mImagePathList.get(i), maxFileSize);
                        UploadedMedia media = twitter.uploadMedia(imageFile);
                        mediaIds[i] = media.getMediaId();
                    }
                    statusUpdate.setMediaIds(mediaIds);
                }

                status = twitter.updateStatus(statusUpdate);
            }
            PostStockSettings postStockSettings = new PostStockSettings();
            for (HashtagEntity hashtagEntity : status.getHashtagEntities()) {
                postStockSettings.addHashtag("#".concat(hashtagEntity.getText()));
            }
        } catch (TwitterException e) {
            e.printStackTrace();
            return e;
        }
        return null;
    }
}