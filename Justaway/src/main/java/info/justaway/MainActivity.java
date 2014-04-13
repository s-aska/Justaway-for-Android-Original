package info.justaway;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.greenrobot.event.EventBus;
import info.justaway.adapter.MainPagerAdapter;
import info.justaway.adapter.UserSearchAdapter;
import info.justaway.event.AlertDialogEvent;
import info.justaway.event.NewRecordEvent;
import info.justaway.event.action.AccountChangeEvent;
import info.justaway.event.action.AccountChangePostEvent;
import info.justaway.event.action.EditorEvent;
import info.justaway.event.action.SeenTopEvent;
import info.justaway.event.connection.CleanupEvent;
import info.justaway.event.connection.ConnectEvent;
import info.justaway.event.connection.DisconnectEvent;
import info.justaway.fragment.main.BaseFragment;
import info.justaway.fragment.main.DirectMessagesFragment;
import info.justaway.fragment.main.InteractionsFragment;
import info.justaway.fragment.main.TimelineFragment;
import info.justaway.fragment.main.UserListFragment;
import info.justaway.task.DestroyDirectMessageTask;
import info.justaway.task.SendDirectMessageTask;
import info.justaway.task.UpdateStatusTask;
import info.justaway.util.TwitterUtil;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;

public class MainActivity extends FragmentActivity {

    private static final int REQUEST_ACCOUNT_SETTING = 200;
    private static final int REQUEST_SETTINGS = 300;
    private static final int REQUEST_TAB_SETTINGS = 400;
    private static final int ERROR_CODE_DUPLICATE_STATUS = 187;
    private static final long TAB_ID_TIMELINE = -1L;
    private static final long TAB_ID_INTERACTIONS = -2L;
    private static final long TAB_ID_DIRECT_MESSAGE = -3L;
    private static final Pattern USERLIST_PATTERN = Pattern.compile("^(@[a-zA-Z0-9_]+)/(.*)$");
    private JustawayApplication mApplication;
    private MainPagerAdapter mMainPagerAdapter;
    private ViewPager mViewPager;
    private ProgressDialog mProgressDialog;
    private TextView mTitle;
    private TextView mSubTitle;
    private TextView mSignalButton;
    private LinearLayout mNormalLayout;
    private AutoCompleteTextView mSearchText;
    private Status mInReplyToStatus;
    private ActionBarDrawerToggle mDrawerToggle;
    private Activity mActivity;
    private AccessTokenAdapter mAccessTokenAdapter;
    private AccessToken mSwitchAccessToken;

    public void setInReplyToStatus(Status inReplyToStatus) {
        this.mInReplyToStatus = inReplyToStatus;
    }

    /**
     * ActionBarでCustomView使ってるので自分で再実装
     */
    @Override
    public void setTitle(CharSequence title) {
        if (mTitle != null) {
            Matcher matcher = USERLIST_PATTERN.matcher(title);
            if (matcher.find()) {
                mTitle.setText(matcher.group(2));
                mSubTitle.setText(matcher.group(1));
            } else {
                mTitle.setText(title);
                mSubTitle.setText("@" + mApplication.getScreenName());
            }
        }
    }

    @Override
    public void setTitle(int titleId) {
        setTitle(getString(titleId));
    }

    @SuppressWarnings("MagicConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApplication = JustawayApplication.getApplication();
        mApplication.setTheme(this);
        mActivity = this;

        // クイックモード時に起動と同時にキーボードが出現するのを抑止
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            // アプリアイコンのクリックを有効化
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);

            int options = actionBar.getDisplayOptions();
            if ((options & ActionBar.DISPLAY_SHOW_CUSTOM) == ActionBar.DISPLAY_SHOW_CUSTOM) {
                actionBar.setDisplayOptions(options ^ ActionBar.DISPLAY_SHOW_CUSTOM);
            } else {
                actionBar.setDisplayOptions(options | ActionBar.DISPLAY_SHOW_CUSTOM);
                if (actionBar.getCustomView() == null) {
                    actionBar.setCustomView(R.layout.action_bar_main);
                    ViewGroup group = (ViewGroup) actionBar.getCustomView();
                    mTitle = (TextView) group.findViewById(R.id.title);
                    mSubTitle = (TextView) group.findViewById(R.id.sub_title);

                    mNormalLayout = (LinearLayout) group.findViewById(R.id.normal_layout);
                    mSearchText = (AutoCompleteTextView) findViewById(R.id.search_text);
                    mSearchText.setAdapter(new UserSearchAdapter(this, R.layout.row_auto_complete));
                    mSearchText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            if (mSearchText.getText() == null) {
                                return;
                            }
                            Intent intent = null;
                            String searchWord = mSearchText.getText().toString();
                            switch (i) {
                                case 0:
                                    intent = new Intent(mActivity, SearchActivity.class);
                                    intent.putExtra("query", searchWord);
                                    break;
                                case 1:
                                    intent = new Intent(mActivity, UserSearchActivity.class);
                                    intent.putExtra("query", searchWord);
                                    break;
                                case 2:
                                    intent = new Intent(mActivity, ProfileActivity.class);
                                    intent.putExtra("screenName", searchWord);
                                    break;
                            }
                            startActivity(intent);
                        }
                    });

                    TextView searchButton = (TextView) group.findViewById(R.id.search);
                    searchButton.setTypeface(JustawayApplication.getFontello());
                    searchButton.setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mDrawerToggle.setDrawerIndicatorEnabled(false);
                                    mNormalLayout.setVisibility(View.GONE);
                                    mSearchText.setVisibility(View.VISIBLE);
                                    mSearchText.setText("");
                                    mSearchText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                                        public void onFocusChange(View v, boolean hasFocus) {
                                            if (!hasFocus) {
                                                return;
                                            }
                                            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                                                    .showSoftInput(v, InputMethodManager.SHOW_FORCED);
                                            mSearchText.setOnFocusChangeListener(null);
                                        }
                                    });
                                    mSearchText.requestFocus();
                                }
                            }
                    );

                    mSignalButton = (TextView) group.findViewById(R.id.signal);
                    mSignalButton.setTypeface(JustawayApplication.getFontello());
                    mSignalButton.setOnClickListener(
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    final boolean turnOn = !mApplication.getStreamingMode();
                                    DialogFragment dialog = StreamingSwitchDialogFragment.newInstance(turnOn);
                                    dialog.show(getSupportFragmentManager(), "dialog");
                                }
                            }
                    );
                }
            }
        }

        setContentView(R.layout.activity_main);
        int drawer = mApplication.getThemeName().equals("black") ? R.drawable.ic_dark_drawer : R.drawable.ic_dark_drawer;

        // DrawerLayout
        final DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                drawer, R.string.open, R.string.close) {

            public void onDrawerClosed(View view) {
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu(); //
            }
        };

        // アカウント切り替え
        ListView drawerList = (ListView) findViewById(R.id.account_list);
        mAccessTokenAdapter = new AccessTokenAdapter(this, R.layout.row_switch_account);
        ArrayList<AccessToken> accessTokens = mApplication.getAccessTokens();
        if (accessTokens != null) {
            for (AccessToken accessToken : accessTokens) {
                mAccessTokenAdapter.add(accessToken);
            }
        }

        LayoutInflater inflater = getLayoutInflater();
        @SuppressLint("InflateParams")
        View footer = inflater.inflate(R.layout.drawer_menu, null, false);
        assert footer != null;
        footer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AccountSettingActivity.class);
                startActivityForResult(intent, REQUEST_ACCOUNT_SETTING);
            }
        });
        drawerList.addFooterView(footer, null, true);

        drawerList.setAdapter(mAccessTokenAdapter);
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                AccessToken accessToken = mAccessTokenAdapter.getItem(i);
                if (mApplication.getUserId() != accessToken.getUserId()) {
                    mApplication.switchAccessToken(accessToken);
                    mAccessTokenAdapter.notifyDataSetChanged();
                }
                mDrawerLayout.closeDrawer(findViewById(R.id.left_drawer));
            }
        });
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        setTitle(R.string.title_main);

        // クイックモード時に起動と同時に入力エリアにフォーカスするのを抑止
        findViewById(R.id.main).requestFocus();

        // アクセストークンがない場合に認証用のアクティビティを起動する
        if (!mApplication.hasAccessToken()) {
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setup();

        /**
         * 違うタブだったら移動、同じタブだったら最上部にスクロールという美しい実装
         * ActionBarのタブに頼っていない為、自力でsetCurrentItemでタブを動かしている
         * タブの切替がスワイプだけで良い場合はこの処理すら不要
         */
        Typeface fontello = JustawayApplication.getFontello();
        Button tweet = (Button) findViewById(R.id.action_tweet);
        final Button send = (Button) findViewById(R.id.send);
        tweet.setTypeface(fontello);
        send.setTypeface(fontello);
        tweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), PostActivity.class);
                if (findViewById(R.id.quick_tweet_layout).getVisibility() == View.VISIBLE) {
                    EditText status = (EditText) findViewById(R.id.quick_tweet_edit);
                    if (status == null) {
                        return;
                    }
                    String msg = status.getText() != null ? status.getText().toString() : null;
                    if (msg != null && msg.length() > 0) {
                        intent.putExtra("status", msg);
                        intent.putExtra("selection", msg.length());
                        if (mInReplyToStatus != null) {
                            intent.putExtra("inReplyToStatus", mInReplyToStatus);
                        }
                        status.setText("");
                        status.clearFocus();
                    }
                }
                startActivity(intent);
            }
        });

        final int defaultTextColor = JustawayApplication.getApplication().getThemeTextColor(this, R.attr.menu_text_color);
        final int disabledTextColor = JustawayApplication.getApplication().getThemeTextColor(this, R.attr.menu_text_color_disabled);
        ((EditText) findViewById(R.id.quick_tweet_edit)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                int textColor;
                int length = TwitterUtil.count(charSequence.toString());
                // 140文字をオーバーした時は文字数を赤色に
                if (length < 0) {
                    textColor = Color.RED;
                } else if (length == 140) {
                    textColor = disabledTextColor;
                } else {
                    textColor = defaultTextColor;
                }
                TextView count = ((TextView) findViewById(R.id.count));
                count.setTextColor(textColor);
                count.setText(String.valueOf(length));

                if (length < 0 || length == 140) {
                    // 文字数が0文字または140文字以上の時はボタンを無効
                    send.setEnabled(false);
                } else {
                    send.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        tweet.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (findViewById(R.id.quick_tweet_layout).getVisibility() == View.VISIBLE) {
                    hideQuickPanel();
                } else {
                    showQuickPanel();
                }
                return true;
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText status = (EditText) findViewById(R.id.quick_tweet_edit);
                String msg = status.getText() != null ? status.getText().toString() : null;
                if (msg != null && msg.length() > 0) {
                    showProgressDialog(getString(R.string.progress_sending));

                    if (msg.startsWith("D ")) {
                        SendDirectMessageTask task = new SendDirectMessageTask(null) {
                            @Override
                            protected void onPostExecute(TwitterException e) {
                                dismissProgressDialog();
                                if (e == null) {
                                    EditText status = (EditText) findViewById(R.id.quick_tweet_edit);
                                    status.setText("");
                                } else {
                                    JustawayApplication.showToast(R.string.toast_update_status_failure);
                                }
                            }
                        };
                        task.execute(msg);
                    } else {
                        StatusUpdate statusUpdate = new StatusUpdate(msg);
                        if (mInReplyToStatus != null) {
                            statusUpdate.setInReplyToStatusId(mInReplyToStatus.getId());
                            setInReplyToStatus(null);
                        }

                        UpdateStatusTask task = new UpdateStatusTask(null) {
                            @Override
                            protected void onPostExecute(TwitterException e) {
                                dismissProgressDialog();
                                if (e == null) {
                                    EditText status = (EditText) findViewById(R.id.quick_tweet_edit);
                                    status.setText("");
                                } else if (e.getErrorCode() == ERROR_CODE_DUPLICATE_STATUS) {
                                    JustawayApplication.showToast(getString(R.string.toast_update_status_already));
                                } else {
                                    JustawayApplication.showToast(R.string.toast_update_status_failure);
                                }
                            }
                        };
                        task.execute(statusUpdate);
                    }
                }
            }
        });

        if (mApplication.getStreamingMode()) {
            mApplication.startStreaming();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (mSwitchAccessToken != null) {
            mApplication.switchAccessToken(mSwitchAccessToken);
            mSwitchAccessToken = null;
        }
        mApplication.resumeStreaming();
        if (mApplication.getTwitterStreamConnected()) {
            mApplication.setThemeTextColor(this, mSignalButton, R.attr.holo_green);
        } else {
            if (mApplication.getStreamingMode()) {
                mApplication.setThemeTextColor(this, mSignalButton, R.attr.holo_red);
            } else {
                mSignalButton.setTextColor(Color.WHITE);
            }
        }
    }

    @Override
    protected void onPause() {
        mApplication.pauseStreaming();
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(AlertDialogEvent event) {
        event.getDialogFragment().show(getSupportFragmentManager(), "dialog");
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(SeenTopEvent event) {
        showTopView();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(EditorEvent event) {
        View singleLineTweet = findViewById(R.id.quick_tweet_layout);
        if (singleLineTweet != null && singleLineTweet.getVisibility() == View.VISIBLE) {
            EditText editStatus = (EditText) findViewById(R.id.quick_tweet_edit);
            editStatus.setText(event.getText());
            if (event.getSelectionStart() != null) {
                if (event.getSelectionStop() != null) {
                    editStatus.setSelection(event.getSelectionStart(), event.getSelectionStop());
                } else {
                    editStatus.setSelection(event.getSelectionStart());
                }
            }
            setInReplyToStatus(event.getInReplyToStatus());
            editStatus.requestFocus();
            mApplication.showKeyboard(editStatus);
        } else {
            Intent intent = new Intent(this, PostActivity.class);
            intent.putExtra("status", event.getText());
            if (event.getSelectionStart() != null) {
                intent.putExtra("selection", event.getSelectionStart());
            }
            if (event.getSelectionStop() != null) {
                intent.putExtra("selection_stop", event.getSelectionStop());
            }
            if (event.getInReplyToStatus() != null) {
                intent.putExtra("inReplyToStatus", event.getInReplyToStatus());
            }
            startActivity(intent);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(ConnectEvent event) {
        mApplication.setThemeTextColor(this, mSignalButton, R.attr.holo_green);
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(DisconnectEvent event) {
        if (mApplication.getStreamingMode()) {
            mApplication.setThemeTextColor(this, mSignalButton, R.attr.holo_red);
        } else {
            mSignalButton.setTextColor(Color.WHITE);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(CleanupEvent event) {
        if (mApplication.getStreamingMode()) {
            mApplication.setThemeTextColor(this, mSignalButton, R.attr.holo_orange);
        } else {
            mSignalButton.setTextColor(Color.WHITE);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(AccountChangeEvent event) {
        if (mAccessTokenAdapter != null) {
            mAccessTokenAdapter.notifyDataSetChanged();
        }
        setupTab();
        mViewPager.setCurrentItem(0);
        EventBus.getDefault().post(new AccountChangePostEvent(mMainPagerAdapter.getItemId(mViewPager.getCurrentItem())));
    }

    public void onEventMainThread(NewRecordEvent event) {
        int position = mMainPagerAdapter.findPositionById(event.getTabId());
        if (mViewPager.getCurrentItem() == position && event.getAutoScroll()) {
            return;
        }
        if (position < 0) {
            return;
        }
        LinearLayout tabMenus = (LinearLayout) findViewById(R.id.tab_menus);
        Button button = (Button) tabMenus.getChildAt(position);
        if (button != null) {
            mApplication.setThemeTextColor(this, button, R.attr.holo_blue);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("signalButtonColor", mSignalButton.getCurrentTextColor());

        LinearLayout tab_menus = (LinearLayout) findViewById(R.id.tab_menus);
        int count = tab_menus.getChildCount();
        final int tabColors[] = new int[count];
        for (int i = 0; i < count; i++) {
            Button button = (Button) tab_menus.getChildAt(i);
            if (button == null) {
                continue;
            }
            tabColors[i] = button.getCurrentTextColor();
        }

        outState.putIntArray("tabColors", tabColors);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mSignalButton.setTextColor(savedInstanceState.getInt("signalButtonColor"));

        final int[] tabColors = savedInstanceState.getIntArray("tabColors");
        assert tabColors != null;
        LinearLayout tab_menus = (LinearLayout) findViewById(R.id.tab_menus);
        int count = Math.min(tab_menus.getChildCount(), tabColors.length);
        for (int i = 0; i < count; i++) {
            Button button = (Button) tab_menus.getChildAt(i);
            if (button == null) {
                continue;
            }
            button.setTextColor(tabColors[i]);
        }
    }

    public void showQuickPanel() {
        findViewById(R.id.quick_tweet_layout).setVisibility(View.VISIBLE);
        EditText editStatus = (EditText) findViewById(R.id.quick_tweet_edit);
        editStatus.setFocusable(true);
        editStatus.setFocusableInTouchMode(true);
        editStatus.setEnabled(true);
        mApplication.setQuickMod(true);
    }

    public void hideQuickPanel() {
        EditText editStatus = (EditText) findViewById(R.id.quick_tweet_edit);
        editStatus.setFocusable(false);
        editStatus.setFocusableInTouchMode(false);
        editStatus.setEnabled(false);
        editStatus.clearFocus();
        findViewById(R.id.quick_tweet_layout).setVisibility(View.GONE);
        setInReplyToStatus(null);
        mApplication.setQuickMod(false);
    }

    public void setupTab() {
        ArrayList<JustawayApplication.Tab> tabs = mApplication.loadTabs();
        if (tabs.size() > 0) {
            Typeface fontello = JustawayApplication.getFontello();
            TypedValue outValueBackground = new TypedValue();
            TypedValue outValueTextColor = new TypedValue();
            Resources.Theme theme = getTheme();
            if (theme != null) {
                theme.resolveAttribute(R.attr.button_stateful, outValueBackground, true);
                theme.resolveAttribute(R.attr.menu_text_color, outValueTextColor, true);
            }
            LinearLayout tabMenus = (LinearLayout) findViewById(R.id.tab_menus);
            tabMenus.removeAllViews();
            mMainPagerAdapter.clearTab();
            int position = 0;
            float density = getResources().getDisplayMetrics().density;
            int width = (int) (60 * density + 0.5f);
            LinearLayout.LayoutParams layoutParams =
                    new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.WRAP_CONTENT);
            for (JustawayApplication.Tab tab : tabs) {
                // 標準のタブを動的に生成する時に実装する
                Button button = new Button(this);
                button.setTypeface(fontello);
                button.setLayoutParams(layoutParams);
                button.setTextSize(22);
                button.setText(tab.getIcon());
                button.setTextColor(outValueTextColor.data);
                button.setBackgroundResource(outValueBackground.resourceId);
                bindTabListener(button, position++);
                tabMenus.addView(button);
                if (tab.id == TAB_ID_TIMELINE) {
                    mMainPagerAdapter.addTab(TimelineFragment.class, null, tab.getName(), tab.id);
                } else if (tab.id == TAB_ID_INTERACTIONS) {
                    mMainPagerAdapter.addTab(InteractionsFragment.class, null, tab.getName(), tab.id);
                } else if (tab.id == TAB_ID_DIRECT_MESSAGE) {
                    mMainPagerAdapter.addTab(DirectMessagesFragment.class, null, tab.getName(), tab.id);
                } else {
                    Bundle args = new Bundle();
                    args.putLong("userListId", tab.id);
                    mMainPagerAdapter.addTab(UserListFragment.class, args, tab.getName(), tab.id);
                }
            }
            mMainPagerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // 前回バグで強制終了した場合はダイアログ表示、Yesでレポート送信
        MyUncaughtExceptionHandler.showBugReportDialogIfExist(this);

        // スリープさせない指定
        if (JustawayApplication.getApplication().getKeepScreenOn()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        mApplication.resetDisplaySettings();
        mApplication.resetNotification();

        // フォントサイズの変更や他のアクティビティでのfav/RTを反映
        mMainPagerAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_TAB_SETTINGS:
                if (resultCode == RESULT_OK) {
                    setupTab();
                }
                break;
            case REQUEST_ACCOUNT_SETTING:
                if (resultCode == RESULT_OK) {
                    mSwitchAccessToken = (AccessToken) data.getSerializableExtra("accessToken");
                }
                if (mAccessTokenAdapter != null) {
                    mAccessTokenAdapter.clear();
                    for (AccessToken accessToken : mApplication.getAccessTokens()) {
                        mAccessTokenAdapter.add(accessToken);
                    }
                }
                break;
            case REQUEST_SETTINGS:
                if (resultCode == RESULT_OK) {
                    mApplication.resetDisplaySettings();
                    finish();
                }
            default:
                break;
        }
    }

    private void bindTabListener(TextView textView, final int position) {
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseFragment f = mMainPagerAdapter.findFragmentByPosition(position);
                if (f == null) {
                    return;
                }
                int id = mViewPager.getCurrentItem();
                if (id != position) {
                    mViewPager.setCurrentItem(position);
                    if (f.isTop()) {
                        showTopView();
                    }
                } else {
                    f.goToTop();
                }
            }
        });
        textView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                BaseFragment f = mMainPagerAdapter.findFragmentByPosition(position);
                if (f == null) {
                    return false;
                }
                f.reload();
                return true;
            }
        });
    }

    private void setup() {

        /**
         * スワイプで動かせるタブを実装するのに最低限必要な実装
         */
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mMainPagerAdapter = new MainPagerAdapter(this, mViewPager);

        setupTab();

        findViewById(R.id.footer).setVisibility(View.VISIBLE);

        /**
         * タブは前後タブまでは状態が保持されるがそれ以上離れるとViewが破棄されてしまう、
         * あまりに使いづらいの上限を増やしている、指定値＋前後のタブまでが保持されるようになる
         * デフォルト値は1（表示しているタブの前後までしか保持されない）
         */
        mViewPager.setOffscreenPageLimit(10);

        /**
         * スワイプ移動でも移動先が未読アプしている場合、アピ解除判定を行う
         */
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                BaseFragment f = mMainPagerAdapter.findFragmentByPosition(position);
                if (f.isTop()) {
                    showTopView();
                }
                LinearLayout tab_menus = (LinearLayout) findViewById(R.id.tab_menus);
                int count = tab_menus.getChildCount();
                for (int i = 0; i < count; i++) {
                    Button button = (Button) tab_menus.getChildAt(i);
                    if (button == null) {
                        continue;
                    }
                    if (i == position) {
                        button.setSelected(true);
                    } else {
                        button.setSelected(false);
                    }
                }
                setTitle(mMainPagerAdapter.getPageTitle(position));
            }
        });

        if (mApplication.getQuickMode()) {
            showQuickPanel();
        }
    }

    /**
     * 新しいレコードを見たアピ
     */
    public void showTopView() {
        LinearLayout tab_menus = (LinearLayout) findViewById(R.id.tab_menus);
        Button button = (Button) tab_menus.getChildAt(mViewPager.getCurrentItem());
        if (button != null) {
            mApplication.setThemeTextColor(this, button, R.attr.menu_text_color);
        }
    }

    /**
     * 弄らないとアプリをバックボタンで閉じる度にタイムラインが初期化されてしまう（アクティビティがfinishされる）
     * moveTaskToBackはホームボタンを押した時と同じ動き
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            EditText editText = (EditText) findViewById(R.id.quick_tweet_edit);
            if (editText != null && editText.getText() != null && editText.getText().length() > 0) {
                editText.setText("");
                setInReplyToStatus(null);
                return false;
            }
            finish();
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.profile) {
            /**
             * screenNameは変更可能なのでuserIdを使う
             */
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("userId", mApplication.getUserId());
            startActivity(intent);
        } else if (itemId == R.id.tab_settings) {
            Intent intent = new Intent(this, TabSettingsActivity.class);
            startActivityForResult(intent, REQUEST_TAB_SETTINGS);
        } else if (itemId == R.id.search) {
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
        } else if (itemId == R.id.settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivityForResult(intent, REQUEST_SETTINGS);
        } else if (itemId == R.id.official_website) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.official_website)));
            startActivity(intent);
        } else if (itemId == R.id.feedback) {
            View singleLineTweet = findViewById(R.id.quick_tweet_layout);
            if (singleLineTweet != null && singleLineTweet.getVisibility() == View.VISIBLE) {
                EditText editStatus = (EditText) findViewById(R.id.quick_tweet_edit);
                editStatus.setText(" #justaway");
                editStatus.requestFocus();
                mApplication.showKeyboard(editStatus);
                return true;
            }
            Intent intent = new Intent(this, PostActivity.class);
            intent.putExtra("status", " #justaway");
            startActivity(intent);
        } else if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        } else if (itemId == android.R.id.home) {
            mSearchText.setText("");
            mSearchText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        return;
                    }
                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(v.getWindowToken(), 0);
                    mSearchText.setVisibility(View.GONE);
                    mNormalLayout.setVisibility(View.VISIBLE);
                    mDrawerToggle.setDrawerIndicatorEnabled(true);
                    mSearchText.setOnFocusChangeListener(null);
                }
            });
            mSearchText.requestFocus();
            mSearchText.clearFocus();
        }
        return true;
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

    public void doDestroyDirectMessage(Long id) {
        new DestroyDirectMessageTask().execute(id);
        // 自分宛のDMを消してもStreaming APIで拾えないで自力で消す
        DirectMessagesFragment fragment = (DirectMessagesFragment) mMainPagerAdapter
                .findFragmentById(TAB_ID_DIRECT_MESSAGE);
        if (fragment != null) {
            fragment.remove(id);
        }
    }

    public static final class StreamingSwitchDialogFragment extends DialogFragment {

        private static StreamingSwitchDialogFragment newInstance(boolean turnOn) {
            final Bundle args = new Bundle(1);
            args.putBoolean("turnOn", turnOn);

            final StreamingSwitchDialogFragment f = new StreamingSwitchDialogFragment();
            f.setArguments(args);
            return f;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final boolean turnOn = getArguments().getBoolean("turnOn");

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(turnOn ? R.string.confirm_create_streaming : R.string.confirm_destroy_streaming);
            builder.setPositiveButton(getString(R.string.button_ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            JustawayApplication.getApplication().setStreamingMode(turnOn);
                            if (turnOn) {
                                JustawayApplication.getApplication().startStreaming();
                                JustawayApplication.showToast(R.string.toast_create_streaming);
                            } else {
                                JustawayApplication.getApplication().stopStreaming();
                                JustawayApplication.showToast(R.string.toast_destroy_streaming);
                            }
                            dismiss();
                        }
                    }
            );
            builder.setNegativeButton(getString(R.string.button_cancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                        }
                    }
            );
            return builder.create();
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
        public void clear() {
            super.clear();
            mAccessTokenList.clear();
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
            ImageView icon = (ImageView) view.findViewById(R.id.icon);
            JustawayApplication.getApplication().displayUserIcon(accessToken.getUserId(), icon);
            ((TextView) view.findViewById(R.id.screen_name)).setText(accessToken.getScreenName());

            if (JustawayApplication.getApplication().getUserId() == accessToken.getUserId()) {
                ((TextView) view.findViewById(R.id.screen_name)).setTextColor(JustawayApplication.getApplication().getThemeTextColor(mActivity, R.attr.holo_blue));
            } else {
                ((TextView) view.findViewById(R.id.screen_name)).setTextColor(JustawayApplication.getApplication().getThemeTextColor(mActivity, R.attr.text_color));
            }

            return view;
        }
    }
}
