package info.justaway.adapter.main;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;

import info.justaway.JustawayApplication;
import info.justaway.R;
import info.justaway.fragment.main.tab.BaseFragment;
import info.justaway.model.AccessTokenManager;
import info.justaway.model.TabManager;
import info.justaway.model.UserListCache;
import twitter4j.UserList;

/**
 * MainActivityで使うPagerAdapter
 */
public class MainPagerAdapter extends FragmentPagerAdapter {
    private final Context mContext;
    private final ViewPager mViewPager;
    private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

    private static final class TabInfo {
        private final Class<? extends Fragment> mClazz;
        private final Bundle mArgs;
        private String mTabTitle;
        private String mSearchWord;
        private final long mId;

        /**
         * タブ内のActivity、引数を設定する。
         *
         * @param clazz    タブに表示するFragment
         * @param args     タブに表示するFragmentに対する引数
         * @param tabTitle タブのタイトル
         * @param id       タブの識別子、タブの取得に利用する
         */
        TabInfo(Class<? extends Fragment> clazz, Bundle args, String tabTitle, long id) {
            mClazz = clazz;
            mArgs = args;
            mTabTitle = tabTitle;
            mId = id;
        }

        TabInfo(Class<? extends Fragment> clazz, Bundle args, String tabTitle, long id, String searchWord) {
            mClazz = clazz;
            mArgs = args;
            mTabTitle = tabTitle;
            mId = id;
            mSearchWord = searchWord;
        }
    }

    public MainPagerAdapter(FragmentActivity context, ViewPager viewPager) {
        super(context.getSupportFragmentManager());
        viewPager.setAdapter(this);
        mContext = context;
        mViewPager = viewPager;
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

    public int findPositionById(long id) {
        int position = 0;
        for (TabInfo tab : mTabs) {
            if (tab.mId == id) {
                return position;
            }
            position++;
        }
        return -1;
    }

    public int findPositionBySearchWord(String searchWord) {
        int position = 0;
        for (TabInfo tab : mTabs) {
            if (tab.mId == TabManager.SEARCH_TAB_ID && searchWord.equals(tab.mSearchWord)) {
                return position;
            }
            position++;
        }
        return -1;
    }

    public BaseFragment findFragmentById(long id) {
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
    public void addTab(Class<? extends Fragment> clazz, Bundle args, String tabTitle, long id) {
        TabInfo info = new TabInfo(clazz, args, tabTitle, id);
        mTabs.add(info);
    }

    public void addTab(Class<? extends Fragment> clazz, Bundle args, String tabTitle, long id, String searchWord) {
        TabInfo info = new TabInfo(clazz, args, tabTitle, id, searchWord);
        mTabs.add(info);
    }

    public void clearTab() {
        mTabs.clear();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        TabInfo tab = mTabs.get(position);
        if (tab.mTabTitle.equals("-")) {
            UserList userList = UserListCache.getUserList(tab.mArgs.getInt("userListId"));
            if (userList != null) {
                if (userList.getUser().getId() == AccessTokenManager.getUserId()) {
                    tab.mTabTitle = userList.getName();
                } else {
                    tab.mTabTitle = userList.getFullName();
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
