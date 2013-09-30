package info.justaway;

import info.justaway.util.TwitterUtils;

import java.io.File;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import android.R.color;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
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
    private Button mTweetButton;
    private Button mImgButton;
    private ProgressDialog mProgressDialog;
    private Long inReplyToStatusId;
    private File imgPath;

    final Context c = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mEditText = (EditText) findViewById(R.id.status);
        mTextView = (TextView) findViewById(R.id.count);
        mTweetButton = (Button) findViewById(R.id.tweet);
        mImgButton = (Button) findViewById(R.id.img);
        mTwitter = TwitterUtils.getTwitterInstance(this);

        Intent intent = getIntent();
        String status = intent.getStringExtra("status");
        if (status != null) {
            mEditText.setText(status);
        }
        int selection = intent.getIntExtra("selection", 0);
        if (selection > 0) {
            mEditText.setSelection(selection);
        }
        inReplyToStatusId = intent.getLongExtra("inReplyToStatusId", 0);

        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            String text = intent.getData().getQueryParameter("text");
            String url = intent.getData().getQueryParameter("url");
            String hashtags = intent.getData().getQueryParameter("hashtags");
            if (text == null) {
                text = "";
            }
            if (url != null) {
                text += " " + url;
            }
            if (hashtags != null) {
                text += " #" + hashtags;
            }
            mEditText.setText(text);
        }

        if (Intent.ACTION_SEND.equals(intent.getAction())) {
            if (intent.getExtras().get(Intent.EXTRA_STREAM) != null) {
                Uri imgUri = (Uri) intent.getExtras().get(Intent.EXTRA_STREAM);
                uriToFile(imgUri);
            } else {
                String pageUri = intent.getExtras().getString(Intent.EXTRA_TEXT);
                String pageTitle = intent.getExtras().getString(Intent.EXTRA_SUBJECT);
                if (pageTitle == null) {
                    pageTitle = ""; 
                }
                if (pageUri != null) {
                    pageTitle += " " + pageUri;
                }
                mEditText.setText(pageTitle);
            }
        }

        findViewById(R.id.tweet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog("送信中！！１１１１１");
                StatusUpdate super_sugoi = new StatusUpdate(mEditText.getText().toString());
                if (inReplyToStatusId > 0) {
                    super_sugoi.setInReplyToStatusId(inReplyToStatusId);
                }
                if (imgPath != null) {
                    super_sugoi.setMedia(imgPath);
                }
                new PostTask().execute(super_sugoi);
            }
        });

        findViewById(R.id.suddenly).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String totsuzen = mEditText.getText().toString() + "\n";
                int i;
                String hito = "人";
                String hunya = "^Y";
                String ue = "";
                String shita = "";
                String gen = "";
                int j = 0;
                String gentotsu = "";

                int len = totsuzen.length();
                for (i = 0; totsuzen.charAt(i) != '\n'; i++) {
                    ue += hito;
                    shita += hunya;
                }
                int moji = i + 5;
                for (i = 0; len > i; i++) {
                    if (totsuzen.charAt(i) == '\n') {
                        gen = "＞ " + totsuzen.substring(j, i) + " ＜\n";
                        i = i + 1;
                        j = i;
                        if (moji > gen.length()) {
                            int n;
                            String as = "";
                            int a = moji - gen.length();
                            for (n = 0; a > n; n++) {
                                as = as + "　";
                            }
                            gen = gen.substring(0, gen.length() - 3) + as + " ＜\n";
                        }
                        gentotsu = gentotsu + gen;
                    }
                }
                mEditText.setText("＿" + ue + "＿\n" + gentotsu + "￣" + shita + "￣");
            }
        });

        findViewById(R.id.img).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
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
                    textColor = Color.WHITE;
                }
                mTextView.setTextColor(textColor);
                mTextView.setText(String.valueOf(length));

                // 文字数が0文字または140文字以上の時はボタンを無効
                if (s.length() == 0 || s.length() > 140) {
                    mTweetButton.setEnabled(false);
                } else {
                    mTweetButton.setEnabled(true);
                }
            }

            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Uri uri = data.getData();
            uriToFile(uri);
        }
    }

    private void uriToFile(Uri uri) {
        ContentResolver cr = getContentResolver();
        String[] columns = { MediaStore.Images.Media.DATA };
        Cursor c = cr.query(uri, columns, null, null, null);
        c.moveToFirst();
        File path = new File(c.getString(0));
        if (!path.exists()) {
            return;
        }
        this.imgPath = path;
        showToast("画像セットok");
        mImgButton.setTextColor(getResources().getColor(color.holo_blue_bright));
    }

    private class PostTask extends AsyncTask<StatusUpdate, Void, Boolean> {
        @Override
        protected Boolean doInBackground(StatusUpdate... params) {
            StatusUpdate super_sugoi = params[0];
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
        case R.id.tweet_battery:
            Intent batteryIntent = getApplicationContext().registerReceiver(null,
                    new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            int level = batteryIntent.getIntExtra("level", 0);
            int scale = batteryIntent.getIntExtra("scale", 100);
            int status = batteryIntent.getIntExtra("status", 0);
            int battery = level * 100 / scale;
            String model = Build.MODEL;

            switch (status) {
            case BatteryManager.BATTERY_STATUS_FULL:
                mEditText.setText(model + " のバッテリー残量:" + battery + "% (0゜・◡・♥​​)");
                break;
            case BatteryManager.BATTERY_STATUS_CHARGING:
                mEditText.setText(model + " のバッテリー残量:" + battery + "% 充電なう(・◡・♥​​)");
                break;
            default:
                if (level <= 10) {
                    mEditText.setText(model + " のバッテリー残量:" + battery + "% (◞‸◟)");
                } else {
                    mEditText.setText(model + " のバッテリー残量:" + battery + "% (・◡・♥​​)");
                }
                break;
            }
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
