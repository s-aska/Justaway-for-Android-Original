package justaway.signinwithtwitter;

import twitter4j.Twitter;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class PostActivity extends Activity {

    private Twitter mTwitter;
    private EditText mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mEditText = (EditText) findViewById(R.id.status);
        mTwitter = TwitterUtils.getTwitterInstance(this);

        findViewById(R.id.tweet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String super_sugoi = mEditText.getText().toString();
                new PostTask().execute(super_sugoi);
            }
        });
    }

    private class PostTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String super_sugoi = params[0];
            try {
                mTwitter.updateStatus(super_sugoi);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                // mEditText.setText("")したいけど寝る
                showToast("ok");
            } else {
                showToast("残念~！もう一回！！");
            }
        }
    }

    public void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
