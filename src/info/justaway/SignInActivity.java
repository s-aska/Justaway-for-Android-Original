package info.justaway;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class SignInActivity extends Activity {

    private String mCallbackURL;
    private Twitter mTwitter;
    private RequestToken mRequestToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        JustawayApplication application = JustawayApplication.getApplication();

        mCallbackURL = getString(R.string.twitter_callback_url);
        mTwitter = application.getTwitter();

        Typeface fontello = JustawayApplication.getFontello();
        TextView button = (TextView) findViewById(R.id.action_start_oauth);
        button.setTypeface(fontello);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAuthorize();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        /**
         * 初回認証後バックキーで終了して「最近使ったアプリ」から復帰するとここに来る
         */
        if (JustawayApplication.getApplication().hasAccessToken()) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    /**
     * OAuth認証（厳密には認可）を開始します。
     */
    private void startAuthorize() {
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    mRequestToken = mTwitter.getOAuthRequestToken(mCallbackURL);
                    return mRequestToken.getAuthorizationURL();
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String url) {
                if (url == null) return;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        };
        task.execute();
    }

    @Override
    public void onNewIntent(Intent intent) {
        if (intent == null || intent.getData() == null
                || !intent.getData().toString().startsWith(mCallbackURL)) {
            return;
        }
        String verifier = intent.getData().getQueryParameter("oauth_verifier");
        new GetAccessTokenTask().execute(verifier);
    }

    private class GetAccessTokenTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String verifier = params[0];
            try {
                AccessToken accessToken = mTwitter.getOAuthAccessToken(mRequestToken, verifier);
                successOAuth(accessToken);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                JustawayApplication.showToast(R.string.toast_sign_in_success);
            } else {
                JustawayApplication.showToast(R.string.toast_sign_in_failure);
            }
        }
    }

    private void successOAuth(AccessToken accessToken) {
        JustawayApplication application = JustawayApplication.getApplication();
        application.setAccessToken(accessToken);
        application.setUserId(accessToken.getUserId());
        application.setScreenName(accessToken.getScreenName());
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
