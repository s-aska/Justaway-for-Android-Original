package info.justaway;

import android.app.ActionBar;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.Bind;
import butterknife.OnClick;
import info.justaway.adapter.SimplePagerAdapter;
import info.justaway.fragment.list.UserListStatusesFragment;
import info.justaway.fragment.list.UserMemberFragment;
import info.justaway.model.TwitterManager;
import info.justaway.model.UserListCache;
import info.justaway.util.MessageUtil;
import info.justaway.util.ThemeUtil;
import twitter4j.ResponseList;
import twitter4j.UserList;

public class UserListActivity extends FragmentActivity {

    @Bind(R.id.users_label) TextView mUsersLabel;
    @Bind(R.id.tweets_label) TextView mTweetsLabel;
    @Bind(R.id.list_pager) ViewPager mListPager;

    private UserList mUserList;
    private Boolean mIsFollowing;
    private int mCurrentPosition = 0;
    private int mColorBlue;
    private int mColorWhite;
    private final static int MENU_CREATE = 1;
    private final static int MENU_DESTROY = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeUtil.setTheme(this);
        setContentView(R.layout.activity_user_list);
        ButterKnife.bind(this);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        mUserList = (UserList) intent.getSerializableExtra("userList");
        if (mUserList == null) {
            return;
        }
        mIsFollowing = mUserList.isFollowing();

        mColorBlue = ThemeUtil.getThemeTextColor(R.attr.holo_blue);
        mColorWhite = ThemeUtil.getThemeTextColor(R.attr.text_color);
        mUsersLabel.setTextColor(mColorBlue);

        setTitle(mUserList.getFullName());

        /**
         * スワイプで動かせるタブを実装するのに最低限必要な実装
         */
        SimplePagerAdapter pagerAdapter = new SimplePagerAdapter(this, mListPager);
        Bundle args = new Bundle();
        args.putLong("listId", mUserList.getId());

        pagerAdapter.addTab(UserMemberFragment.class, args);
        pagerAdapter.addTab(UserListStatusesFragment.class, args);
        pagerAdapter.notifyDataSetChanged();
        mListPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    mUsersLabel.setTextColor(mColorBlue);
                } else {
                    mTweetsLabel.setTextColor(mColorBlue);
                }

                if (mCurrentPosition == 0) {
                    mUsersLabel.setTextColor(mColorWhite);
                } else {
                    mTweetsLabel.setTextColor(mColorWhite);
                }

                mCurrentPosition = position;
            }
        });
    }

    @OnClick(R.id.users_label)
    void onClickUsersLabel() {
        mListPager.setCurrentItem(0);
    }

    @OnClick(R.id.tweets_label)
    void onClickTweetsLabel() {
        mListPager.setCurrentItem(1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, MENU_CREATE, Menu.NONE, R.string.menu_create_user_list_subscription);
        menu.add(Menu.NONE, MENU_DESTROY, Menu.NONE, R.string.menu_destroy_user_list_subscription);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem create = menu.findItem(MENU_CREATE);
        MenuItem destroy = menu.findItem(MENU_DESTROY);
        if (create == null || destroy == null) {
            return false;
        }
        if (mIsFollowing) {
            create.setVisible(false);
            destroy.setVisible(true);
        } else {
            create.setVisible(true);
            destroy.setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case MENU_CREATE:
                new AsyncTask<Void, Void, Boolean>() {
                    @Override
                    protected Boolean doInBackground(Void... params) {
                        try {
                            TwitterManager.getTwitter().createUserListSubscription(mUserList.getId());
                            return true;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return false;
                        }
                    }

                    @Override
                    protected void onPostExecute(Boolean success) {
                        if (success) {
                            MessageUtil.showToast(R.string.toast_create_user_list_subscription_success);
                            mIsFollowing = true;
                            ResponseList<UserList> userLists = UserListCache.getUserLists();
                            if (userLists != null) {
                                userLists.add(0, mUserList);
                            }
                        } else {
                            MessageUtil.showToast(R.string.toast_create_user_list_subscription_failure);
                        }
                    }
                }.execute();
                break;
            case MENU_DESTROY:
                new AsyncTask<Void, Void, Boolean>() {
                    @Override
                    protected Boolean doInBackground(Void... params) {
                        try {
                            TwitterManager.getTwitter().destroyUserListSubscription(mUserList.getId());
                            return true;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return false;
                        }
                    }

                    @Override
                    protected void onPostExecute(Boolean success) {
                        if (success) {
                            MessageUtil.showToast(R.string.toast_destroy_user_list_subscription_success);
                            mIsFollowing = false;
                            ResponseList<UserList> userLists = UserListCache.getUserLists();
                            if (userLists != null) {
                                userLists.remove(mUserList);
                            }
                        } else {
                            MessageUtil.showToast(R.string.toast_destroy_user_list_subscription_failure);
                        }
                    }
                }.execute();
                break;
        }
        return true;
    }
}
