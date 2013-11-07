package info.justaway;

import java.util.ArrayList;

import info.justaway.fragment.BaseFragment;
import info.justaway.fragment.DirectMessageFragment;
import info.justaway.fragment.InteractionsFragment;
import info.justaway.fragment.TimelineFragment;
import info.justaway.fragment.UserListFragment;
import info.justaway.model.Row;
import info.justaway.task.DestroyDirectMessageTask;
import info.justaway.task.DestroyStatusTask;
import info.justaway.task.FavoriteTask;
import info.justaway.task.RetweetTask;
import info.justaway.task.UnFavoriteTask;
import info.justaway.task.VerifyCredentialsLoader;
import twitter4j.DirectMessage;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.TwitterStream;
import twitter4j.User;
import twitter4j.UserStreamAdapter;
import android.R.color;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * @author aska
 * 
 */
public class MainActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<User> {

    private JustawayApplication app;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager viewPager;
    private Row selectedRow;
    private final int REQUEST_CHOOSE_USER_LIST = 100;

    /**
     * 自分自身のUserオブジェクト(Twitter) リプのタブでツイートが自分に対してのリプかどうかの判定などで使用している
     */
    public User getUser() {
        return JustawayApplication.getApplication().getUser();
    }

    public void setUser(User user) {
        JustawayApplication.getApplication().setUser(user);
    }

    /**
     * タブビューを実現するためのもの、とても大事 サポートパッケージv4から、2系でも使えるパッケージを使用
     */
    public ViewPager getViewPager() {
        return viewPager;
    }

    public void setViewPager(ViewPager viewPager) {
        this.viewPager = viewPager;
    }

    /**
     * コンテキストメニュー表示時の選択したツイートをセットしている Streaming API対応で勝手に画面がスクロールされる為、
     * positionから取得されるitemが変わってしまい、どこかに保存する必要があった
     */
    public Row getSelectedRow() {
        return selectedRow;
    }

    public void setSelectedRow(Row selectedRow) {
        this.selectedRow = selectedRow;
    }

    /**
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        app = JustawayApplication.getApplication();

        // スリープさせない指定
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // アクセストークンがない場合に認証用のアクティビティを起動する
        if (!app.hasAccessToken()) {
            Intent intent = new Intent(this, SigninActivity.class);
            startActivity(intent);
            finish();
        } else {

            // とりあえず勝手にストリーミング開始するようにしている
            TwitterStream twitterStream = app.getTwitterStream();
            twitterStream.addListener(getUserStreamAdapter());
            twitterStream.user();

            /**
             * onCreateLoader => onLoadFinished と繋がる
             */
            getSupportLoaderManager().initLoader(0, null, this);
        }

        /**
         * 違うタブだったら移動、同じタブだったら最上部にスクロールという美しい実装
         * ActionBarのタブに頼っていない為、自力でsetCurrentItemでタブを動かしている
         * タブの切替がスワイプだけで良い場合はこの処理すら不要
         */
        Typeface fontello = Typeface.createFromAsset(getAssets(), "fontello.ttf");
        Button home = (Button) findViewById(R.id.action_timeline);
        Button interactions = (Button) findViewById(R.id.action_interactions);
        Button directmessage = (Button) findViewById(R.id.action_directmessage);
        Button tweet = (Button) findViewById(R.id.action_tweet);
        home.setTypeface(fontello);
        interactions.setTypeface(fontello);
        directmessage.setTypeface(fontello);
        tweet.setTypeface(fontello);
        home.setOnClickListener(tabMenuOnClickListener(0));
        interactions.setOnClickListener(tabMenuOnClickListener(1));
        directmessage.setOnClickListener(tabMenuOnClickListener(2));
        tweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), PostActivity.class);
                startActivity(intent);
            }
        });
    }

    public void initTab() {
        LinearLayout tab_menus = (LinearLayout) findViewById(R.id.tab_menus);

        int count = tab_menus.getChildCount();
        // 4つめ以降のタブを消す
        if (count > 3) {
            for (int position = count - 1; position > 2; position--) {
                tab_menus.removeView(tab_menus.getChildAt(position));
                mSectionsPagerAdapter.removeTab(position);
            }
            mSectionsPagerAdapter.notifyDataSetChanged();
        }

        ArrayList<Integer> tabs = app.loadTabs();
        int position = 2;
        for (Integer tab : tabs) {
            Typeface fontello = Typeface.createFromAsset(getAssets(), "fontello.ttf");
            // 標準のタブを動的に生成する時に実装する
            if (tab == -1) {

            } else if (tab == -2) {

            } else if (tab == -3) {

            } else if (tab > 0) {
                Button button = new Button(this);
                button.setWidth(60);
                button.setTypeface(fontello);
                button.setBackgroundColor(getResources().getColor(R.color.transparent));
                button.setText(R.string.fontello_list);
                button.setOnClickListener(tabMenuOnClickListener(++position));
                final int fp = position;
                button.setOnLongClickListener(new View.OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View v) {
                        UserListFragment f = (UserListFragment) mSectionsPagerAdapter
                                .findFragmentByPosition(fp);
                        f.reload();
                        return false;
                    }

                });
                tab_menus.addView(button);
                Bundle args = new Bundle();
                args.putInt("userListId", tab);
                mSectionsPagerAdapter.addTab(UserListFragment.class, args, "list", tab);
            }
        }
        mSectionsPagerAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // 前回バグで強制終了した場合はダイアログ表示、Yesでレポート送信
        MyUncaughtExceptionHandler.showBugReportDialogIfExist(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
        case REQUEST_CHOOSE_USER_LIST:
            if (resultCode == RESULT_OK) {
                Bundle bundle = data.getExtras();
                ArrayList<Integer> lists = bundle.getIntegerArrayList("lists");
                ArrayList<Integer> tabs = new ArrayList<Integer>();
                // 後々タブ設定画面に標準のタブを含める
                tabs.add(-1);
                tabs.add(-2);
                tabs.add(-3);
                tabs.addAll(lists);
                app.saveTabs(tabs);
                app.setLists(lists);
                initTab();
            } else if (resultCode == RESULT_CANCELED) {
                ArrayList<Integer> lists = new ArrayList<Integer>();
                ArrayList<Integer> tabs = new ArrayList<Integer>();
                // 後々タブ設定画面に標準のタブを含める
                tabs.add(-1);
                tabs.add(-2);
                tabs.add(-3);
                app.saveTabs(tabs);
                app.setLists(lists);
                initTab();
            }
            break;
        default:
            break;
        }
    }

    private View.OnClickListener tabMenuOnClickListener(final int position) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BaseFragment f = mSectionsPagerAdapter.findFragmentByPosition(position);
                if (f == null) {
                    return;
                }
                int id = viewPager.getCurrentItem();
                if (id != position) {
                    viewPager.setCurrentItem(position);
                    if (f.isTop()) {
                        showTopView();
                    }
                } else {
                    f.goToTop();
                }
            }
        };
    }

    /**
     * 認証済みのユーザーアカウントを取得
     * 
     * @param id
     * @param args
     * @return User 認証済みのユーザー
     */
    @Override
    public Loader<User> onCreateLoader(int id, Bundle args) {
        return new VerifyCredentialsLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<User> loader, User user) {
        // VerifyCredentialsLoaderが失敗する場合も考慮
        if (user == null) {
            JustawayApplication.getApplication().resetAccessToken();
            Intent intent = new Intent(this, SigninActivity.class);
            startActivity(intent);
            finish();
        } else {
            setUser(user);
            JustawayApplication.showToast(user.getScreenName() + " さんこんにちわ！！！！");

            /**
             * スワイプで動かせるタブを実装するのに最低限必要な実装
             */
            ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
            mSectionsPagerAdapter = new SectionsPagerAdapter(this, viewPager);
            setViewPager(viewPager);

            mSectionsPagerAdapter.addTab(TimelineFragment.class, null, "Home", -1);
            mSectionsPagerAdapter.addTab(InteractionsFragment.class, null, "Home", -2);
            mSectionsPagerAdapter.addTab(DirectMessageFragment.class, null, "Home", -3);
            initTab();
//            mSectionsPagerAdapter.notifyDataSetChanged();

            /**
             * タブは前後タブまでは状態が保持されるがそれ以上離れるとViewが破棄されてしまう、
             * あまりに使いづらいの上限を増やしている、指定値＋前後のタブまでが保持されるようになる
             * デフォルト値は1（表示しているタブの前後までしか保持されない）
             */
            viewPager.setOffscreenPageLimit(3);

            /**
             * スワイプ移動でも移動先が未読アプしている場合、アピ解除判定を行う
             */
            viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    BaseFragment f = mSectionsPagerAdapter.findFragmentByPosition(position);
                    if (f.isTop()) {
                        showTopView();
                    }
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<User> arg0) {

    }

    /**
     * 新しいツイートが来たアピ
     */
    public void onNewTimeline(Boolean autoScroll) {
        // 表示中のタブかつ自動スクロール時はハイライトしない
        if (viewPager.getCurrentItem() == 0 && autoScroll == true) {
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
        if (viewPager.getCurrentItem() == 1 && autoScroll == true) {
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
        if (viewPager.getCurrentItem() == 1 && autoScroll == true) {
            return;
        }
        Button button = (Button) findViewById(R.id.action_directmessage);
        button.setTextColor(getResources().getColor(color.holo_blue_bright));
    }

    /**
     * 新しいレコードを見たアピ
     */
    public void showTopView() {
        int id = viewPager.getCurrentItem() == 0 ? R.id.action_timeline : viewPager
                .getCurrentItem() == 1 ? R.id.action_interactions : R.id.action_directmessage;
        Button button = (Button) findViewById(id);
        button.setTextColor(getResources().getColor(color.white));
    }

    /**
     * タブの切替毎に必要なFragmentを取得するためのAdapterクラス
     */
    public static class SectionsPagerAdapter extends FragmentPagerAdapter {
        private final Context mContext;
        private final ViewPager mViewPager;
        private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

        private static final class TabInfo {
            private final Class<?> clazz;
            private final Bundle args;
            private final String tabTitle;
            private final Integer id;

            /**
             * タブ内のActivity、引数を設定する。
             * 
             * @param clazz タブ内のv4.Fragment
             * @param args タブ内のv4.Fragmentに対する引数
             * @param tabTitle タブに表示するタイトル
             */
            TabInfo(Class<?> clazz, Bundle args, String tabTitle, Integer id) {
                this.clazz = clazz;
                this.args = args;
                this.tabTitle = tabTitle;
                this.id = id;
            }
        }

        public SectionsPagerAdapter(FragmentActivity context, ViewPager viewPager) {
            super(context.getSupportFragmentManager());
            viewPager.setAdapter(this);
            mContext = context;
            mViewPager = viewPager;
        }

        @Override
        public BaseFragment getItem(int position) {
            TabInfo info = mTabs.get(position);
            return (BaseFragment) Fragment.instantiate(mContext, info.clazz.getName(), info.args);
        }

        @Override
        public long getItemId(int position) {
            return mTabs.get(position).id;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        public BaseFragment findFragmentByPosition(int position) {
            return (BaseFragment) instantiateItem(mViewPager, position);
        }

        /**
         * タブ内に起動するActivity、引数、タイトルを設定する
         * 
         * @param clazz 起動するv4.Fragmentクラス
         * @param args v4.Fragmentに対する引数
         * @param tabTitle タブのタイトル
         */
        public void addTab(Class<?> clazz, Bundle args, String tabTitle, Integer id) {
            TabInfo info = new TabInfo(clazz, args, tabTitle, id);
            mTabs.add(info);
        }

        public void removeTab(int position) {
            mTabs.remove(position);
        }

        @Override
        public int getCount() {
            // タブ数
            return mTabs.size();
        }
    }

    /**
     * 弄らないとアプリをバックボタンで閉じる度にタイムラインが初期化されてしまう（アクティビティがfinishされる）
     * moveTaskToBackはホームボタンを押した時と同じ動き
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
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
        if (itemId == R.id.signout) {
            JustawayApplication.getApplication().resetAccessToken();
            finish();
        } else if (itemId == R.id.death) {
            int index = 5;
            String[] strs = new String[index];
            String str = strs[index];// ここでIndexOutOfBoundsException
        } else if (itemId == R.id.reload) {
            TwitterStream twitterStream = JustawayApplication.getApplication().getTwitterStream();
            if (twitterStream != null) {
                twitterStream.cleanUp();
                twitterStream.shutdown();
                twitterStream.user();
            }
            initTab();
        } else if (itemId == R.id.onore) {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("userId", getUser().getId());
            startActivity(intent);
        } else if (itemId == R.id.user_list) {
            Intent intent = new Intent(this, ChooseUserListsActivity.class);
            startActivityForResult(intent, REQUEST_CHOOSE_USER_LIST);
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
                 * 自分宛のリプまたは自分のツイートのRTは別タブ
                 */
                int id = 0;
                if (getUser().getId() == status.getInReplyToUserId()) {
                    id = 1;
                } else {
                    Status retweet = status.getRetweetedStatus();
                    if (retweet != null && getUser().getId() == retweet.getUser().getId()) {
                        id = 1;
                    }
                }
                BaseFragment fragmen = (BaseFragment) mSectionsPagerAdapter
                        .findFragmentByPosition(id);
                if (fragmen != null) {
                    fragmen.add(Row.newStatus(status));
                }
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
                super.onDeletionNotice(statusDeletionNotice);
                for (int id = 0; id < 2; id++) {
                    BaseFragment fragmen = (BaseFragment) mSectionsPagerAdapter
                            .findFragmentByPosition(id);
                    if (fragmen != null) {
                        fragmen.removeStatus(statusDeletionNotice.getStatusId());
                    }
                }
            }

            @Override
            public void onFavorite(User source, User target, Status status) {
                // 自分の fav をタイムラインに反映
                if (source.getId() == getUser().getId()) {
                    BaseFragment fragmen = (BaseFragment) mSectionsPagerAdapter
                            .findFragmentByPosition(0);
                    fragmen.replaceStatus(status);
                    return;
                }
                final Row row = Row.newFavorite(source, target, status);

                // FIXME: 「つながり」的なタブができたらちゃんと実装する
                view.post(new Runnable() {
                    @Override
                    public void run() {
                        JustawayApplication.showToast(row.getSource().getScreenName() + " fav "
                                + row.getStatus().getText());
                    }
                });

                BaseFragment fragmen = (BaseFragment) mSectionsPagerAdapter
                        .findFragmentByPosition(1);
                fragmen.add(row);
            }

            @Override
            public void onUnfavorite(User arg0, User arg1, Status arg2) {

                final User source = arg0;
                final Status status = arg2;

                // 自分の unfav をタイムラインに反映
                if (source.getId() == getUser().getId()) {
                    BaseFragment fragmen = (BaseFragment) mSectionsPagerAdapter
                            .findFragmentByPosition(0);
                    fragmen.replaceStatus(status);
                    return;
                }

                // FIXME: 「つながり」的なタブができたらちゃんと実装する
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
                // TODO Auto-generated method stub
                super.onDirectMessage(directMessage);
                BaseFragment fragmen = (BaseFragment) mSectionsPagerAdapter
                        .findFragmentByPosition(2);
                if (fragmen != null) {
                    fragmen.add(Row.newDirectMessage(directMessage));
                }
            }

            @Override
            public void onDeletionNotice(long directMessageId, long userId) {
                super.onDeletionNotice(directMessageId, userId);
                DirectMessageFragment fragmen = (DirectMessageFragment) mSectionsPagerAdapter
                        .findFragmentByPosition(2);
                if (fragmen != null) {
                    fragmen.remove(directMessageId);
                }
            }
        };
    }

    public void doFavorite(Long id) {
        new FavoriteTask().execute(id);
    }

    public void doDestroyFavorite(Long id) {
        new UnFavoriteTask().execute(id);
    }

    public void doRetweet(Long id) {
        new RetweetTask().execute(id);
    }

    public void doDestroyStatus(long id) {
        new DestroyStatusTask().execute(id);
    }

    public void doDestroyDirectMessage(Long id) {
        new DestroyDirectMessageTask().execute(id);
        // 自分宛のDMを消してもStreaming APIで拾えないで自力で消す
        DirectMessageFragment fragmen = (DirectMessageFragment) mSectionsPagerAdapter
                .findFragmentByPosition(2);
        if (fragmen != null) {
            fragmen.remove(id);
        }
    }
}
