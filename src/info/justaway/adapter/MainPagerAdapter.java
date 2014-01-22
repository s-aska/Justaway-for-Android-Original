package info.justaway.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;

import info.justaway.JustawayApplication;
import info.justaway.R;
import info.justaway.fragment.main.BaseFragment;

/**
 * MainActivityで使うPagerAdapter
 */
public class MainPagerAdapter extends FragmentPagerAdapter {
    private final Context mContext;
    private final ViewPager mViewPager;
    private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
    private static String sDefaultListName;

    private static final class TabInfo {
        private final Class<?> mClazz;
        private final Bundle mArgs;
        private String mTabTitle;
        private final int mId;

        /**
         * タブ内のActivity、引数を設定する。
         *
         * @param clazz    タブに表示するFragment
         * @param args     タブに表示するFragmentに対する引数
         * @param tabTitle タブのタイトル
         * @param id       タブの識別子、タブの取得に利用する
         */
        TabInfo(Class<?> clazz, Bundle args, String tabTitle, int id) {
            this.mClazz = clazz;
            this.mArgs = args;
            this.mTabTitle = tabTitle;
            this.mId = id;
        }
    }

    public MainPagerAdapter(FragmentActivity context, ViewPager viewPager) {
        super(context.getSupportFragmentManager());
        viewPager.setAdapter(this);
        mContext = context;
        mViewPager = viewPager;
        sDefaultListName = context.getString(R.string.title_default_list);
    }

    @Override
    public BaseFragment getItem(int position) {
        TabInfo info = mTabs.get(position);
        return (BaseFragment) Fragment.instantiate(mContext, info.mClazz.getName(), info.mArgs);
    }

    @Override
    public long getItemId(int position) {
        return mTabs.get(position).mId;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public BaseFragment findFragmentByPosition(int position) {
        return (BaseFragment) instantiateItem(mViewPager, position);
    }

    public int findPositionById(int id) {
        int position = 0;
        for (TabInfo tab : mTabs) {
            if (tab.mId == id) {
                return position;
            }
            position++;
        }
        return -1;
    }

    public BaseFragment findFragmentById(int id) {
        int position = 0;
        for (TabInfo tab : mTabs) {
            if (tab.mId == id) {
                return (BaseFragment) instantiateItem(mViewPager, position);
            }
            position++;
        }
        return null;
    }

    /**
     * タブ追加
     *
     * @param clazz    タブに表示するFragment
     * @param args     タブに表示するFragmentに対する引数
     * @param tabTitle タブのタイトル
     * @param id       タブの識別子、タブの取得に利用する
     */
    public void addTab(Class<?> clazz, Bundle args, String tabTitle, Integer id) {
        TabInfo info = new TabInfo(clazz, args, tabTitle, id);
        mTabs.add(info);
    }

    public void removeTab(int position) {
        mTabs.remove(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        TabInfo tab = mTabs.get(position);
        JustawayApplication application = JustawayApplication.getApplication();
        if (tab.mTabTitle.equals(sDefaultListName)) {
            int userListId = tab.mArgs.getInt("userListId");
            String listName = application.getUserListName(userListId);
            String listUser = application.getUserListScreenName(userListId);
            if (listName != null) {
                if (listUser.equals(JustawayApplication.getApplication().getScreenName())) {
                    tab.mTabTitle = sDefaultListName + " " + listName;
                } else {
                tab.mTabTitle = sDefaultListName + " " + listUser+"/"+listName;
                }
            }
        }
        return tab.mTabTitle;
    }

    @Override
    public int getCount() {
        return mTabs.size();
    }
}
