package info.justaway;

import android.R.color;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;

import info.justaway.adapter.MainPagerAdapter;
import info.justaway.fragment.main.BaseFragment;
import info.justaway.fragment.main.DirectMessagesFragment;
import info.justaway.fragment.main.InteractionsFragment;
import info.justaway.fragment.main.TimelineFragment;
import info.justaway.fragment.main.UserListFragment;
import info.justaway.model.Row;
import info.justaway.task.DestroyDirectMessageTask;
import info.justaway.task.LoadUserListsTask;
import info.justaway.task.ReFetchFavoriteStatus;
import twitter4j.DirectMessage;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusUpdate;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.User;
import twitter4j.UserStreamAdapter;

/**
 * @author aska
 */
public class MainActivity extends FragmentActivity {

    private JustawayApplication mApplication;
    private TwitterStream mTwitterStream;
    private MainPagerAdapter mMainPagerAdapter;
    private ViewPager mViewPager;
    private ProgressDialog mProgressDialog;
    private static final int REQUEST_CHOOSE_USER_LIST = 100;
    private static final int REQUEST_ACCOUNT_SETTING = 200;
    private static final int ERROR_CODE_DUPLICATE_STATUS = 187;
    private static final int TAB_ID_TIMELINE = -1;
    private static final int TAB_ID_INTERACTIONS = -2;
    private static final int TAB_ID_DIRECT_MESSAGE = -3;

    private Long mInReplyToStatusId;

    public Long getInReplyToStatusId() {
        return mInReplyToStatusId;
    }

    public void setInReplyToStatusId(Long inReplyToStatusId) {
        this.mInReplyToStatusId = inReplyToStatusId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // クイックモード時に起動と同時にキーボードが出現するのを抑止
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        setContentView(R.layout.activity_main);

        setTitle(R.string.title_main);

        // クイックモード時に起動と同時に入力エリアにフォーカスするのを抑止
        findViewById(R.id.main).requestFocus();

        mApplication = JustawayApplication.getApplication();

        // アクセストークンがない場合に認証用のアクティビティを起動する
        if (!mApplication.hasAccessToken()) {
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setup();

        /**
         * ユーザーリストの一覧をアプリケーションのメンバ変数に読み込んでおく
         * これがないと最新のリスト名を表示できない
         */
        new LoadUserListsTask().execute();

        /**
         * 違うタブだったら移動、同じタブだったら最上部にスクロールという美しい実装
         * ActionBarのタブに頼っていない為、自力でsetCurrentItemでタブを動かしている
         * タブの切替がスワイプだけで良い場合はこの処理すら不要
         */
        Typeface fontello = JustawayApplication.getFontello();
        Button home = (Button) findViewById(R.id.action_timeline);
        Button interactions = (Button) findViewById(R.id.action_interactions);
        Button directMessage = (Button) findViewById(R.id.action_direct_message);
        Button tweet = (Button) findViewById(R.id.action_tweet);
        Button send = (Button) findViewById(R.id.send);
        home.setTypeface(fontello);
        interactions.setTypeface(fontello);
        directMessage.setTypeface(fontello);
        tweet.setTypeface(fontello);
        send.setTypeface(fontello);
        home.setOnClickListener(tabMenuOnClickListener(0));
        interactions.setOnClickListener(tabMenuOnClickListener(1));
        directMessage.setOnClickListener(tabMenuOnClickListener(2));
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
                        Long inReplyToStatusId = getInReplyToStatusId();
                        intent.putExtra("status", msg);
                        intent.putExtra("selection", msg.length());
                        if (inReplyToStatusId != null && inReplyToStatusId > 0) {
                            intent.putExtra("inReplyToStatusId", inReplyToStatusId);
                        }
                        status.setText("");
                        status.clearFocus();
                    }
                }
                startActivity(intent);
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
                    StatusUpdate super_sugoi = new StatusUpdate(msg);
                    Long inReplyToStatusId = getInReplyToStatusId();
                    if (inReplyToStatusId != null && inReplyToStatusId > 0) {
                        super_sugoi.setInReplyToStatusId(inReplyToStatusId);
                        setInReplyToStatusId((long) 0);
                    }
                    new UpdateStatusTask().execute(super_sugoi);
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
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
        setInReplyToStatusId((long) 0);
        mApplication.setQuickMod(false);
    }

    public void setupTab() {
        LinearLayout tab_menus = (LinearLayout) findViewById(R.id.tab_menus);

        int count = tab_menus.getChildCount();
        // 4つめ以降のタブを消す
        if (count > 3) {
            for (int position = count - 1; position > 2; position--) {
                View view = tab_menus.getChildAt(position);
                if (view != null) {
                    tab_menus.removeView(view);
                }
                mMainPagerAdapter.removeTab(position);
            }
        }

        Typeface fontello = JustawayApplication.getFontello();
        ArrayList<Integer> tabs = mApplication.loadTabs();
        if (tabs.size() > 3) {
            int position = 2;
            for (Integer tab : tabs) {
                // 標準のタブを動的に生成する時に実装する
                if (tab > 0) {
                    Button button = new Button(this);
                    button.setWidth(60);
                    button.setTypeface(fontello);
                    button.setTextSize(22);
                    button.setBackgroundColor(getResources().getColor(R.color.menu_background));
                    button.setText(R.string.fontello_list);
                    button.setOnClickListener(tabMenuOnClickListener(++position));
                    tab_menus.addView(button);
                    Bundle args = new Bundle();
                    args.putInt("userListId", tab);
                    mMainPagerAdapter.addTab(UserListFragment.class, args, "-", tab);
                }
            }
        }

        mMainPagerAdapter.notifyDataSetChanged();
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

        mApplication.resetFontSize();

        // フォントサイズの変更や他のアクティビティでのfav/RTを反映
        mMainPagerAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mTwitterStream != null) {
            mTwitterStream.cleanUp();
            mTwitterStream.shutdown();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CHOOSE_USER_LIST:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    if (bundle == null) {
                        return;
                    }
                    ArrayList<Integer> lists = bundle.getIntegerArrayList("lists");
                    ArrayList<Integer> tabs = new ArrayList<Integer>();
                    // 後々タブ設定画面に標準のタブを含める
                    tabs.add(-1);
                    tabs.add(-2);
                    tabs.add(-3);
                    tabs.addAll(lists);
                    mApplication.saveTabs(tabs);
                    setupTab();
                    if (lists != null && lists.size() > 0) {
                        JustawayApplication.showToast(R.string.toast_register_list_for_tab);
                    }
                } else {

                    /**
                     * リストの削除を検出してタブを再構成
                     */
                    ArrayList<Integer> tabs = mApplication.loadTabs();
                    ArrayList<Integer> new_tabs = new ArrayList<Integer>();
                    for (Integer tab : tabs) {
                        if (tab > 0 && mApplication.getUserList(tab) == null) {
                            continue;
                        }
                        new_tabs.add(tab);
                    }
                    if (tabs.size() != new_tabs.size()) {
                        mApplication.saveTabs(new_tabs);
                        setupTab();
                    }
                }
                break;
            case REQUEST_ACCOUNT_SETTING:

                if (mTwitterStream != null) {
                    mTwitterStream.cleanUp();
                    mTwitterStream.shutdown();
                }

                setupTab();
                int count = mMainPagerAdapter.getCount();
                for (int id = 0; id < count; id++) {
                    BaseFragment fragment = mMainPagerAdapter
                            .findFragmentByPosition(id);
                    if (fragment != null) {
                        fragment.getListAdapter().clear();
                        fragment.reload();
                    }
                }
            default:
                break;
        }
    }

    private View.OnClickListener tabMenuOnClickListener(final int position) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        };
    }

    public void setup() {

        /**
         * スワイプで動かせるタブを実装するのに最低限必要な実装
         */
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mMainPagerAdapter = new MainPagerAdapter(this, mViewPager);

        mMainPagerAdapter.addTab(TimelineFragment.class, null, getString(R.string.title_main), TAB_ID_TIMELINE);
        mMainPagerAdapter.addTab(InteractionsFragment.class, null, getString(R.string.title_interactions), TAB_ID_INTERACTIONS);
        mMainPagerAdapter.addTab(DirectMessagesFragment.class, null, getString(R.string.title_direct_messages), TAB_ID_DIRECT_MESSAGE);
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
                        button.setBackgroundColor(getResources().getColor(
                                R.color.menu_active_background));
                    } else {
                        button.setBackgroundColor(getResources().getColor(
                                R.color.menu_background));
                    }
                }
                setTitle(mMainPagerAdapter.getPageTitle(position));
            }
        });

        if (mApplication.getQuickMode()) {
            showQuickPanel();
        }
    }

    public void setupStream() {
        if (mTwitterStream != null) {
            mTwitterStream.cleanUp();
            mTwitterStream.shutdown();
        }
        mTwitterStream = mApplication.getTwitterStream();
        mTwitterStream.addListener(getUserStreamAdapter());
        mTwitterStream.user();
    }

    /**
     * 新しいツイートが来たアピ
     */
    public void onNewTimeline(Boolean autoScroll) {
        // 表示中のタブかつ自動スクロール時はハイライトしない
        if (mViewPager.getCurrentItem() == 0 && autoScroll) {
            return;
        }
        Button button = (Button) findViewById(R.id.action_timeline);
        button.setTextColor(getResources().getColor(color.holo_blue_bright));
    }

    /**
     * 新しいリプが来たアピ
     */
    public void onNewInteractions(Boolean autoScroll) {
        // 表示中のタブかつ自動スクロール時はハイライトしない
        if (mViewPager.getCurrentItem() == 1 && autoScroll) {
            return;
        }
        Button button = (Button) findViewById(R.id.action_interactions);
        button.setTextColor(getResources().getColor(color.holo_blue_bright));
    }

    /**
     * 新しいDMが来たアピ
     */
    public void onNewDirectMessage(Boolean autoScroll) {
        // 表示中のタブかつ自動スクロール時はハイライトしない
        if (mViewPager.getCurrentItem() == 2 && autoScroll) {
            return;
        }
        Button button = (Button) findViewById(R.id.action_direct_message);
        button.setTextColor(getResources().getColor(color.holo_blue_bright));
    }

    /**
     * 新しいツイートが来たアピ
     */
    public void onNewListStatus(int listId, Boolean autoScroll) {
        // 表示中のタブかつ自動スクロール時はハイライトしない
        int position = mMainPagerAdapter.findPositionById(listId);
        if (mViewPager.getCurrentItem() == position && autoScroll) {
            return;
        }
        if (position >= 0) {
            LinearLayout tab_menus = (LinearLayout) findViewById(R.id.tab_menus);
            Button button = (Button) tab_menus.getChildAt(position);
            if (button != null) {
                button.setTextColor(getResources().getColor(color.holo_blue_bright));
            }
        }
    }

    /**
     * 新しいレコードを見たアピ
     */
    public void showTopView() {
        LinearLayout tab_menus = (LinearLayout) findViewById(R.id.tab_menus);
        Button button = (Button) tab_menus.getChildAt(mViewPager.getCurrentItem());
        if (button != null) {
            button.setTextColor(getResources().getColor(color.white));
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
                setInReplyToStatusId((long) 0);
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
        } else if (itemId == R.id.user_list) {
            Intent intent = new Intent(this, ChooseUserListsActivity.class);
            startActivityForResult(intent, REQUEST_CHOOSE_USER_LIST);
        } else if (itemId == R.id.search) {
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
        } else if (itemId == R.id.settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
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
        } else if (itemId == R.id.account) {
            Intent intent = new Intent(this, AccountSettingActivity.class);
            startActivityForResult(intent, REQUEST_ACCOUNT_SETTING);
        }
        return true;
    }

    /**
     * ストリーミング受信時の処理
     */
    private UserStreamAdapter getUserStreamAdapter() {
        final View view = findViewById(R.id.action_interactions);
        return new UserStreamAdapter() {

            @Override
            public void onStatus(Status status) {

                /**
                 * ツイートを表示するかどうかはFragmentに任せる
                 */
                int count = mMainPagerAdapter.getCount();
                for (int id = 0; id < count; id++) {
                    BaseFragment fragment = mMainPagerAdapter
                            .findFragmentByPosition(id);
                    if (fragment != null) {
                        fragment.add(Row.newStatus(status));
                    }
                }
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
                super.onDeletionNotice(statusDeletionNotice);
                int count = mMainPagerAdapter.getCount();
                for (int id = 0; id < count; id++) {
                    BaseFragment fragment = mMainPagerAdapter
                            .findFragmentByPosition(id);
                    if (fragment != null) {
                        fragment.removeStatus(statusDeletionNotice.getStatusId());
                    }
                }
            }

            @Override
            public void onFavorite(User source, User target, Status status) {
                // 自分の fav を反映
                if (source.getId() == mApplication.getUserId()) {
                    mApplication.setFav(status.getId());
                    return;
                }
                Row row = Row.newFavorite(source, target, status);
                BaseFragment fragment = mMainPagerAdapter
                        .findFragmentById(TAB_ID_INTERACTIONS);
                new ReFetchFavoriteStatus(fragment).execute(row);
            }

            @Override
            public void onUnfavorite(User arg0, User arg1, Status arg2) {

                final User source = arg0;
                final Status status = arg2;

                // 自分の unfav を反映
                if (source.getId() == mApplication.getUserId()) {
                    mApplication.removeFav(status.getId());
                    return;
                }

                view.post(new Runnable() {
                    @Override
                    public void run() {
                        JustawayApplication.showToast(source.getScreenName() + " unfav "
                                + status.getText());
                    }
                });
            }

            @Override
            public void onDirectMessage(DirectMessage directMessage) {
                super.onDirectMessage(directMessage);
                BaseFragment fragment = mMainPagerAdapter
                        .findFragmentById(TAB_ID_DIRECT_MESSAGE);
                if (fragment != null) {
                    fragment.add(Row.newDirectMessage(directMessage));
                }
            }

            @Override
            public void onDeletionNotice(long directMessageId, long userId) {
                super.onDeletionNotice(directMessageId, userId);
                DirectMessagesFragment fragment = (DirectMessagesFragment) mMainPagerAdapter
                        .findFragmentById(TAB_ID_DIRECT_MESSAGE);
                if (fragment != null) {
                    fragment.remove(directMessageId);
                }
            }
        };
    }

    public class UpdateStatusTask extends AsyncTask<StatusUpdate, Void, TwitterException> {
        @Override
        protected TwitterException doInBackground(StatusUpdate... params) {
            StatusUpdate superSugoi = params[0];
            try {
                mApplication.getTwitter().updateStatus(superSugoi);
                return null;
            } catch (TwitterException e) {
                e.printStackTrace();
                return e;
            }
        }

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

    public void notifyDataSetChanged() {

        /**
         * 重く同期で処理すると一瞬画面が固まる
         */
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                mMainPagerAdapter.notifyDataSetChanged();
            }
        });
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
}
