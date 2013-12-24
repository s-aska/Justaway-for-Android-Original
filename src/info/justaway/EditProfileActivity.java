package info.justaway;

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
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.File;

import info.justaway.task.VerifyCredentialsLoader;
import twitter4j.User;

public class EditProfileActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<User> {

    private EditText name;
    private EditText location;
    private EditText url;
    private EditText description;
    private ImageView icon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        name = ((EditText) findViewById(R.id.name));
        location = ((EditText) findViewById(R.id.location));
        url = ((EditText) findViewById(R.id.webSite));
        description = ((EditText) findViewById(R.id.bio));
        icon = ((ImageView) findViewById(R.id.icon));

        findViewById(R.id.updateProfileImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });

        findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new UpdateProfileTask().execute();
            }
        });

        /**
         * onCreateLoader => onLoadFinished と繋がる
         */
        getSupportLoaderManager().initLoader(0, null, this);

    }

    @Override
    public Loader<User> onCreateLoader(int id, Bundle args) {
        return new VerifyCredentialsLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<User> loader, User user) {
        JustawayApplication application = JustawayApplication.getApplication();
        if (user == null) {
            application.resetAccessToken();
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
            finish();
        } else {
            name.setText(user.getName());
            location.setText(user.getLocation());
            url.setText(user.getURLEntity().getExpandedURL());
            description.setText(user.getDescription());
            application.displayRoundedImage(user.getOriginalProfileImageURL(), icon);
        }
    }

    @Override
    public void onLoaderReset(Loader<User> arg0) {

    }

    private class UpdateProfileTask extends AsyncTask<Void, Void, User> {
        @Override
        protected User doInBackground(Void... params) {
            try {
                User user = JustawayApplication.getApplication().getTwitter().updateProfile(
                        name.getText().toString(),
                        url.getText().toString(),
                        location.getText().toString(),
                        description.getText().toString());
                return user;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(User user) {
            // dismissProgressDialog();
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
        if (c == null) {
            return null;
        }

        c.moveToFirst();
        File path = new File(c.getString(0));
        if (!path.exists()) {
            return null;
        }

        return path;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Uri uri = data.getData();
            File file = uriToFile(uri);
            if (file != null) {
                UpdateProfileImageFragment dialog = new UpdateProfileImageFragment(file, uri);
                dialog.show(getSupportFragmentManager(), "dialog");
            }
        }
    }
}