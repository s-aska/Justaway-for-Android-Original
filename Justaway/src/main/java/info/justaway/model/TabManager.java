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
    public static final long FAVORITES_TAB_ID = -4L;
    public static final long SEARCH_TAB_ID = -5L;
    private static final String TABS = "tabs-";
    private static ArrayList<Tab> sTabs = new ArrayList<>();

    private static SharedPreferences getSharedPreferences() {
        return JustawayApplication.getApplication()
                .getSharedPreferences("settings", Context.MODE_PRIVATE);
    }

    public static ArrayList<Tab> loadTabs() {
        sTabs.clear();
        String json = getSharedPreferences().getString(TABS.concat(
                String.valueOf(AccessTokenManager.getUserId())).concat("/v2"), null);
        if (json != null) {
            Gson gson = new Gson();
            TabData tabData = gson.fromJson(json, TabData.class);
            sTabs = tabData.tabs;
        }
        if (sTabs.size() == 0) {
            sTabs = generalTabs();
        }
        return sTabs;
    }

    public static void saveTabs(ArrayList<Tab> tabs) {
        TabData tabData = new TabData();
        tabData.tabs = tabs;
        Gson gson = new Gson();
        String json = gson.toJson(tabData);
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.remove(TABS.concat(String.valueOf(AccessTokenManager.getUserId())));
        editor.putString(TABS.concat(String.valueOf(AccessTokenManager.getUserId())).concat("/v2"), json);
        editor.apply();
        sTabs = tabs;
    }

    public static ArrayList<Tab> generalTabs() {
        ArrayList<Tab> tabs = new ArrayList<>();
        tabs.add(new Tab(TIMELINE_TAB_ID));
        tabs.add(new Tab(INTERACTIONS_TAB_ID));
        tabs.add(new Tab(DIRECT_MESSAGES_TAB_ID));
        tabs.add(new Tab(FAVORITES_TAB_ID));
        return tabs;
    }

    public static boolean hasTabId(Long findTab) {
        for (Tab tab : sTabs) {
            if (tab.id.equals(findTab)) {
                return true;
            }
        }
        return false;
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
            } else if (id == FAVORITES_TAB_ID) {
                return JustawayApplication.getApplication().getString(R.string.title_favorites);
            } else if (id == SEARCH_TAB_ID) {
                return JustawayApplication.getApplication().getString(R.string.title_search) + ":" + name;
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
            } else if (id == FAVORITES_TAB_ID) {
                return R.string.fontello_star;
            } else if (id == SEARCH_TAB_ID) {
                return R.string.fontello_search;
            } else {
                return R.string.fontello_list;
            }
        }
    }
}
