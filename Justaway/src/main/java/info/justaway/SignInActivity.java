package info.justaway;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.Bind;
import butterknife.OnClick;
import info.justaway.model.AccessTokenManager;
import info.justaway.model.TwitterManager;
import info.justaway.model.UserIconManager;
import info.justaway.util.MessageUtil;
import info.justaway.util.ThemeUtil;
import info.justaway.widget.FontelloButton;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class SignInActivity extends Activity {

    private static final String STATE_REQUEST_TOKEN = "request_token";
    private RequestToken mRequestToken;

    @Bind(R.id.start_oauth_button) FontelloButton mStartOauthButton;
    @Bind(R.id.connect_with_twitter) TextView mConnectWithTwitter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeUtil.setTheme(this);
        setContentView(R.layout.activity_signin);

        ButterKnife.bind(this);

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
                        MessageUtil.showProgressDialog(this, getString(R.string.progress_process));
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
        MessageUtil.showProgressDialog(this, getString(R.string.progress_process));
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
                Twitter twitter = TwitterManager.getTwitterInstance();
                AccessToken accessToken = twitter.getOAuthAccessToken(mRequestToken, params[0]);
                AccessTokenManager.setAccessToken(accessToken);
                twitter.setOAuthAccessToken(accessToken);
                return twitter.verifyCredentials();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(User user) {
            MessageUtil.dismissProgressDialog();
            if (user != null) {
                UserIconManager.addUserIconMap(user);
                MessageUtil.showToast(R.string.toast_sign_in_success);
                successOAuth();
            }
        }
    }


    @OnClick(R.id.start_oauth_button)
    void startOAuth() {
        MessageUtil.showProgressDialog(this, getString(R.string.progress_process));
        AsyncTask<Void, Void, RequestToken> task = new AsyncTask<Void, Void, RequestToken>() {
            @Override
            protected RequestToken doInBackground(Void... params) {
                try {
                    Twitter twitter = TwitterManager.getTwitterInstance();
                    return twitter.getOAuthRequestToken(getString(R.string.twitter_callback_url));
                } catch (TwitterException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(RequestToken token) {
                MessageUtil.dismissProgressDialog();
                if (token == null) {
                    MessageUtil.showToast(R.string.toast_connection_failure);
                    return;
                }
                final String url = token.getAuthorizationURL();
                if (url == null) {
                    MessageUtil.showToast(R.string.toast_get_authorization_url_failure);
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
