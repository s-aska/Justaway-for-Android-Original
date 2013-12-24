package info.justaway;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import info.justaway.fragment.profile.DescriptionFragment;
import info.justaway.fragment.profile.FavoritesListFragment;
import info.justaway.fragment.profile.FollowersListFragment;
import info.justaway.fragment.profile.FollowingListFragment;
import info.justaway.fragment.profile.SummaryFragment;
import info.justaway.fragment.profile.UserListMembershipsFragment;
import info.justaway.fragment.profile.UserTimelineFragment;
import info.justaway.model.Profile;
import info.justaway.task.ShowUserLoader;
import twitter4j.Relationship;
import twitter4j.Twitter;
import twitter4j.User;

public class ProfileActivity extends FragmentActivity implements
        LoaderManager.LoaderCallbacks<Profile> {

    private Twitter twitter;
    private ImageView banner;
    private User user;
    private int currentPosition = 0;
    private int blue;
    private int white;

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

        JustawayApplication application = JustawayApplication.getApplication();

        twitter = application.getTwitter();
        banner = (ImageView) findViewById(R.id.banner);
        banner.setImageResource(R.drawable.suzuri);

        blue = getResources().getColor(R.color.holo_blue_light);
        white = getResources().getColor(android.R.color.secondary_text_dark);

        ((TextView) findViewById(R.id.statusesCount)).setTextColor(blue);
        ((TextView) findViewById(R.id.statusesCountLabel)).setTextColor(blue);

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
            JustawayApplication.showToast("読み込みに失敗しました:;(∩´﹏`∩);:");
            return;
        }
        user = profile.getUser();
        if (user == null) {
            JustawayApplication.showToast("読み込みに失敗しました:;(∩´﹏`∩);:");
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

        String bannerUrl = user.getProfileBannerMobileRetinaURL();
        if (bannerUrl != null) {
            JustawayApplication.getApplication().displayImage(bannerUrl, banner);
        }

        Relationship relationship = profile.getRelationship();

        /**
         * スワイプで動かせるタブを実装するのに最低限必要な実装
         */
        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
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

        // ユーザリスト用のタブ
        final ViewPager listViewPager = (ViewPager) findViewById(R.id.listPager);
        SectionsPagerAdapter listPagerAdapter = new SectionsPagerAdapter(this, listViewPager);

        Bundle listArgs = new Bundle();
        listArgs.putSerializable("user", user);
        listPagerAdapter.addTab(UserTimelineFragment.class, listArgs);
        listPagerAdapter.addTab(FollowingListFragment.class, listArgs);
        listPagerAdapter.addTab(FollowersListFragment.class, listArgs);
        listPagerAdapter.addTab(UserListMembershipsFragment.class, listArgs);
        listPagerAdapter.addTab(FavoritesListFragment.class, listArgs);
        listPagerAdapter.notifyDataSetChanged();
        listViewPager.setOffscreenPageLimit(5);
        listViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        ((TextView) findViewById(R.id.statusesCount)).setTextColor(blue);
                        ((TextView) findViewById(R.id.statusesCountLabel)).setTextColor(blue);
                        break;
                    case 1:
                        ((TextView) findViewById(R.id.friendsCount)).setTextColor(blue);
                        ((TextView) findViewById(R.id.friendsCountLabel)).setTextColor(blue);
                        break;
                    case 2:
                        ((TextView) findViewById(R.id.followersCount)).setTextColor(blue);
                        ((TextView) findViewById(R.id.followersCountLabel)).setTextColor(blue);
                        break;
                    case 3:
                        ((TextView) findViewById(R.id.listedCount)).setTextColor(blue);
                        ((TextView) findViewById(R.id.listedCountLabel)).setTextColor(blue);
                        break;
                    case 4:
                        ((TextView) findViewById(R.id.favouritesCount)).setTextColor(blue);
                        ((TextView) findViewById(R.id.favouritesCountLabel)).setTextColor(blue);
                        break;
                }

                // 青くなってるのを取り消す処理
                switch (currentPosition) {
                    case 0:
                        ((TextView) findViewById(R.id.statusesCount)).setTextColor(white);
                        ((TextView) findViewById(R.id.statusesCountLabel)).setTextColor(white);
                        break;
                    case 1:
                        ((TextView) findViewById(R.id.friendsCount)).setTextColor(white);
                        ((TextView) findViewById(R.id.friendsCountLabel)).setTextColor(white);
                        break;
                    case 2:
                        ((TextView) findViewById(R.id.followersCount)).setTextColor(white);
                        ((TextView) findViewById(R.id.followersCountLabel)).setTextColor(white);
                        break;
                    case 3:
                        ((TextView) findViewById(R.id.listedCount)).setTextColor(white);
                        ((TextView) findViewById(R.id.listedCountLabel)).setTextColor(white);
                        break;
                    case 4:
                        ((TextView) findViewById(R.id.favouritesCount)).setTextColor(white);
                        ((TextView) findViewById(R.id.favouritesCountLabel)).setTextColor(white);
                        break;
                }
                currentPosition = position;
            }
        });

        findViewById(R.id.statuses).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listViewPager.setCurrentItem(0);
            }
        });
        findViewById(R.id.friends).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listViewPager.setCurrentItem(1);
            }
        });
        findViewById(R.id.friends).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listViewPager.setCurrentItem(1);
            }
        });
        findViewById(R.id.followers).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listViewPager.setCurrentItem(2);
            }
        });
        findViewById(R.id.listed).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listViewPager.setCurrentItem(3);
            }
        });
        findViewById(R.id.favourites).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listViewPager.setCurrentItem(4);
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

}