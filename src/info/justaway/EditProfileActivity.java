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
    private User user;

    private EditText name;
    private EditText location;
    private EditText url;
    private EditText description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        context = this;

        JustawayApplication application = JustawayApplication.getApplication();
        twitter = application.getTwitter();

        Intent intent = getIntent();
        user = (User) intent.getSerializableExtra("user");

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

    private class UpdateProfileTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                twitter.updateProfile(name.getText().toString(), location.getText().toString(),
                        url.getText().toString(), description.getText().toString());
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            // dismissProgressDialog();
            if (success) {
                showToast("プロフィールを保存しました");
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