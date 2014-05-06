package info.justaway;

import android.app.ActionBar;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.RadioGroup;

import butterknife.InjectView;
import butterknife.OnClick;

public class CreateUserListActivity extends Activity {

    @InjectView(R.id.list_name) EditText mListName;
    @InjectView(R.id.list_description) EditText mListDescription;
    @InjectView(R.id.privacy_radio_group) RadioGroup mPrivacyRadioGroup;

    private boolean mPrivacy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JustawayApplication.getApplication().setTheme(this);
        setContentView(R.layout.activity_create_user_list);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

    }

    @OnClick(R.id.save)
    void Save() {
        JustawayApplication.showProgressDialog(this, getString(R.string.progress_process));
        if (mPrivacyRadioGroup.getCheckedRadioButtonId() == R.id.public_radio) {
            mPrivacy = true;
        }
        new CreateUserListTask().execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    private class CreateUserListTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                // noinspection ConstantConditions
                JustawayApplication.getApplication().getTwitter().createUserList(
                        mListName.getText().toString(),
                        mPrivacy,
                        mListDescription.getText().toString());
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            JustawayApplication.dismissProgressDialog();
            if (success) {
                JustawayApplication.showToast(R.string.toast_create_user_list_success);
                finish();
            } else {
                JustawayApplication.showToast(R.string.toast_create_user_list_failure);
            }
        }
    }
}