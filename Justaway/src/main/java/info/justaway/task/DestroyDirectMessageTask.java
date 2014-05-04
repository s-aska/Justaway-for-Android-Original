package info.justaway.task;

import android.os.AsyncTask;

import de.greenrobot.event.EventBus;
import info.justaway.JustawayApplication;
import info.justaway.R;
import info.justaway.event.model.StreamingDestroyMessageEvent;
import twitter4j.DirectMessage;

public class DestroyDirectMessageTask extends AsyncTask<Long, Void, DirectMessage> {

    @Override
    protected DirectMessage doInBackground(Long... params) {
        try {
            return JustawayApplication.getApplication().getTwitter().destroyDirectMessage(params[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(DirectMessage directMessage) {
        if (directMessage != null) {
            JustawayApplication.showToast(R.string.toast_destroy_direct_message_success);
            EventBus.getDefault().post(new StreamingDestroyMessageEvent(directMessage.getId()));
        } else {
            JustawayApplication.showToast(R.string.toast_destroy_direct_message_failure);
        }
    }
}
