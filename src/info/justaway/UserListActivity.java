package info.justaway;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TextView;

import info.justaway.adapter.SimplePagerAdapter;
import info.justaway.fragment.list.UserListStatusesFragment;
import info.justaway.fragment.list.UserMemberFragment;

public class UserListActivity extends BaseActivity {

    private int mListId;
    private int mCurrentPosition = 0;
    private int mColorBlue;
    private int mColorWhite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        Intent intent = getIntent();
        mListId = intent.getIntExtra("listId", 0);

        mColorBlue = getResources().getColor(R.color.holo_blue_light);
        mColorWhite = getResources().getColor(android.R.color.secondary_text_dark);
        ((TextView) findViewById(R.id.users_label)).setTextColor(mColorBlue);

        setTitle(intent.getStringExtra("listName"));

        /**
         * スワイプで動かせるタブを実装するのに最低限必要な実装
         */
        final ViewPager viewPager = (ViewPager) findViewById(R.id.list_pager);
        SimplePagerAdapter pagerAdapter = new SimplePagerAdapter(this, viewPager);

        Bundle args = new Bundle();
        args.putSerializable("listId", mListId);
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
}
