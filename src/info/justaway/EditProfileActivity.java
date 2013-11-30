package info.justaway;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

import twitter4j.Twitter;
import twitter4j.User;

public class EditProfileActivity extends FragmentActivity {

    private Context context;
    private Twitter twitter;
    private JustawayApplication application;

    private EditText name;
    private EditText location;
    private EditText url;
    private EditText description;
    private ImageView icon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        context = this;

        application = JustawayApplication.getApplication();
        twitter = application.getTwitter();
        User user = application.getUser();

        name = ((EditText) findViewById(R.id.name));
        name.setText(user.getName());
        location = ((EditText) findViewById(R.id.location));
        location.setText(user.getLocation());
        url = ((EditText) findViewById(R.id.webSite));
        url.setText(user.getURLEntity().getExpandedURL());
        description = ((EditText) findViewById(R.id.bio));
        description.setText(user.getDescription());
        icon = ((ImageView) findViewById(R.id.icon));
        application.displayRoundedImage(user.getBiggerProfileImageURL(), icon);

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
    }

    private class UpdateProfileTask extends AsyncTask<Void, Void, User> {
        @Override
        protected User doInBackground(Void... params) {
            try {
                User user = twitter.updateProfile(name.getText().toString(), url.getText().toString(),
                        location.getText().toString(), description.getText().toString());
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
                showToast("プロフィールを保存しました");
                application.setUser(user);
                finish();
            } else {
                showToast("プロフィールの保存に失敗しました");
            }
        }
    }

    private File uriToFile(Uri uri) {
        ContentResolver cr = getContentResolver();
        String[] columns = {MediaStore.Images.Media.DATA};
        Cursor c = cr.query(uri, columns, null, null, null);
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

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}