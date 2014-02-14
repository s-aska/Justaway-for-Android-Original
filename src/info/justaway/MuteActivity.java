package info.justaway;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import info.justaway.adapter.SimplePagerAdapter;
import info.justaway.fragment.mute.SourceFragment;
import info.justaway.fragment.mute.UserFragment;
import info.justaway.fragment.mute.WordFragment;

public class MuteActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mute);

        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setOffscreenPageLimit(5);

        SimplePagerAdapter simplePagerAdapter = new SimplePagerAdapter(this, viewPager);
        simplePagerAdapter.addTab(UserFragment.class, null);
        simplePagerAdapter.addTab(SourceFragment.class, null);
        simplePagerAdapter.addTab(WordFragment.class, null);
        simplePagerAdapter.notifyDataSetChanged();


    }
}
