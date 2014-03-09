package info.justaway;


import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import info.justaway.plugin.TwiccaPlugin;
import info.justaway.settings.PostStockSettings;
import info.justaway.task.UpdateStatusTask;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;

public class PostActivity extends FragmentActivity {

    private static final int REQUEST_GALLERY = 1;
    private static final int REQUEST_CAMERA = 2;
    private static final int REQUEST_TWICCA = 3;
    private static final int OPTION_MENU_GROUP_TWICCA = 1;
    private static final int ERROR_CODE_DUPLICATE_STATUS = 187;
    private static final Pattern URL_PATTERN = Pattern.compile("(http://|https://){1}[\\w\\.\\-/:\\#\\?\\=\\&\\;\\%\\~\\+]+");

    private Context mContext;
    private EditText mEditText;
    private TextView mTextView;
    private Button mTweetButton;
    private Button mImgButton;
    private Long mInReplyToStatusId;
    private File mImgPath;
    private Uri mImageUri;
    private AlertDialog mDraftDialog;
    private AlertDialog mHashtagDialog;
    private boolean mWidgetMode;
    private Spinner mSpinner;
    private PostStockSettings mPostStockSettings;
    private TextView mTitle;
    private TextView mUndoButton;
    private ArrayList<String> mTextHistory = new ArrayList<String>();
    private List<ResolveInfo> mTwiccaPlugins;

    @SuppressWarnings("MagicConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        mContext = this;

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            int options = actionBar.getDisplayOptions();
            if ((options & ActionBar.DISPLAY_SHOW_CUSTOM) == ActionBar.DISPLAY_SHOW_CUSTOM) {
                actionBar.setDisplayOptions(options ^ ActionBar.DISPLAY_SHOW_CUSTOM);
            } else {
                actionBar.setDisplayOptions(options | ActionBar.DISPLAY_SHOW_CUSTOM);
                if (actionBar.getCustomView() == null) {
                    actionBar.setCustomView(R.layout.action_bar_post);
                    ViewGroup group = (ViewGroup) actionBar.getCustomView();
                    mTitle = (TextView) group.findViewById(R.id.title);
                    mUndoButton = (TextView) group.findViewById(R.id.undo);
                    mUndoButton.setTypeface(JustawayApplication.getFontello());
                    mUndoButton.setEnabled(false);
                    mUndoButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String text = mTextHistory.get(mTextHistory.size() - 1);
                            if (mEditText.getText() != null && text.equals(mEditText.getText().toString())) {
                                mTextHistory.remove(mTextHistory.size() - 1);
                                if (mTextHistory.size() > 0) {
                                    text = mTextHistory.get(mTextHistory.size() - 1);
                                }
                            }
                            mEditText.setText(text);
                            mEditText.setSelection(mEditText.length());
                            mTextHistory.remove(mTextHistory.size() - 1);
                            if (mTextHistory.size() > 0 && text.equals(mEditText.getText().toString())) {
                                mTextHistory.remove(mTextHistory.size() - 1);
                            }
                            mUndoButton.setEnabled(mTextHistory.size() > 0);
                        }
                    });
                }
            }
        }

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
            mTitle.setText(getString(R.string.widget_title_post_mode));
        } else {
            mTitle.setText(getString(R.string.title_post));
            if (actionBar != null) {
                actionBar.setHomeButtonEnabled(true);
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }

        String status = intent.getStringExtra("status");
        if (status != null) {
            mEditText.setText(status);
        }

        int selection_start = intent.getIntExtra("selection", 0);
        if (selection_start > 0) {
            int selection_stop = intent.getIntExtra("selection_stop", 0);
            if (selection_stop > 0) {
                mEditText.setSelection(selection_start, selection_stop);
            } else {
                mEditText.setSelection(selection_start);
            }
        }

        Status inReplyToStatus = (Status) intent.getSerializableExtra("inReplyToStatus");
        if (inReplyToStatus != null) {
            if (inReplyToStatus.getRetweetedStatus() != null) {
                inReplyToStatus = inReplyToStatus.getRetweetedStatus();
            }
            mInReplyToStatusId = inReplyToStatus.getId();
            JustawayApplication.getApplication().displayRoundedImage(inReplyToStatus.getUser().getProfileImageURL(),
                    ((ImageView) findViewById(R.id.in_reply_to_user_icon)));

            TextView textView = (TextView) findViewById(R.id.in_reply_to_status);
            textView.setText(inReplyToStatus.getText());

            // スクロール可能にするのに必要
            textView.setMovementMethod(ScrollingMovementMethod.getInstance());
        } else {
            findViewById(R.id.in_reply_to_status).setVisibility(View.GONE);
        }

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
                JustawayApplication.showProgressDialog(mContext, getString(R.string.progress_sending));
                StatusUpdate statusUpdate = new StatusUpdate(mEditText.getText().toString());
                if (mInReplyToStatusId != null) {
                    statusUpdate.setInReplyToStatusId(mInReplyToStatusId);
                }
                if (mImgPath != null) {
                    statusUpdate.setMedia(mImgPath);
                }

                UpdateStatusTask task = new UpdateStatusTask((AccessToken) mSpinner.getSelectedItem()) {
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
                };
                task.execute(statusUpdate);
            }
        });

        suddenlyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = mEditText.getText().toString();
                int selectStart = mEditText.getSelectionStart();
                int selectEnd = mEditText.getSelectionEnd();

                // 突然の死対象のテキストを取得
                String targetText;
                if (selectStart != selectEnd) {
                    targetText = text.substring(selectStart, selectEnd) + "\n";
                } else {
                    targetText = text + "\n";
                }

                String top = "";
                String under = "";
                Paint paint = new Paint();
                float maxTextWidth = 0;

                // 対象のテキストの最大文字列幅を取得
                String[] lines = targetText.split("\\n");
                for (String line : lines) {
                    if (paint.measureText(line) > maxTextWidth) {
                        maxTextWidth = paint.measureText(line);
                    }
                }

                // 上と下を作る
                int i;
                for (i = 0; (maxTextWidth / 12) > i; i++) {
                    top += "人";
                }
                for (i = 0; (maxTextWidth / 13) > i; i++) {
                    under += "^Y";
                }

                String suddenly = "";
                for (String line : lines) {
                    float spaceWidth = maxTextWidth - paint.measureText(line);
                    // maxとくらべて13以上差がある場合はスペースを挿入して調整する
                    if (spaceWidth >= 12) {
                        int spaceNumber = (int) spaceWidth / 12;
                        for (i = 0; i < spaceNumber; i++) {
                            line += "　";
                        }
                        if ((spaceWidth % 12) >= 6) {
                            line += "　";
                        }
                    }
                    suddenly = suddenly.concat("＞ " + line + " ＜\n");
                }

                if (selectStart != selectEnd) {
                    mEditText.setText(text.substring(0, selectStart) + "＿" + top + "＿\n" + suddenly + "￣" + under + "￣" + text.substring(selectEnd));
                } else {
                    mEditText.setText("＿" + top + "＿\n" + suddenly + "￣" + under + "￣");
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
                View view = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.list, null);
                assert view != null;
                ListView listView = (ListView) view.findViewById(R.id.list);

                // 下書きをViewに描写するアダプター
                final DraftAdapter adapter = new DraftAdapter(mContext, R.layout.row_word);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        String draft = adapter.getItem(i);
                        mEditText.setText(draft);
                        mDraftDialog.dismiss();
                        adapter.remove(i);
                        mPostStockSettings.removeDraft(draft);
                    }
                });

                PostStockSettings postStockSettings = new PostStockSettings();

                for (String draft : postStockSettings.getDrafts()) {
                    adapter.add(draft);
                }
                mDraftDialog = new AlertDialog.Builder(mContext)
                        .setTitle(R.string.dialog_title_draft)
                        .setView(view)
                        .show();
            }
        });

        hashtagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                View view = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.list, null);
                assert view != null;
                ListView listView = (ListView) view.findViewById(R.id.list);

                // ハッシュタグをViewに描写するアダプター
                final HashtagAdapter adapter = new HashtagAdapter(mContext, R.layout.row_word);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        String hashtag = adapter.getItem(i);
                        if (mEditText.getText() != null) {
                            mEditText.setText(mEditText.getText().toString().concat(" ".concat(hashtag)));
                            mHashtagDialog.dismiss();
                        }
                    }
                });

                PostStockSettings postStockSettings = new PostStockSettings();

                for (String hashtag : postStockSettings.getHashtags()) {
                    adapter.add(hashtag);
                }
                mHashtagDialog = new AlertDialog.Builder(mContext)
                        .setTitle(R.string.dialog_title_hashtag)
                        .setView(view)
                        .show();
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
                // 直近のと一緒なら保存しない
                if (mTextHistory.size() == 0 || !s.toString().equals(mTextHistory.get(mTextHistory.size() - 1))) {
                    mTextHistory.add(s.toString());
                }
                mUndoButton.setEnabled(mTextHistory.size() > 0);
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
            } else if (requestCode == REQUEST_TWICCA) {
                mEditText.setText(data.getStringExtra(Intent.EXTRA_TEXT));
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
        int length = str.codePointCount(0, str.length());

        // 短縮URLを考慮
        Matcher matcher = URL_PATTERN.matcher(str);
        while (matcher.find()) {
            length = length - matcher.group().length() + 22;
            if (matcher.group().contains("https://")) ++length;
        }

        length = 140 - length;
        // 140文字をオーバーした時は文字数を赤色に
        if (length < 0) {
            textColor = Color.RED;
        } else {
            textColor = Color.WHITE;
        }
        mTextView.setTextColor(textColor);
        mTextView.setText(String.valueOf(length));

        if (length == 0 || length > 140) {
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
                                }
                        )
                        .setNegativeButton(
                                R.string.button_destroy,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                }
                        )
                        .show();
            } else {
                finish();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.post, menu);
        // twiccaプラグインを読む
        if (mTwiccaPlugins == null) {
            mTwiccaPlugins = TwiccaPlugin.getResolveInfo(this.getPackageManager(),
                    TwiccaPlugin.TWICCA_ACTION_EDIT_TWEET);
        }
        if (!mTwiccaPlugins.isEmpty()) {
            PackageManager pm = this.getPackageManager();
            int i = 0;
            for (ResolveInfo resolveInfo : mTwiccaPlugins) {
                if (pm == null || resolveInfo.activityInfo == null) {
                    continue;
                }
                String label = (String) resolveInfo.activityInfo.loadLabel(pm);
                if (label == null) {
                    continue;
                }
                menu.add(OPTION_MENU_GROUP_TWICCA, i, 100, label);
                i++;
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getGroupId() == OPTION_MENU_GROUP_TWICCA) {
            ResolveInfo resolveInfo = mTwiccaPlugins.get(item.getItemId());
            if (resolveInfo.activityInfo != null && mEditText.getText() != null) {
                Intent intent = TwiccaPlugin.createIntentEditTweet(
                        "", mEditText.getText().toString(), "", 0, resolveInfo.activityInfo.packageName,
                        resolveInfo.activityInfo.name);
                startActivityForResult(intent, REQUEST_TWICCA);
            }
            return true;
        }
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
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
