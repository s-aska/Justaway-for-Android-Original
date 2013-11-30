package info.justaway;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import twitter4j.Twitter;
import twitter4j.User;

public class EditProfileActivity extends Activity {

    private Context context;
    private Twitter twitter;
    private JustawayApplication application;

    private EditText name;
    private EditText location;
    private EditText url;
    private EditText description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        context = this;

        application = JustawayApplication.getApplication();
        twitter = application.getTwitter();

        Intent intent = getIntent();
        User user = application.getUser();

        //user = application.getUser();
        name = ((EditText) findViewById(R.id.name));
        name.setText(user.getName());
        location = ((EditText) findViewById(R.id.location));
        location.setText(user.getLocation());
        url = ((EditText) findViewById(R.id.webSite));
        url.setText(user.getURL());
        description = ((EditText) findViewById(R.id.bio));
        description.setText(user.getDescription());

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
                User user = twitter.updateProfile(name.getText().toString(), location.getText().toString(),
                        url.getText().toString(), description.getText().toString());
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

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}