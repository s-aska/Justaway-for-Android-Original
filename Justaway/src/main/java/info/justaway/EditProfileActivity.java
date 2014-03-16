package info.justaway;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.File;

import info.justaway.fragment.profile.UpdateProfileImageFragment;
import info.justaway.task.VerifyCredentialsLoader;
import twitter4j.User;

public class EditProfileActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<User> {

    private static final int REQ_PICK_PROFILE_IMAGE = 1;

    private EditText mName;
    private EditText mLocation;
    private EditText mUrl;
    private EditText mDescription;
    private ImageView mIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JustawayApplication.getApplication().setTheme(this);
        setContentView(R.layout.activity_edit_profile);

        final Activity activity = this;
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mName = ((EditText) findViewById(R.id.name));
        mLocation = ((EditText) findViewById(R.id.location));
        mUrl = ((EditText) findViewById(R.id.web_site));
        mDescription = ((EditText) findViewById(R.id.bio));
        mIcon = ((ImageView) findViewById(R.id.icon));

        findViewById(R.id.update_profile_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQ_PICK_PROFILE_IMAGE);
            }
        });

        findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JustawayApplication.showProgressDialog(activity, getString(R.string.progress_process));
                new UpdateProfileTask().execute();
            }
        });

        /**
         * onCreateLoader => onLoadFinished と繋がる
         */
        getSupportLoaderManager().initLoader(0, null, this);

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

    @Override
    public Loader<User> onCreateLoader(int id, Bundle args) {
        return new VerifyCredentialsLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<User> loader, User user) {
        JustawayApplication application = JustawayApplication.getApplication();
        if (user == null) {
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
            finish();
        } else {
            mName.setText(user.getName());
            mLocation.setText(user.getLocation());
            mUrl.setText(user.getURLEntity().getExpandedURL());
            mDescription.setText(user.getDescription());
            application.displayRoundedImage(user.getOriginalProfileImageURL(), mIcon);
        }
    }

    @Override
    public void onLoaderReset(Loader<User> arg0) {

    }

    private class UpdateProfileTask extends AsyncTask<Void, Void, User> {
        @Override
        protected User doInBackground(Void... params) {
            try {
                //noinspection ConstantConditions
                return JustawayApplication.getApplication().getTwitter().updateProfile(
                        mName.getText().toString(),
                        mUrl.getText().toString(),
                        mLocation.getText().toString(),
                        mDescription.getText().toString());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(User user) {
            JustawayApplication.dismissProgressDialog();
            if (user != null) {
                JustawayApplication.showToast(R.string.toast_update_profile_success);
                finish();
            } else {
                JustawayApplication.showToast(R.string.toast_update_profile_failure);
            }
        }
    }

    private File uriToFile(Uri uri) {
        ContentResolver cr = getContentResolver();
        String[] columns = {MediaStore.Images.Media.DATA};
        Cursor c = cr.query(uri, columns, null, null, null);
        assert c != null;
        c.moveToFirst();
        String fileName = c.getString(0);
        if (fileName == null) {
            JustawayApplication.showToast(getString(R.string.toast_set_image_failure));
            return null;
        }
        File path = new File(fileName);

        if (!path.exists()) {
            return null;
        }

        return path;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_PICK_PROFILE_IMAGE:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    File file = uriToFile(uri);
                    if (file != null) {
                        UpdateProfileImageFragment dialog = UpdateProfileImageFragment.newInstance(file, uri);
                        dialog.show(getSupportFragmentManager(), "dialog");
                    }
                }
                break;
        }
    }
}