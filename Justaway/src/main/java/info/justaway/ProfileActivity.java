package info.justaway;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import info.justaway.adapter.SimplePagerAdapter;
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

public class ProfileActivity extends FragmentActivity implements
        LoaderManager.LoaderCallbacks<Profile> {

    private ImageView mBanner;
    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JustawayApplication.getApplication().setTheme(this);
        setContentView(R.layout.activity_profile);

        ActionBar actionBar = getActionBar();
        if (actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mBanner = (ImageView) findViewById(R.id.banner);

        Typeface fontello = JustawayApplication.getFontello();
        ((TextView) findViewById(R.id.collapse_label)).setTypeface(fontello);

        // インテント経由での起動をサポート
        Intent intent = getIntent();
        Bundle args = new Bundle(1);
        if (Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null) {
            args.putString("screenName", intent.getData().getLastPathSegment());
        } else {
            String screenName = intent.getStringExtra("screenName");
            if (screenName != null) {
                args.putString("screenName", screenName);
            } else {
                args.putLong("userId", intent.getLongExtra("userId", 0));
            }
        }
        JustawayApplication.showProgressDialog(this, getString(R.string.progress_loading));
        getSupportLoaderManager().initLoader(0, args, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        String text;
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.send_reply:
                intent = new Intent(this, PostActivity.class);
                text = "@" + mUser.getScreenName() + " ";
                intent.putExtra("status", text);
                intent.putExtra("selection", text.length());
                startActivity(intent);
                break;
            case R.id.send_direct_messages:
                intent = new Intent(this, PostActivity.class);
                text = "D " + mUser.getScreenName() + " ";
                intent.putExtra("status", text);
                intent.putExtra("selection", text.length());
                startActivity(intent);
                break;
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
            case R.id.report_spam:
                new AlertDialog.Builder(ProfileActivity.this)
                        .setTitle(R.string.confirm_report_spam)
                        .setPositiveButton(
                                R.string.button_report_spam,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        JustawayApplication.showProgressDialog(ProfileActivity.this, getString(R.string.progress_process));
                                        new ReportSpamTask().execute(mUser.getId());
                                    }
                                })
                        .setNegativeButton(
                                R.string.button_cancel,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                        .show();

                break;
            case R.id.create_block:
                new AlertDialog.Builder(ProfileActivity.this)
                        .setTitle(R.string.confirm_create_block)
                        .setPositiveButton(
                                R.string.button_create_block,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        JustawayApplication.showProgressDialog(ProfileActivity.this, getString(R.string.progress_process));
                                        new CreateBlockTask().execute(mUser.getId());
                                    }
                                })
                        .setNegativeButton(
                                R.string.button_cancel,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                        .show();

                break;
        }
        return true;
    }

    @Override
    public Loader<Profile> onCreateLoader(int arg0, Bundle args) {
        String screenName = args.getString("screenName");
        if (screenName != null) {
            return new ShowUserLoader(this, screenName);
        } else {
            return new ShowUserLoader(this, args.getLong("userId"));
        }
    }

    @Override
    public void onLoadFinished(Loader<Profile> arg0, Profile profile) {
        JustawayApplication.dismissProgressDialog();
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
        SimplePagerAdapter pagerAdapter = new SimplePagerAdapter(this, viewPager);

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
        SimplePagerAdapter listPagerAdapter = new SimplePagerAdapter(this, listViewPager);

        Bundle listArgs = new Bundle();
        listArgs.putSerializable("user", mUser);
        listPagerAdapter.addTab(UserTimelineFragment.class, listArgs);
        listPagerAdapter.addTab(FollowingListFragment.class, listArgs);
        listPagerAdapter.addTab(FollowersListFragment.class, listArgs);
        listPagerAdapter.addTab(UserListMembershipsFragment.class, listArgs);
        listPagerAdapter.addTab(FavoritesListFragment.class, listArgs);
        listPagerAdapter.notifyDataSetChanged();
        listViewPager.setOffscreenPageLimit(5);

        /**
         * タブのラベル情報を配列に入れておく
         */
        final TextView[] countTexts = {
                (TextView) findViewById(R.id.statuses_count),
                (TextView) findViewById(R.id.friends_count),
                (TextView) findViewById(R.id.followers_count),
                (TextView) findViewById(R.id.listed_count),
                (TextView) findViewById(R.id.favourites_count),
        };

        final TextView[] labelTexts = {
                (TextView) findViewById(R.id.statuses_count_label),
                (TextView) findViewById(R.id.friends_count_label),
                (TextView) findViewById(R.id.followers_count_label),
                (TextView) findViewById(R.id.listed_count_label),
                (TextView) findViewById(R.id.favourites_count_label),
        };

        final LinearLayout[] tabs = {
                (LinearLayout) findViewById(R.id.statuses),
                (LinearLayout) findViewById(R.id.friends),
                (LinearLayout) findViewById(R.id.followers),
                (LinearLayout) findViewById(R.id.listed),
                (LinearLayout) findViewById(R.id.favourites),
        };

        final int colorBlue = JustawayApplication.getApplication().getThemeTextColor(this, R.attr.holo_blue);
        final int colorWhite = JustawayApplication.getApplication().getThemeTextColor(this, R.attr.text_color);

        countTexts[0].setTextColor(colorBlue);
        labelTexts[0].setTextColor(colorBlue);

        listViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                /**
                 * タブのindexと選択されたpositionを比較して色を設定
                 */
                for (int i = 0; i < countTexts.length; i++) {
                    countTexts[i].setTextColor(i == position ? colorBlue : colorWhite);
                    labelTexts[i].setTextColor(i == position ? colorBlue : colorWhite);
                }
            }
        });

        for (int i = 0; i < tabs.length; i++) {
            final int finalI = i;
            tabs[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listViewPager.setCurrentItem(finalI);
                }
            });
        }

        findViewById(R.id.collapse).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View frame = findViewById(R.id.frame);
                if (frame.getVisibility() == View.VISIBLE) {
                    findViewById(R.id.frame).setVisibility(View.GONE);
                    ((TextView) findViewById(R.id.collapse_label)).setText(R.string.fontello_down);
                } else {
                    findViewById(R.id.frame).setVisibility(View.VISIBLE);
                    ((TextView) findViewById(R.id.collapse_label)).setText(R.string.fontello_up);
                }
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<Profile> arg0) {
    }

    public void restart() {
        Intent intent = new Intent();
        intent.setClass(this, ProfileActivity.class);
        intent.putExtra("userId", mUser.getId());
        startActivity(intent);
        finish();
    }

    private class ReportSpamTask extends AsyncTask<Long, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Long... params) {
            Long userId = params[0];
            try {
                JustawayApplication.getApplication().getTwitter().reportSpam(userId);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            JustawayApplication.dismissProgressDialog();
            if (success) {
                JustawayApplication.showToast(R.string.toast_report_spam_success);
                restart();
            } else {
                JustawayApplication.showToast(R.string.toast_report_spam_failure);
            }

        }
    }

    private class CreateBlockTask extends AsyncTask<Long, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Long... params) {
            Long userId = params[0];
            try {
                JustawayApplication.getApplication().getTwitter().createBlock(userId);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            JustawayApplication.dismissProgressDialog();
            if (success) {
                JustawayApplication.showToast(R.string.toast_create_block_success);
                restart();
            } else {
                JustawayApplication.showToast(R.string.toast_create_block_failure);
            }

        }
    }
}