package info.justaway;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;


public class PostListenerService extends WearableListenerService {

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        DataEvent dataEvent = dataEvents.get(0);

        String text = DataMap.fromByteArray(dataEvent.getDataItem().getData()).getString("text");
        dataEvents.close();

        Intent intent = new Intent(this, PostActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("status", text);
        intent.putExtra("wearable", true);
        startActivity(intent);
    }

}
