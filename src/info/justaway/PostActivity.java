package info.justaway;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import info.justaway.settings.PostStockSettings;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;

public class PostActivity extends FragmentActivity {

    private static final int REQUEST_GALLERY = 1;
    private static final int REQUEST_CAMERA = 2;
    private static final int ERROR_CODE_DUPLICATE_STATUS = 187;

    private Context mContext;
    private EditText mEditText;
    private TextView mTextView;
    private Button mTweetButton;
    private Button mImgButton;
    private Long mInReplyToStatusId;
    private File mImgPath;
    private Uri mImageUri;
    private DraftFragment mDraftDialog;
    private HashtagFragment mHashtagDialog;
    private boolean mWidgetMode;
    private Spinner mSpinner;
    private PostStockSettings mPostStockSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        mContext = this;

        JustawayApplication.getApplication().warmUpUserIconMap();

        Typeface fontello = JustawayApplication.getFontello();

        mEditText = (EditText) findViewById(R.id.status);
        mTextView = (TextView) findViewById(R.id.count);
        mTweetButton = (Button) findViewById(R.id.tweet);
        mImgButton = (Button) findViewById(R.id.img);
        Button suddenlyButton = (Button) findViewById(R.id.suddenly);
        Button draftButton = (Button) findViewById(R.id.draft);
        Button hashtagButton = (Button) findViewById(R.id.hashtag);

        mTweetButton.setTypeface(fontello);
        mImgButton.setTypeface(fontello);
        suddenlyButton.setTypeface(fontello);
        draftButton.setTypeface(fontello);
        hashtagButton.setTypeface(fontello);

        registerForContextMenu(mImgButton);

        // アカウント切り替え
        AccessTokenAdapter adapter = new AccessTokenAdapter(this, R.layout.spinner_switch_account);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mSpinner = (Spinner) findViewById(R.id.switchAccount);
        mSpinner.setAdapter(adapter);

        ArrayList<AccessToken> accessTokens = JustawayApplication.getApplication().getAccessTokens();

        if (accessTokens != null) {
            int i = 0;
            for (AccessToken accessToken : accessTokens) {
                adapter.add(accessToken);

                if (JustawayApplication.getApplication().getUserId() == accessToken.getUserId()) {
                    mSpinner.setSelection(i);
                }
                i++;
            }
        }

        Intent intent = getIntent();
        mWidgetMode = intent.getBooleanExtra("widget", false);
        if (mWidgetMode) {
            setTitle(getString(R.string.widget_title_post_mode));
        } else {
            setTitle(getString(R.string.title_post));
        }

        String status = intent.getStringExtra("status");
        if (status != null) {
            mEditText.setText(status);
        }
        int selection = intent.getIntExtra("selection", 0);
        if (selection > 0) {
            mEditText.setSelection(selection);
        }
        mInReplyToStatusId = intent.getLongExtra("inReplyToStatusId", 0);

        if (intent.getData() != null) {
            String inReplyToStatusId = intent.getData().getQueryParameter("in_reply_to");
            if (inReplyToStatusId != null && inReplyToStatusId.length() > 0) {
                mInReplyToStatusId = Long.valueOf(inReplyToStatusId);
            }

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
            if (intent.getParcelableExtra(Intent.EXTRA_STREAM) != null) {
                Uri imgUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                setImage(imgUri);
            } else if (intent.getExtras() != null) {
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

        if (mEditText.getText() != null) {
            updateCount(mEditText.getText().toString());
        }

        mPostStockSettings = new PostStockSettings();
        if (mPostStockSettings.getDrafts().isEmpty()) {
            draftButton.setEnabled(false);
        }
        if (mPostStockSettings.getHashtags().isEmpty()) {
            hashtagButton.setEnabled(false);
        }

        mTweetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = mEditText.getText().toString();
                JustawayApplication.showProgressDialog(mContext, getString(R.string.progress_sending));
                StatusUpdate superSugoi = new StatusUpdate(mEditText.getText().toString());
                if (mInReplyToStatusId > 0) {
                    superSugoi.setInReplyToStatusId(mInReplyToStatusId);
                }
                if (mImgPath != null) {
                    superSugoi.setMedia(mImgPath);
                }

                if (text.contains("#")) {
                    String hashtag = text.substring(text.indexOf("#"));

                    boolean exist = false;
                    for (String existHashtag : mPostStockSettings.getHashtags()) {
                        if (existHashtag.equals(hashtag)) {
                            exist = true;
                            break;
                        }
                    }
                    if (!exist) {
                        mPostStockSettings.addHashtag(hashtag);
                    }

                }

                new PostTask().execute(superSugoi);
            }
        });

        suddenlyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = mEditText.getText().toString();
                int selectStart = mEditText.getSelectionStart();
                int selectEnd = mEditText.getSelectionEnd();

                String totsuzen;
                if (selectStart != selectEnd) {
                    totsuzen = text.substring(selectStart, selectEnd) + "\n";
                } else {
                    totsuzen = text + "\n";
                }

                int i;
                String top = "";
                String under = "";
                int j = 0;
                String generateTotsu = "";

                // 改行文字がある場所を見つけて上と下を作る
                for (i = 0; totsuzen.charAt(i) != '\n'; i++) {
                    int codeunit = totsuzen.codePointAt(i);
                    if (0xffff < codeunit) {
                        i++;
                    }
                    top += "人";
                    under += "^Y";
                }
                // 突然死したいテキストの文字をひとつづつ見る
                for (i = 0; totsuzen.length() > i; i++) {
                    // 一文字取り出して改行文字なのかチェック
                    if (totsuzen.charAt(i) == '\n') {
                        String gen = "＞ " + totsuzen.substring(j, i) + " ＜\n";
                        i++;
                        j = i;
                        generateTotsu = generateTotsu.concat(gen);
                    }
                }
                if (selectStart != selectEnd) {
                    mEditText.setText(text.substring(0, selectStart) + "＿" + top + "＿\n" + generateTotsu + "￣" + under + "￣" + text.substring(selectEnd));
                } else {
                    mEditText.setText("＿" + top + "＿\n" + generateTotsu + "￣" + under + "￣");
                }
            }
        });

        mImgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.showContextMenu();
            }
        });

        draftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDraftDialog = new DraftFragment();
                mDraftDialog.show(getSupportFragmentManager(), "dialog");
            }
        });

        hashtagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHashtagDialog = new HashtagFragment();
                mHashtagDialog.show(getSupportFragmentManager(), "dialog");
            }
        });

        // 文字数をカウントしてボタンを制御する
        mEditText.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateCount(s.toString());
            }

            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle(getString(R.string.context_menu_title_photo_method));
        menu.add(0, REQUEST_GALLERY, 0, R.string.context_menu_photo_gallery);
        menu.add(0, REQUEST_CAMERA, 0, R.string.context_menu_photo_camera);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case REQUEST_GALLERY:
                intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_GALLERY);
                return true;
            case REQUEST_CAMERA:
                String filename = System.currentTimeMillis() + ".jpg";
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, filename);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                mImageUri = getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                intent = new Intent();
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                startActivityForResult(intent, REQUEST_CAMERA);
                return true;
            default:
                return true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_GALLERY) {
                setImage(data.getData());
            } else if (requestCode == REQUEST_CAMERA) {
                setImage(mImageUri);
            }
        }
    }

    private void setImage(Uri uri) {
        ContentResolver cr = getContentResolver();
        String[] columns = {MediaStore.Images.Media.DATA};
        Cursor c = cr.query(uri, columns, null, null, null);
        assert c != null;
        c.moveToFirst();
        String fileName = c.getString(0);
        if (fileName == null) {
            JustawayApplication.showToast(getString(R.string.toast_set_image_failure));
            return;
        }
        File path = new File(fileName);

        if (!path.exists()) {
            return;
        }
        this.mImgPath = path;
        JustawayApplication.showToast(R.string.toast_set_image_success);
        mImgButton.setTextColor(getResources().getColor(R.color.holo_blue_bright));
        mTweetButton.setEnabled(true);
    }

    private void updateCount(String str) {
        int textColor;
        int length = 140 - str.codePointCount(0, str.length());
        // 140文字をオーバーした時は文字数を赤色に
        if (length < 0) {
            textColor = Color.RED;
        } else {
            textColor = Color.WHITE;
        }
        mTextView.setTextColor(textColor);
        mTextView.setText(String.valueOf(length));

        if (str.codePointCount(0, str.length()) == 0
                || str.codePointCount(0, str.length()) > 140) {
            // 文字数が0文字または140文字以上の時はボタンを無効
            if (mImgPath != null) {
                mTweetButton.setEnabled(true);
            } else {
                mTweetButton.setEnabled(false);
            }
        } else {
            mTweetButton.setEnabled(true);
        }
    }

    private class PostTask extends AsyncTask<StatusUpdate, Void, TwitterException> {
        @Override
        protected TwitterException doInBackground(StatusUpdate... params) {
            StatusUpdate statusUpdate = params[0];
            try {
                Twitter twitter = JustawayApplication.getApplication().getTwitterInstance();
                twitter.setOAuthAccessToken((AccessToken) mSpinner.getSelectedItem());
                twitter.updateStatus(statusUpdate);
                return null;
            } catch (TwitterException e) {
                e.printStackTrace();

                return e;
            }
        }

        @Override
        protected void onPostExecute(TwitterException e) {
            JustawayApplication.dismissProgressDialog();
            if (e == null) {
                mEditText.setText("");
                if (!mWidgetMode) {
                    finish();
                } else {
                    mImgPath = null;
                    mImgButton.setTextColor(getResources().getColor(android.R.color.secondary_text_dark));
                    mTweetButton.setEnabled(false);
                }
            } else if (e.getErrorCode() == ERROR_CODE_DUPLICATE_STATUS) {
                JustawayApplication.showToast(getString(R.string.toast_update_status_already));
            } else {
                JustawayApplication.showToast(R.string.toast_update_status_failure);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.post, menu);
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mEditText.getText() != null && mEditText.getText().length() != 0) {
                new AlertDialog.Builder(PostActivity.this)
                        .setTitle(R.string.confirm_save_draft)
                        .setPositiveButton(
                                R.string.button_save,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // 下書きとして保存する
                                        mPostStockSettings.addDraft(mEditText.getText().toString());

                                        finish();
                                    }
                                })
                        .setNegativeButton(
                                R.string.button_destroy,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                })
                        .show();
            } else {
                finish();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.tweet_clear:
                mEditText.setText("");
                break;
            case R.id.tweet_battery:
                Intent batteryIntent = registerReceiver(null,
                        new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

                if (batteryIntent == null) {
                    break;
                }
                int level = batteryIntent.getIntExtra("level", 0);
                int scale = batteryIntent.getIntExtra("scale", 100);
                int status = batteryIntent.getIntExtra("status", 0);
                int battery = level * 100 / scale;
                String model = Build.MODEL;

                switch (status) {
                    case BatteryManager.BATTERY_STATUS_FULL:
                        mEditText.setText(model + " のバッテリー残量：" + battery + "% (0゜・◡・♥​​)");
                        break;
                    case BatteryManager.BATTERY_STATUS_CHARGING:
                        mEditText.setText(model + " のバッテリー残量：" + battery + "% 充電なう(・◡・♥​​)");
                        break;
                    default:
                        if (level <= 14) {
                            mEditText.setText(model + " のバッテリー残量：" + battery + "% (◞‸◟)");
                        } else {
                            mEditText.setText(model + " のバッテリー残量：" + battery + "% (・◡・♥​​)");
                        }
                        break;
                }
                break;
        }
        return true;
    }

    public class DraftFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            Activity activity = getActivity();
            Dialog dialog = new Dialog(activity);
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
            dialog.setContentView(R.layout.list);
            ListView listView = (ListView) dialog.findViewById(R.id.list);

            // 下書きをViewに描写するアダプター
            DraftAdapter adapter = new DraftAdapter(activity, R.layout.row_word);
            listView.setAdapter(adapter);

            PostStockSettings postStockSettings = new PostStockSettings();

            for (String draft : postStockSettings.getDrafts()) {
                adapter.add(draft);
            }

            return dialog;
        }
    }

    public class HashtagFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            Activity activity = getActivity();
            Dialog dialog = new Dialog(activity);
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
            dialog.setContentView(R.layout.list);
            ListView listView = (ListView) dialog.findViewById(R.id.list);

            // ハッシュタグをViewに描写するアダプター
            HashtagAdapter adapter = new HashtagAdapter(activity, R.layout.row_word);
            listView.setAdapter(adapter);

            PostStockSettings postStockSettings = new PostStockSettings();

            for (String hashtag : postStockSettings.getHashtags()) {
                adapter.add(hashtag);
            }

            return dialog;
        }
    }

    public class AccessTokenAdapter extends ArrayAdapter<AccessToken> {

        private ArrayList<AccessToken> mAccessTokenList = new ArrayList<AccessToken>();
        private LayoutInflater mInflater;
        private int mLayout;

        public AccessTokenAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
            this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.mLayout = textViewResourceId;
        }

        @Override
        public void add(AccessToken accessToken) {
            super.add(accessToken);
            mAccessTokenList.add(accessToken);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            // ビューを受け取る
            View view = convertView;
            if (view == null) {
                // 受け取ったビューがnullなら新しくビューを生成
                view = mInflater.inflate(this.mLayout, null);
            }

            AccessToken accessToken = mAccessTokenList.get(position);

            assert view != null;
            view.setPadding(16, 0, 0, 0);
            ImageView icon = (ImageView) view.findViewById(R.id.icon);
            JustawayApplication.getApplication().displayUserIcon(accessToken.getUserId(), icon);
            ((TextView) view.findViewById(R.id.screen_name)).setText(accessToken.getScreenName());

            return view;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            // ビューを受け取る
            View view = convertView;
            if (view == null) {
                // 受け取ったビューがnullなら新しくビューを生成
                view = mInflater.inflate(this.mLayout, null);
            }

            AccessToken accessToken = mAccessTokenList.get(position);

            assert view != null;
            ImageView icon = (ImageView) view.findViewById(R.id.icon);
            JustawayApplication.getApplication().displayUserIcon(accessToken.getUserId(), icon);
            ((TextView) view.findViewById(R.id.screen_name)).setText(accessToken.getScreenName());

            return view;
        }
    }


    public class DraftAdapter extends ArrayAdapter<String> {

        private ArrayList<String> mDraftLists = new ArrayList<String>();
        private LayoutInflater mInflater;
        private int mLayout;

        public DraftAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
            this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.mLayout = textViewResourceId;
        }

        @Override
        public void add(String draft) {
            super.add(draft);
            mDraftLists.add(draft);
        }

        public void remove(int position) {
            super.remove(mDraftLists.remove(position));
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            // ビューを受け取る
            View view = convertView;
            if (view == null) {
                // 受け取ったビューがnullなら新しくビューを生成
                view = mInflater.inflate(this.mLayout, null);
            }

            final String draft = mDraftLists.get(position);

            assert view != null;
            ((TextView) view.findViewById(R.id.word)).setText(draft);
            ((TextView) view.findViewById(R.id.trash)).setTypeface(JustawayApplication.getFontello());

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mEditText.setText(draft);
                    mDraftDialog.dismiss();
                    mDraftLists.remove(position);
                    mPostStockSettings.removeDraft(draft);
                }
            });

            view.findViewById(R.id.trash).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    remove(position);
                    mPostStockSettings.removeDraft(draft);
                }
            });
            return view;
        }
    }

    public class HashtagAdapter extends ArrayAdapter<String> {

        private ArrayList<String> mHashtagLists = new ArrayList<String>();
        private LayoutInflater mInflater;
        private int mLayout;

        public HashtagAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
            this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.mLayout = textViewResourceId;
        }

        @Override
        public void add(String hashtag) {
            super.add(hashtag);
            mHashtagLists.add(hashtag);
        }

        public void remove(int position) {
            super.remove(mHashtagLists.remove(position));
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            // ビューを受け取る
            View view = convertView;
            if (view == null) {
                // 受け取ったビューがnullなら新しくビューを生成
                view = mInflater.inflate(this.mLayout, null);
            }

            final String hashtag = mHashtagLists.get(position);

            assert view != null;
            ((TextView) view.findViewById(R.id.word)).setText(hashtag);
            ((TextView) view.findViewById(R.id.trash)).setTypeface(JustawayApplication.getFontello());

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mEditText.getText() != null) {
                        mEditText.setText(mEditText.getText().toString().concat(" ".concat(hashtag)));
                        mHashtagDialog.dismiss();
                    }
                }
            });

            view.findViewById(R.id.trash).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    remove(position);
                    mPostStockSettings.removeHashtag(hashtag);
                }
            });
            return view;
        }
    }
}
