package info.justaway;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

import info.justaway.task.UpdateStatusTask;
import info.justaway.util.MessageUtil;
import info.justaway.util.ThemeUtil;
import twitter4j.StatusUpdate;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;


public class PostListenerService extends WearableListenerService {

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        DataEvent dataEvent = dataEvents.get(0);

        String text = DataMap.fromByteArray(dataEvent.getDataItem().getData()).getString("text");
        dataEvents.close();

        StatusUpdate statusUpdate = new StatusUpdate(text);
        UpdateStatusTask task = new UpdateStatusTask(null) {
            @Override
            protected void onPostExecute(TwitterException e) {
                if (e == null) {
                    // 成功
                } else {
                    // 失敗
                }
            }
        };
        task.execute(statusUpdate);
    }

}
