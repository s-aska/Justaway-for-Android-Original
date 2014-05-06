package info.justaway.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;

import info.justaway.JustawayApplication;
import info.justaway.R;

public class TabManager {

    public static final long TIMELINE_TAB_ID = -1L;
    public static final long INTERACTIONS_TAB_ID = -2L;
    public static final long DIRECT_MESSAGES_TAB_ID = -3L;
    private static final String TABS = "tabs-";
    private ArrayList<Tab> mTabs = new ArrayList<Tab>();

    public ArrayList<Tab> loadTabs() {
        mTabs.clear();
        JustawayApplication application = JustawayApplication.getApplication();
        SharedPreferences preferences = application.getSharedPreferences("settings", Context.MODE_PRIVATE);
        String json = preferences.getString(TABS.concat(String.valueOf(application.getUserId())).concat("/v2"), null);
        if (json != null) {
            Gson gson = new Gson();
            TabData tabData = gson.fromJson(json, TabData.class);
            mTabs = tabData.tabs;
        }
        if (mTabs.size() == 0) {
            mTabs = generalTabs();
        }
        return mTabs;
    }

    public void saveTabs(ArrayList<Tab> tabs) {
        JustawayApplication application = JustawayApplication.getApplication();
        TabData tabData = new TabData();
        tabData.tabs = tabs;
        Gson gson = new Gson();
        String json = gson.toJson(tabData);
        SharedPreferences preferences = application.getSharedPreferences("settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(TABS.concat(String.valueOf(application.getUserId())));
        editor.putString(TABS.concat(String.valueOf(application.getUserId())).concat("/v2"), json);
        editor.commit();
        mTabs = tabs;
    }

    public ArrayList<Tab> generalTabs() {
        ArrayList<Tab> tabs = new ArrayList<Tab>();
        tabs.add(new Tab(TIMELINE_TAB_ID));
        tabs.add(new Tab(INTERACTIONS_TAB_ID));
        tabs.add(new Tab(DIRECT_MESSAGES_TAB_ID));
        return tabs;
    }

    public static class TabData {
        ArrayList<Tab> tabs;
    }

    public static class Tab {
        public Long id;
        public String name;

        public Tab(Long id) {
            this.id = id;
        }

        public String getName() {
            if (id == TIMELINE_TAB_ID) {
                return JustawayApplication.getApplication().getString(R.string.title_main);
            } else if (id == INTERACTIONS_TAB_ID) {
                return JustawayApplication.getApplication().getString(R.string.title_interactions);
            } else if (id == DIRECT_MESSAGES_TAB_ID) {
                return JustawayApplication.getApplication().getString(R.string.title_direct_messages);
            } else {
                return name;
            }
        }

        public int getIcon() {
            if (id == TIMELINE_TAB_ID) {
                return R.string.fontello_home;
            } else if (id == INTERACTIONS_TAB_ID) {
                return R.string.fontello_at;
            } else if (id == DIRECT_MESSAGES_TAB_ID) {
                return R.string.fontello_mail;
            } else {
                return R.string.fontello_list;
            }
        }
    }

    public boolean hasTabId(Long findTab) {
        for (Tab tab : mTabs) {
            if (tab.id.equals(findTab)) {
                return true;
            }
        }
        return false;
    }
}
