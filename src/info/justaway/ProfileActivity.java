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
import twitter4j.User;

public class ProfileActivity extends BaseActivity implements
        LoaderManager.LoaderCallbacks<Profile> {

    private ImageView mBanner;
    private User mUser;
    private int mCurrentPosition = 0;
    private int mColorBlue;
    private int mColorWhite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mColorBlue = getResources().getColor(R.color.holo_blue_light);
        mColorWhite = getResources().getColor(android.R.color.secondary_text_dark);

        mBanner = (ImageView) findViewById(R.id.banner);
        mBanner.setImageResource(R.drawable.suzuri);

        ((TextView) findViewById(R.id.statuses_count)).setTextColor(mColorBlue);
        ((TextView) findViewById(R.id.statuses_count_label)).setTextColor(mColorBlue);

        // インテント経由での起動をサポート
        Intent intent = getIntent();
        Bundle args = new Bundle(1);
        String screenName;
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
            JustawayApplication.showToast(R.string.toast_load_data_failure);
            return;
        }
        mUser = profile.getUser();
        if (mUser == null) {
            JustawayApplication.showToast(R.string.toast_load_data_failure);
            return;
        }
        ((TextView) findViewById(R.id.favourites_count)).setText(String.valueOf(mUser
                .getFavouritesCount()));
        ((TextView) findViewById(R.id.statuses_count)).setText(String.valueOf(mUser
                .getStatusesCount()));
        ((TextView) findViewById(R.id.friends_count)).setText(String.valueOf(mUser
                .getFriendsCount()));
        ((TextView) findViewById(R.id.followers_count)).setText(String.valueOf(mUser
                .getFollowersCount()));
        ((TextView) findViewById(R.id.listed_count)).setText(String.valueOf(mUser
                .getListedCount()));

        String bannerUrl = mUser.getProfileBannerMobileRetinaURL();
        if (bannerUrl != null) {
            JustawayApplication.getApplication().displayImage(bannerUrl, mBanner);
        }

        Relationship relationship = profile.getRelationship();

        /**
         * スワイプで動かせるタブを実装するのに最低限必要な実装
         */
        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        SectionsPagerAdapter pagerAdapter = new SectionsPagerAdapter(this, viewPager);

        Bundle args = new Bundle();
        args.putSerializable("user", mUser);
        args.putSerializable("relationship", relationship);
        pagerAdapter.addTab(SummaryFragment.class, args);
        pagerAdapter.addTab(DescriptionFragment.class, args);
        pagerAdapter.notifyDataSetChanged();
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    ((TextView) findViewById(R.id.symbol)).setText(getString(R.string.profile_pointer));
                } else {
                    ((TextView) findViewById(R.id.symbol)).setText(getString(R.string.profile_pointer_right));
                }
            }
        });

        // ユーザリスト用のタブ
        final ViewPager listViewPager = (ViewPager) findViewById(R.id.list_pager);
        SectionsPagerAdapter listPagerAdapter = new SectionsPagerAdapter(this, listViewPager);

        Bundle listArgs = new Bundle();
        listArgs.putSerializable("user", mUser);
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
                        ((TextView) findViewById(R.id.statuses_count)).setTextColor(mColorBlue);
                        ((TextView) findViewById(R.id.statuses_count_label)).setTextColor(mColorBlue);
                        break;
                    case 1:
                        ((TextView) findViewById(R.id.friends_count)).setTextColor(mColorBlue);
                        ((TextView) findViewById(R.id.friends_count_label)).setTextColor(mColorBlue);
                        break;
                    case 2:
                        ((TextView) findViewById(R.id.followers_count)).setTextColor(mColorBlue);
                        ((TextView) findViewById(R.id.followers_count_label)).setTextColor(mColorBlue);
                        break;
                    case 3:
                        ((TextView) findViewById(R.id.listed_count)).setTextColor(mColorBlue);
                        ((TextView) findViewById(R.id.listed_count_label)).setTextColor(mColorBlue);
                        break;
                    case 4:
                        ((TextView) findViewById(R.id.favourites_count)).setTextColor(mColorBlue);
                        ((TextView) findViewById(R.id.favourites_count_label)).setTextColor(mColorBlue);
                        break;
                }

                // 青くなってるのを取り消す処理
                switch (mCurrentPosition) {
                    case 0:
                        ((TextView) findViewById(R.id.statuses_count)).setTextColor(mColorWhite);
                        ((TextView) findViewById(R.id.statuses_count_label)).setTextColor(mColorWhite);
                        break;
                    case 1:
                        ((TextView) findViewById(R.id.friends_count)).setTextColor(mColorWhite);
                        ((TextView) findViewById(R.id.friends_count_label)).setTextColor(mColorWhite);
                        break;
                    case 2:
                        ((TextView) findViewById(R.id.followers_count)).setTextColor(mColorWhite);
                        ((TextView) findViewById(R.id.followers_count_label)).setTextColor(mColorWhite);
                        break;
                    case 3:
                        ((TextView) findViewById(R.id.listed_count)).setTextColor(mColorWhite);
                        ((TextView) findViewById(R.id.listed_count_label)).setTextColor(mColorWhite);
                        break;
                    case 4:
                        ((TextView) findViewById(R.id.favourites_count)).setTextColor(mColorWhite);
                        ((TextView) findViewById(R.id.favourites_count_label)).setTextColor(mColorWhite);
                        break;
                }
                mCurrentPosition = position;
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
        Intent intent;
        switch (item.getItemId()) {
            case R.id.open_twitter:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/"
                        + mUser.getScreenName()));
                startActivity(intent);
                break;
            case R.id.open_favstar:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://ja.favstar.fm/users/"
                        + mUser.getScreenName() + "/recent"));
                startActivity(intent);
                break;
            case R.id.open_twilog:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://twilog.org/"
                        + mUser.getScreenName()));
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