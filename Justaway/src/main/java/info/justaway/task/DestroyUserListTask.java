package info.justaway.task;

import android.os.AsyncTask;

import de.greenrobot.event.EventBus;
import info.justaway.JustawayApplication;
import info.justaway.R;
import info.justaway.event.DestroyUserListEvent;
import twitter4j.UserList;

public  class DestroyUserListTask extends AsyncTask<Void, Void, Boolean> {

    UserList mUserList;

    public DestroyUserListTask(UserList userList) {
        mUserList = userList;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            JustawayApplication.getApplication().getTwitter().destroyUserList(mUserList.getId());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            JustawayApplication.showToast(R.string.toast_destroy_user_list_success);
            EventBus.getDefault().post(new DestroyUserListEvent(mUserList.getId()));
            JustawayApplication.getApplication().getUserLists().remove(mUserList);
        } else {
            JustawayApplication.showToast(R.string.toast_destroy_user_list_failure);
        }
    }
}
