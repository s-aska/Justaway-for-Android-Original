package justaway.signinwithtwitter;

import java.util.Locale;

//import twitter4j.auth.OAuthAuthorization;
//import twitter4j.auth.RequestToken;
//import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
//import android.view.View;
//import android.view.View.OnTouchListener;
//import android.widget.Button;

public class MainActivity extends Activity {

	// TODO: config file


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (!TwitterUtils.hasAccessToken(this)) {
            Intent intent = new Intent(this, TwitterOAuthActivity.class);
            startActivity(intent);
            finish();
        }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
