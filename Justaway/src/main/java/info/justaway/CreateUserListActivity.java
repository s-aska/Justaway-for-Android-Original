package info.justaway;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.RadioGroup;

import butterknife.ButterKnife;
import info.justaway.task.CreateUserListTask;
import info.justaway.util.MessageUtil;
import info.justaway.util.ThemeUtil;
import butterknife.Bind;
import butterknife.OnClick;
import twitter4j.TwitterException;

public class CreateUserListActivity extends Activity {

    public static final int ERROR_CODE_NAME_BLANK = 403;

    @Bind(R.id.list_name)
    EditText mListName;
    @Bind(R.id.list_description)
    EditText mListDescription;
    @Bind(R.id.privacy_radio_group)
    RadioGroup mPrivacyRadioGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeUtil.setTheme(this);
        setContentView(R.layout.activity_create_user_list);
        ButterKnife.bind(this);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

    }

    @OnClick(R.id.save)
    void Save() {
        boolean privacy = false;
        MessageUtil.showProgressDialog(this, getString(R.string.progress_process));
        if (mPrivacyRadioGroup.getCheckedRadioButtonId() == R.id.public_radio) {
            privacy = true;
        }

        new CreateUserListTask(mListName.getText().toString(), privacy, mListDescription.getText().toString()) {
            @Override
            protected void onPostExecute(TwitterException e) {
                MessageUtil.dismissProgressDialog();
                if (e == null) {
                    MessageUtil.showToast(R.string.toast_create_user_list_success);
                    finish();
                } else {
                    if (e.getStatusCode() == ERROR_CODE_NAME_BLANK) {
                        MessageUtil.showToast(R.string.toast_create_user_list_failure_name_blank);
                    } else {
                        MessageUtil.showToast(R.string.toast_create_user_list_failure);
                    }
                }
            }
        }.execute();
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


}