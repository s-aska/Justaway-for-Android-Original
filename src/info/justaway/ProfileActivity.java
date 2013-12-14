package info.justaway;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import info.justaway.adapter.TwitterAdapter;
import info.justaway.fragment.profile.DescriptionFragment;
import info.justaway.fragment.profile.SummaryFragment;
import info.justaway.model.Profile;
import info.justaway.model.Row;
import info.justaway.task.ShowUserLoader;
import twitter4j.Paging;
import twitter4j.Relationship;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.User;

public class ProfileActivity extends FragmentActivity implements
        LoaderManager.LoaderCallbacks<Profile> {

    private Context context;
    private Twitter twitter;
    private ImageView banner;
    private User user;
    private JustawayApplication application;
    private TwitterAdapter adapter;
    private ListView listView;
    private ProgressBar mFooter;
    private int currentPage = 1;
    private int nextPage = 1;

    /**
     * Twitter REST API用のインスタンス
     */
    public Twitter getTwitter() {
        return twitter;
    }

    public void setTwitter(Twitter twitter) {
        this.twitter = twitter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        context = this;

        application = JustawayApplication.getApplication();

        twitter = application.getTwitter();
        banner = (ImageView) findViewById(R.id.banner);
        banner.setImageResource(R.drawable.suzuri);

        // リストビューの設定
        listView = (ListView) findViewById(R.id.listView);

        // コンテキストメニューを使える様にする為の指定、但しデフォルトではロングタップで開く
        registerForContextMenu(listView);

        mFooter = (ProgressBar) findViewById(R.id.guruguru);
        mFooter.setVisibility(View.GONE);

        // Status(ツイート)をViewに描写するアダプター
        adapter = new TwitterAdapter(context, R.layout.row_tweet);
        listView.setAdapter(adapter);

        // シングルタップでコンテキストメニューを開くための指定
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.showContextMenu();
            }
        });

        // インテント経由での起動をサポート
        Intent intent = getIntent();
        Bundle args = new Bundle(1);
        String screenName = null;
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            screenName = intent.getData().getLastPathSegment();
        } else {
            screenName = intent.getStringExtra("screenName");
        }
        args.putString("screenName", screenName);
        getSupportLoaderManager().initLoader(0, args, this);
        new UserTimelineTask().execute(screenName);

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // 最後までスクロールされたかどうかの判定
                if (totalItemCount == firstVisibleItem + visibleItemCount) {
                    additionalReading();
                }
            }
        });
    }

    private void additionalReading() {
        // 次のページあるのか確認
        if (currentPage != nextPage) {
            mFooter.setVisibility(View.VISIBLE);
            currentPage++;
            new UserTimelineTask().execute(user.getScreenName().toString());
        }
        return;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        JustawayApplication application = JustawayApplication.getApplication();
        application.onCreateContextMenuForStatus(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        JustawayApplication application = JustawayApplication.getApplication();
        return application.onContextItemSelected(this, item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile, menu);
        return true;
    }

    @Override
    public Loader<Profile> onCreateLoader(int arg0, Bundle args) {
        String screenName = args.getString("screenName");
        return new ShowUserLoader(this, screenName);
    }

    @Override
    public void onLoadFinished(Loader<Profile> arg0, Profile profile) {
        if (profile == null) {
            application.showToast("読み込みに失敗しました:;(∩´﹏`∩);:");
            return;
        }
        user = profile.getUser();
        if (user == null) {
            application.showToast("読み込みに失敗しました:;(∩´﹏`∩);:");
            return;
        }
        ((TextView) findViewById(R.id.favouritesCount)).setText(String.valueOf(user
                .getFavouritesCount()));
        ((TextView) findViewById(R.id.statusesCount)).setText(String.valueOf(user
                .getStatusesCount()));
        ((TextView) findViewById(R.id.friendsCount)).setText(String.valueOf(user
                .getFriendsCount()));
        ((TextView) findViewById(R.id.followersCount)).setText(String.valueOf(user
                .getFollowersCount()));
        ((TextView) findViewById(R.id.listedCount)).setText(String.valueOf(user
                .getListedCount()));

        final View frame = findViewById(R.id.frame);
        findViewById(R.id.statuses).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (frame.getVisibility() == View.VISIBLE) {
                    frame.setVisibility(View.GONE);
                } else {
                    frame.setVisibility(View.VISIBLE);
                }
            }
        });

        String bannerUrl = user.getProfileBannerMobileRetinaURL();
        if (bannerUrl != null) {
            JustawayApplication.getApplication().displayImage(bannerUrl, banner);
        }

        Relationship relationship = profile.getRelationship();

        /**
         * スワイプで動かせるタブを実装するのに最低限必要な実装
         */
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        SectionsPagerAdapter pagerAdapter = new SectionsPagerAdapter(this, viewPager);

        Bundle args = new Bundle();
        args.putSerializable("user", user);
        args.putSerializable("relationship", relationship);
        pagerAdapter.addTab(SummaryFragment.class, args);
        pagerAdapter.addTab(DescriptionFragment.class, args);
        pagerAdapter.notifyDataSetChanged();
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    ((TextView) findViewById(R.id.symbol)).setText("●  ○");
                } else {
                    ((TextView) findViewById(R.id.symbol)).setText("○  ●");
                }
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<Profile> arg0) {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = null;
        switch (item.getItemId()) {
            case R.id.open_twitter:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/"
                        + user.getScreenName()));
                startActivity(intent);
                break;
            case R.id.open_favstar:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://ja.favstar.fm/users/"
                        + user.getScreenName() + "/recent"));
                startActivity(intent);
                break;
            case R.id.open_twilog:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://twilog.org/"
                        + user.getScreenName()));
                startActivity(intent);
                break;
        }
        return true;
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

            /**
             * タブ内のActivity、引数を設定する。
             *
             * @param clazz タブ内のv4.Fragment
             * @param args  タブ内のv4.Fragmentに対する引数
             */
            TabInfo(Class<?> clazz, Bundle args) {
                this.clazz = clazz;
                this.args = args;
            }
        }

        public SectionsPagerAdapter(FragmentActivity context, ViewPager viewPager) {
            super(context.getSupportFragmentManager());
            viewPager.setAdapter(this);
            mContext = context;
            mViewPager = viewPager;
        }

        @Override
        public Fragment getItem(int position) {
            TabInfo info = mTabs.get(position);
            return Fragment.instantiate(mContext, info.clazz.getName(), info.args);
        }

        /**
         * タブ内に起動するActivity、引数、タイトルを設定する
         *
         * @param clazz 起動するv4.Fragmentクラス
         * @param args  v4.Fragmentに対する引数
         */
        public void addTab(Class<?> clazz, Bundle args) {
            TabInfo info = new TabInfo(clazz, args);
            mTabs.add(info);
        }

        @Override
        public int getCount() {
            // タブ数
            return mTabs.size();
        }
    }

    private class UserTimelineTask extends AsyncTask<String, Void, ResponseList<twitter4j.Status>> {
        @Override
        protected ResponseList<twitter4j.Status> doInBackground(String... params) {
            try {
                ResponseList<twitter4j.Status> statuses = JustawayApplication.getApplication().getTwitter().getUserTimeline(params[0], new Paging(nextPage));
                return statuses;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ResponseList<twitter4j.Status> statuses) {
            for (twitter4j.Status status : statuses) {
                adapter.add(Row.newStatus(status));
            }
            mFooter.setVisibility(View.GONE);
            nextPage++;
        }
    }
}