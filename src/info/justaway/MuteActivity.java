package info.justaway;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import info.justaway.adapter.SimplePagerAdapter;
import info.justaway.fragment.mute.SourceMuteFragment;
import info.justaway.fragment.mute.UserMuteFragment;
import info.justaway.fragment.mute.WordMuteFragment;

public class MuteActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mute);

        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setOffscreenPageLimit(5);

        SimplePagerAdapter simplePagerAdapter = new SimplePagerAdapter(this, viewPager);
        simplePagerAdapter.addTab(UserMuteFragment.class, null);
        simplePagerAdapter.addTab(SourceMuteFragment.class, null);
        simplePagerAdapter.addTab(WordMuteFragment.class, null);
        simplePagerAdapter.notifyDataSetChanged();


    }
}
