package info.justaway.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;

/**
 * タブの切替毎に必要なFragmentを取得するためのAdapterクラス
 */
public class SimplePagerAdapter extends FragmentPagerAdapter {
    private final Context mContext;
    private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

    private static final class TabInfo {
        private final Class<? extends Fragment> clazz;
        private final Bundle args;

        /**
         * タブ内のActivity、引数を設定する。
         *
         * @param clazz タブ内のv4.Fragment
         * @param args  タブ内のv4.Fragmentに対する引数
         */
        TabInfo(Class<? extends Fragment> clazz, Bundle args) {
            this.clazz = clazz;
            this.args = args;
        }
    }

    public SimplePagerAdapter(FragmentActivity context, ViewPager viewPager) {
        super(context.getSupportFragmentManager());
        viewPager.setAdapter(this);
        mContext = context;
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
    public void addTab(Class<? extends Fragment> clazz, Bundle args) {
        TabInfo info = new TabInfo(clazz, args);
        mTabs.add(info);
    }

    @Override
    public int getCount() {
        // タブ数
        return mTabs.size();
    }
}