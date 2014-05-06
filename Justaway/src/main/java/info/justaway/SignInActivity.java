package info.justaway;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import info.justaway.widget.JustawayButton;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class SignInActivity extends Activity {

    private static final String STATE_REQUEST_TOKEN = "request_token";
    private RequestToken mRequestToken;

    @InjectView(R.id.start_oauth_button) JustawayButton mStartOauthButton;
    @InjectView(R.id.connect_with_twitter) TextView mConnectWithTwitter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JustawayApplication.getApplication().setTheme(this);
        setContentView(R.layout.activity_signin);

        ButterKnife.inject(this);

        Intent intent = getIntent();
        if (intent.getBooleanExtra("add_account", false)) {
            mStartOauthButton.setVisibility(View.GONE);
            mConnectWithTwitter.setVisibility(View.GONE);
            startOAuth();
            return;
        }

        // バックグランドプロセスが死んでいるとonNewIntentでなくonCreateからoauth_verifierが来る
        if (savedInstanceState != null) {
            mRequestToken = (RequestToken) savedInstanceState.get(STATE_REQUEST_TOKEN);
            if (mRequestToken != null) {
                if (intent.getData() != null) {
                    String oauth_verifier = intent.getData().getQueryParameter("oauth_verifier");
                    if (oauth_verifier != null && !oauth_verifier.isEmpty()) {
                        mStartOauthButton.setVisibility(View.GONE);
                        mConnectWithTwitter.setVisibility(View.GONE);
                        JustawayApplication.showProgressDialog(this, getString(R.string.progress_process));
                        new VerifyOAuthTask().execute(oauth_verifier);
                    }
                }
            }
        }
    }

    @SuppressWarnings("NullableProblems")
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mRequestToken != null) {
            outState.putSerializable(STATE_REQUEST_TOKEN, mRequestToken);
        }
    }

    @SuppressWarnings("NullableProblems")
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mRequestToken = (RequestToken) savedInstanceState.getSerializable(STATE_REQUEST_TOKEN);
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
                || !intent.getData().toString().startsWith(getString(R.string.twitter_callback_url))) {
            return;
        }
        String oauth_verifier = intent.getData().getQueryParameter("oauth_verifier");
        if (oauth_verifier == null || oauth_verifier.isEmpty()) {
            return;
        }
        JustawayApplication.showProgressDialog(this, getString(R.string.progress_process));
        new VerifyOAuthTask().execute(oauth_verifier);
    }

    private void successOAuth() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private class VerifyOAuthTask extends AsyncTask<String, Void, User> {
        @Override
        protected User doInBackground(String... params) {
            try {
                JustawayApplication application = JustawayApplication.getApplication();
                Twitter twitter = application.getTwitterManager().getTwitterInstance();
                AccessToken accessToken = twitter.getOAuthAccessToken(mRequestToken, params[0]);
                application.getAccessTokenManager().setAccessToken(accessToken);
                twitter.setOAuthAccessToken(accessToken);
                return twitter.verifyCredentials();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(User user) {
            JustawayApplication.dismissProgressDialog();
            if (user != null) {
                JustawayApplication.getApplication().getUserIconManager().addUserIconMap(user);
                JustawayApplication.showToast(R.string.toast_sign_in_success);
                successOAuth();
            }
        }
    }


    @OnClick(R.id.start_oauth_button)
    void startOAuth() {
        JustawayApplication.showProgressDialog(this, getString(R.string.progress_process));
        AsyncTask<Void, Void, RequestToken> task = new AsyncTask<Void, Void, RequestToken>() {
            @Override
            protected RequestToken doInBackground(Void... params) {
                try {
                    Twitter twitter = JustawayApplication.getApplication().getTwitterManager().getTwitterInstance();
                    return twitter.getOAuthRequestToken(getString(R.string.twitter_callback_url));
                } catch (TwitterException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(RequestToken token) {
                JustawayApplication.dismissProgressDialog();
                if (token == null) {
                    JustawayApplication.showToast(R.string.toast_connection_failure);
                    return;
                }
                final String url = token.getAuthorizationURL();
                if (url == null) {
                    JustawayApplication.showToast(R.string.toast_get_authorization_url_failure);
                    return;
                }
                mRequestToken = token;
                mStartOauthButton.setVisibility(View.GONE);
                mConnectWithTwitter.setVisibility(View.GONE);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        };
        task.execute();
    }
}
