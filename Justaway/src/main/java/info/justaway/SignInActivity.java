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

    private static final String STATE_REQUEST_TOKEN = "request_token";

    private String mCallbackURL;
    private Twitter mTwitter;
    private RequestToken mRequestToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JustawayApplication.getApplication().setTheme(this);
        setContentView(R.layout.activity_signin);

        JustawayApplication application = JustawayApplication.getApplication();

        mCallbackURL = getString(R.string.twitter_callback_url);
        mTwitter = application.getTwitterInstance();

        Typeface fontello = JustawayApplication.getFontello();
        TextView button = (TextView) findViewById(R.id.action_start_oauth);
        button.setTypeface(fontello);

        if (getIntent().getBooleanExtra("add_account", false)) {
            button.setVisibility(View.GONE);
            findViewById(R.id.connect_with_twitter).setVisibility(View.GONE);
            startOAuth();
            return;
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startOAuth();
            }
        });

        if (savedInstanceState != null) {
            mRequestToken = (RequestToken) savedInstanceState.get(STATE_REQUEST_TOKEN);
            if (mRequestToken != null) {
                button.setVisibility(View.GONE);
                findViewById(R.id.connect_with_twitter).setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mRequestToken != null) {
            outState.putSerializable(STATE_REQUEST_TOKEN, mRequestToken);
        }
    }

    private void startOAuth() {
        new AsyncTask<Void, Void, RequestToken>() {
            @Override
            protected RequestToken doInBackground(Void... params) {
                try {
                    return mTwitter.getOAuthRequestToken(mCallbackURL);
                } catch (TwitterException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(RequestToken token) {
                mRequestToken = token;
                final String url = token.getAuthorizationURL();
                if (url == null) {
                    JustawayApplication.showToast(R.string.toast_get_authorization_url_failure);
                    return;
                }
                findViewById(R.id.action_start_oauth).setVisibility(View.GONE);
                findViewById(R.id.connect_with_twitter).setVisibility(View.GONE);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        }.execute();
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        /**
//         * 初回認証後バックキーで終了して「最近使ったアプリ」から復帰するとここに来る
//         */
//        if (JustawayApplication.getApplication().hasAccessToken()) {
//            Intent intent = new Intent(this, MainActivity.class);
//            startActivity(intent);
//            finish();
//        }
//    }

    @Override
    public void onNewIntent(Intent intent) {
        if (intent == null || intent.getData() == null
                || !intent.getData().toString().startsWith(mCallbackURL)) {
            return;
        }
        String oauth_verifier = intent.getData().getQueryParameter("oauth_verifier");
        if (oauth_verifier == null || oauth_verifier.isEmpty()) {
            return;
        }
        new AsyncTask<String, Void, AccessToken>() {
            @Override
            protected AccessToken doInBackground(String... params) {
                String verifier = params[0];
                try {
                    return mTwitter.getOAuthAccessToken(mRequestToken, verifier);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(AccessToken accessToken) {
                if (accessToken != null) {
                    JustawayApplication.showToast(R.string.toast_sign_in_success);
                    successOAuth(accessToken);
                    // 認証画面で別アカウントにして認証した時に誤爆する
//                } else {
//                    JustawayApplication.showToast(R.string.toast_sign_in_failure);
                }
            }
        }.execute(oauth_verifier);
    }

    private void successOAuth(AccessToken accessToken) {
        JustawayApplication application = JustawayApplication.getApplication();
        application.setAccessToken(accessToken);

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
