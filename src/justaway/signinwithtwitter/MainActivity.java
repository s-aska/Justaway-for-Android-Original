package justaway.signinwithtwitter;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterStream;
import twitter4j.User;
import twitter4j.UserStreamAdapter;
import android.R.color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {

    private Twitter twitter;
    private TwitterStream twitterStream;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager viewPager;
    private Status selectedStatus;
    private User user;

    /**
     * 自分自身のUserオブジェクト(Twitter) リプのタブでツイートが自分に対してのリプかどうかの判定などで使用している
     */
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Twitter REST API用のインスタンス
     */
    public Twitter getTwitter() {
        return twitter;
    }

    public void setTwitter(Twitter twitter) {
        this.twitter = twitter;
    }

    /**
     * Twitter Streaming API用のインスタンス
     */
    public TwitterStream getTwitterStream() {
        return twitterStream;
    }

    public void setTwitterStream(TwitterStream twitterStream) {
        this.twitterStream = twitterStream;
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
    public Status getSelectedStatus() {
        return selectedStatus;
    }

    public void setSelectedStatus(Status selectedStatus) {
        this.selectedStatus = selectedStatus;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // スリープさせない指定
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // アクセストークンがない場合に認証用のアクティビティを起動する
        if (!TwitterUtils.hasAccessToken(this)) {
            Intent intent = new Intent(this, SigninActivity.class);
            startActivity(intent);
            finish();
        } else {

            // とりあえず勝手にストリーミング開始するようにしている
            twitter = TwitterUtils.getTwitterInstance(this);
            twitterStream = TwitterUtils.getTwitterStreamInstance(this);
            twitterStream.addListener(getUserStreamAdapter());
            twitterStream.user();

            // 自分の user_id, screen_name を取得、頻繁に変える人もいるのでSharedPreferenceには保存しない
            new ProfileTask().execute();
        }

        /**
         * スワイプで動かせるタブを実装するのに最低限必要な実装
         */
        mSectionsPagerAdapter = new SectionsPagerAdapter(
                getSupportFragmentManager());
        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        setViewPager(viewPager);
        viewPager.setAdapter(mSectionsPagerAdapter);

        /**
         * タブは前後タブまでは状態が保持されるがそれ以上離れるとViewが破棄されてしまう、
         * あまりに使いづらいの上限を増やしている、指定値＋前後のタブまでが保持されるようになる
         * デフォルト値は1（表示しているタブの前後までしか保持されない）
         */
        viewPager.setOffscreenPageLimit(3);

        /**
         * 違うタブだったら移動、同じタブだったら最上部にスクロールという美しい実装
         * ActionBarのタブに頼っていない為、自力でsetCurrentItemでタブを動かしている
         * タブの切替がスワイプだけで良い場合はこの処理すら不要
         */
        findViewById(R.id.action_timeline).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BaseFragment f = mSectionsPagerAdapter.getItem(0);
                        int id = viewPager.getCurrentItem();
                        if (id != 0) {
                            viewPager.setCurrentItem(0);
                            if (f.isTop()) {
                                showTopView();
                            }
                        } else {
                            f.goToTop();
                        }
                    }
                });

        findViewById(R.id.action_interactions).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BaseFragment f = mSectionsPagerAdapter.getItem(1);
                        int id = viewPager.getCurrentItem();
                        if (id != 1) {
                            viewPager.setCurrentItem(1);
                            if (f.isTop()) {
                                showTopView();
                            }
                        } else {
                            f.goToTop();
                        }
                    }
                });

        findViewById(R.id.action_tweet).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(v.getContext(),
                                PostActivity.class);
                        startActivity(intent);
                    }
                });
    }

    @Override
    protected void onDestroy() {

        // ちゃんと接続を切らないとアプリが凍結されるらしい
        if (twitterStream != null) {
            twitterStream.cleanUp();
            twitterStream.shutdown();
        }
        super.onDestroy();
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
    public void onNewTInteractions(Boolean autoScroll) {
        // 表示中のタブかつ自動スクロール時はハイライトしない
        if (viewPager.getCurrentItem() == 1 && autoScroll == true) {
            return;
        }
        Button button = (Button) findViewById(R.id.action_interactions);
        button.setTextColor(getResources().getColor(color.holo_blue_bright));
    }

    /**
     * 新しいレコードを見たアピ
     */
    public void showTopView() {
        int id = viewPager.getCurrentItem() == 0 ? R.id.action_timeline
                : R.id.action_interactions;
        Button button = (Button) findViewById(id);
        button.setTextColor(getResources().getColor(color.white));
    }

    /**
     * タブの切替毎に必要なFragmentを取得するためのAdapterクラス
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private SparseArray<BaseFragment> fragments = new SparseArray<BaseFragment>();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public BaseFragment getItem(int position) {
            BaseFragment fragment = fragments.get(position);
            if (fragment != null) {
                return fragment;
            }
            if (position == 0) {
                fragment = (BaseFragment) new TimelineFragment();
            } else if (position == 1) {
                fragment = (BaseFragment) new InteractionsFragment();
            } else {

            }
            fragments.put(position, fragment);
            return fragment;
        }

        @Override
        public int getCount() {
            // タブ数
            return 2;
        }

        // @Override
        // public CharSequence getPageTitle(int position) {
        // switch (position) {
        // case 0:
        // return "Timeline";
        // case 1:
        // return "Interactions";
        // case 2:
        // return "...";
        // }
        // return null;
        // }
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
        System.out.println(itemId);
        if (itemId == R.id.signout) {
            TwitterUtils.resetAccessToken(this);
            finish();
        } else if (itemId == R.id.reload) {
            if (twitterStream != null) {
                twitterStream.cleanUp();
                twitterStream.shutdown();
                twitterStream.user();
            }
        }
        return true;
    }

    public void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
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
                 * 自分宛のリプかどうかで渡すタブをスイッチ
                 */
                int id = getUser().getId() == status.getInReplyToUserId() ? 1
                        : 0;
                BaseFragment fragmen = (BaseFragment) mSectionsPagerAdapter
                        .getItem(id);
                if (fragmen != null) {
                    fragmen.onStatus(status);
                }
            }

            @Override
            public void onFavorite(User arg0, User arg1, Status arg2) {

                final User source = arg0;
                final Status status = arg2;

                // 自分の fav に反応しない
                if (source.getId() == getUser().getId()) {
                    return;
                }

                // FIXME: 「つながり」的なタブができたらちゃんと実装する
                view.post(new Runnable() {
                    @Override
                    public void run() {
                        showToast(source.getScreenName() + " fav "
                                + status.getText());
                    }
                });
            }

            @Override
            public void onUnfavorite(User arg0, User arg1, Status arg2) {

                final User source = arg0;
                final Status status = arg2;

                // 自分の unfav に反応しない
                if (source.getId() == getUser().getId()) {
                    return;
                }

                // FIXME: 「つながり」的なタブができたらちゃんと実装する
                view.post(new Runnable() {
                    @Override
                    public void run() {
                        showToast(source.getScreenName() + " unfav "
                                + status.getText());
                    }
                });
            }
        };
    }

    private class ProfileTask extends AsyncTask<Void, Void, User> {

        @Override
        protected User doInBackground(Void... params) {
            try {
                User user = getTwitter().verifyCredentials();
                return user;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(User user) {
            if (user != null) {
                setUser(user);
            }
        }
    }

    public void doFavorite(Long id) {
        new FavoriteTask().execute(id);
    }

    public void doRetweet(Long id) {
        new RetweetTask().execute(id);
    }

    private class FavoriteTask extends AsyncTask<Long, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Long... params) {
            try {
                getTwitter().createFavorite(params[0]);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result == true) {
                showToast("ふぁぼに成功しました>゜))彡");
            } else {
                showToast("ふぁぼに失敗しました＞＜");
            }
        }
    }

    private class RetweetTask extends AsyncTask<Long, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Long... params) {
            Long super_sugoi = params[0];
            try {
                getTwitter().retweetStatus(super_sugoi);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                showToast("RTに成功しました>゜))彡");
            } else {
                showToast("RTに失敗しました＞＜");
            }
        }
    }
}
