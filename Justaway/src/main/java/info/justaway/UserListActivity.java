package info.justaway;

import android.app.ActionBar;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import info.justaway.adapter.SimplePagerAdapter;
import info.justaway.fragment.list.UserListStatusesFragment;
import info.justaway.fragment.list.UserMemberFragment;
import twitter4j.ResponseList;
import twitter4j.UserList;

public class UserListActivity extends FragmentActivity {

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
        setContentView(R.layout.activity_user_list);

        ActionBar actionBar = getActionBar();
        if (actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        mUserList = (UserList) intent.getSerializableExtra("userList");
        if (mUserList == null) {
            return;
        }
        mIsFollowing = mUserList.isFollowing();

        mColorBlue = getResources().getColor(R.color.holo_blue_light);
        mColorWhite = getResources().getColor(android.R.color.secondary_text_dark);
        ((TextView) findViewById(R.id.users_label)).setTextColor(mColorBlue);

        setTitle(mUserList.getFullName());

        /**
         * スワイプで動かせるタブを実装するのに最低限必要な実装
         */
        final ViewPager viewPager = (ViewPager) findViewById(R.id.list_pager);
        SimplePagerAdapter pagerAdapter = new SimplePagerAdapter(this, viewPager);

        Bundle args = new Bundle();
        args.putLong("listId", mUserList.getId());

        pagerAdapter.addTab(UserMemberFragment.class, args);
        pagerAdapter.addTab(UserListStatusesFragment.class, args);
        pagerAdapter.notifyDataSetChanged();
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    ((TextView) findViewById(R.id.users_label)).setTextColor(mColorBlue);
                } else {
                    ((TextView) findViewById(R.id.tweets_label)).setTextColor(mColorBlue);
                }

                if (mCurrentPosition == 0) {
                    ((TextView) findViewById(R.id.users_label)).setTextColor(mColorWhite);
                } else {
                    ((TextView) findViewById(R.id.tweets_label)).setTextColor(mColorWhite);
                }

                mCurrentPosition = position;
            }
        });

        findViewById(R.id.users_label).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(0);
            }
        });
        findViewById(R.id.tweets_label).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(1);
            }
        });
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
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
        } else if (itemId == MENU_CREATE) {
            new AsyncTask<Void, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Void... params) {
                    try {
                        JustawayApplication.getApplication().getTwitter().createUserListSubscription(mUserList.getId());
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }

                @Override
                protected void onPostExecute(Boolean success) {
                    if (success) {
                        JustawayApplication.showToast(R.string.toast_create_user_list_subscription_success);
                        mIsFollowing = true;
                        ResponseList<UserList> userLists = JustawayApplication.getApplication().getUserLists();
                        if (userLists != null) {
                            userLists.add(0, mUserList);
                        }
                    } else {
                        JustawayApplication.showToast(R.string.toast_create_user_list_subscription_failure);
                    }
                }
            }.execute();
        } else if (itemId == MENU_DESTROY) {
            new AsyncTask<Void, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Void... params) {
                    try {
                        JustawayApplication.getApplication().getTwitter().destroyUserListSubscription(mUserList.getId());
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }

                @Override
                protected void onPostExecute(Boolean success) {
                    if (success) {
                        JustawayApplication.showToast(R.string.toast_destroy_user_list_subscription_success);
                        mIsFollowing = false;
                        ResponseList<UserList> userLists = JustawayApplication.getApplication().getUserLists();
                        if (userLists != null) {
                            userLists.remove(mUserList);
                        }
                    } else {
                        JustawayApplication.showToast(R.string.toast_destroy_user_list_subscription_failure);
                    }
                }
            }.execute();
        }
        return true;
    }


}
