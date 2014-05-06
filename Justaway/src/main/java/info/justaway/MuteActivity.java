package info.justaway;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import info.justaway.adapter.SimplePagerAdapter;
import info.justaway.fragment.mute.SourceFragment;
import info.justaway.fragment.mute.UserFragment;
import info.justaway.fragment.mute.WordFragment;
import info.justaway.util.ThemeUtil;

public class MuteActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeUtil.setTheme(this);
        setContentView(R.layout.activity_mute);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setOffscreenPageLimit(3); // 3だと不要なんだけど一応...

        SimplePagerAdapter simplePagerAdapter = new SimplePagerAdapter(this, viewPager);
        simplePagerAdapter.addTab(UserFragment.class, null);
        simplePagerAdapter.addTab(SourceFragment.class, null);
        simplePagerAdapter.addTab(WordFragment.class, null);
        simplePagerAdapter.notifyDataSetChanged();

        final int colorBlue = ThemeUtil.getThemeTextColor(this, R.attr.holo_blue);
        final int colorWhite = ThemeUtil.getThemeTextColor(this, R.attr.text_color);

        /**
         * タブのラベル情報を配列に入れておく
         */
        final TextView[] tabs = {
                (TextView) findViewById(R.id.tab_user),
                (TextView) findViewById(R.id.tab_source),
                (TextView) findViewById(R.id.tab_word),
        };

        /**
         * タップしたら移動
         */
        for (int i = 0; i < tabs.length; i++) {
            final int item = i;
            tabs[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    viewPager.setCurrentItem(item);
                }
            });
        }

        /**
         * 最初のタブを青くする
         */
        tabs[0].setTextColor(colorBlue);

        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {

                /**
                 * タブのindexと選択されたpositionを比較して色を設定
                 */
                for (int i = 0; i < tabs.length; i++) {
                    tabs[i].setTextColor(i == position ? colorBlue : colorWhite);
                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }
}
