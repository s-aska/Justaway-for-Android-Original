package info.justaway;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.EditText;

import twitter4j.Twitter;
import twitter4j.User;

public class EditProfileActivity extends Activity {

    private Context context;
    private Twitter twitter;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        context = this;

        JustawayApplication application = JustawayApplication.getApplication();
        twitter = application.getTwitter();

        user = application.getUser();
        ((EditText) findViewById(R.id.name)).setText(user.getName());
        ((EditText) findViewById(R.id.location)).setText(user.getLocation());
        ((EditText) findViewById(R.id.webSite)).setText(user.getURL());
        ((EditText) findViewById(R.id.bio)).setText(user.getDescription());
    }

}