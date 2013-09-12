package justaway.signinwithtwitter;

import twitter4j.Twitter;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PostActivity extends Activity {

    private Twitter mTwitter;
    private EditText mEditText;
    private TextView mTextView;
    private Button mButton;
    private ProgressDialog mProgressDialog;

    final Context c = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mEditText = (EditText) findViewById(R.id.status);
        mTextView = (TextView) findViewById(R.id.count);
        mButton = (Button) findViewById(R.id.tweet);
        mTwitter = TwitterUtils.getTwitterInstance(this);

        findViewById(R.id.tweet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog("送信中！！１１１１１");
                String super_sugoi = mEditText.getText().toString();
                new PostTask().execute(super_sugoi);
            }
        });

        findViewById(R.id.suddenly).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String totsuzen = mEditText.getText().toString()+"\n";
                int i;
                String hito="人";
                String hunya="^Y";
                String ue="";
                String shita="";
                String gen="";
                int j = 0;
                String gentotsu ="";

                int len = totsuzen.length();
                for(i=0;totsuzen.charAt(i)!='\n';i++) {
                    ue += hito;
                    shita += hunya;
                }
                int moji = i+5;
                for(i=0;len>i;i++){
                    if(totsuzen.charAt(i)=='\n'){
                        gen = "＞ "+totsuzen.substring(j,i)+" ＜\n";
                        i = i+1;
                        j = i;
                        if(moji>gen.length()){
                            int n;
                            String as="";
                            int a = moji-gen.length();
                            for(n=0;a>n;n++){
                                as = as+"　";    
                            }
                            gen = gen.substring(0,gen.length()-3)+as+" ＜\n";//aの分だけgenの空白を増やす   
                        }
                        gentotsu = gentotsu+gen;
                    }
                }
                mEditText.setText("＿"+ue+"＿\n"+gentotsu+"￣"+shita+"￣");

            }
        });

        // 文字数をカウントしてボタンを制御する
        mEditText.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int textColor;
                int length = 140 - s.length();
                // 140文字をオーバーした時は文字数を赤色に
                if (length < 0) {
                    textColor = Color.RED;
                } else {
                    textColor = Color.BLACK;
                }
                mTextView.setTextColor(textColor);
                mTextView.setText(String.valueOf(length));

                // 文字数が0文字または140文字以上の時はボタンを無効
                if (s.length() == 0 || s.length() > 140) {
                    mButton.setEnabled(false);
                } else {
                    mButton.setEnabled(true);
                }
            }
            public void afterTextChanged(Editable s) {
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
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
            dismissProgressDialog();
            if (success) {
                mEditText.setText("");
                finish();
            } else {
                showToast("残念~！もう一回！！");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.post_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.tweet_clear:
            mEditText.setText("");
            break;
        }
        return true;
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private void showProgressDialog(String message) {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(message);
        mProgressDialog.show();
    }

    private void dismissProgressDialog() {
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
    }
}
