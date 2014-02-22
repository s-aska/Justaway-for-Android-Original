package info.justaway.settings;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;

import info.justaway.JustawayApplication;

public class PostStockSettings {

    private JustawayApplication mApplication;
    private PostStockSettingsDate mPostStockSettingsDate;
    private static final String PREF_NAME = "post_settings";
    private static final String PREF_KEY = "data";

    private static final String DRAFT_LIST_FILE = "DraftListFile";

    public PostStockSettings() {
        mApplication = JustawayApplication.getApplication();
        loadPostStockSettings();
    }

    public void loadPostStockSettings() {

        // 下書きの保存の仕方を移行
        SaveLoadTraining saveLoadTraining = new SaveLoadTraining(DRAFT_LIST_FILE);
        ArrayList<String> draftList = saveLoadTraining.loadArray();
        if (!draftList.isEmpty()) {
            mPostStockSettingsDate = new PostStockSettingsDate();
            mPostStockSettingsDate.hashtags = new ArrayList<String>();
            mPostStockSettingsDate.drafts = draftList;
            savePostStockSettings();

            SharedPreferences file = mApplication.getSharedPreferences(DRAFT_LIST_FILE, 0);
            file.edit().clear().commit();
        }

        SharedPreferences preferences = mApplication.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = preferences.getString(PREF_KEY, null);
        Gson gson = new Gson();
        if (json != null) {
            mPostStockSettingsDate = gson.fromJson(json, PostStockSettingsDate.class);
        } else {
            mPostStockSettingsDate = new PostStockSettingsDate();
            mPostStockSettingsDate.hashtags = new ArrayList<String>();
            mPostStockSettingsDate.drafts = new ArrayList<String>();

        }
    }

    public void savePostStockSettings() {
        SharedPreferences preferences = mApplication.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String exportJson = gson.toJson(mPostStockSettingsDate);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREF_KEY, exportJson);
        editor.commit();
    }

    public void addHashtag(String hashtag) {
        for (String muteWord : mPostStockSettingsDate.hashtags) {
            if (muteWord.equals(hashtag)) {
                return;
            }
        }
        mPostStockSettingsDate.hashtags.add(hashtag);
        savePostStockSettings();
    }

    public void removeHashtag(String hashtag) {
        mPostStockSettingsDate.hashtags.remove(hashtag);
        savePostStockSettings();
    }

    public void addDraft(String draft) {
        for (String muteWord : mPostStockSettingsDate.hashtags) {
            if (muteWord.equals(draft)) {
                return;
            }
        }
        mPostStockSettingsDate.drafts.add(draft);
        savePostStockSettings();
    }

    public void removeDraft(String draft) {
        mPostStockSettingsDate.drafts.remove(draft);
        savePostStockSettings();
    }

    public ArrayList<String> getHashtags() {
        return mPostStockSettingsDate.hashtags;
    }

    public ArrayList<String> getDrafts() {
        return mPostStockSettingsDate.drafts;
    }

    public static class PostStockSettingsDate {
        ArrayList<String> hashtags;
        ArrayList<String> drafts;
    }

    /**
     * SharedPreferencesにArrayListを突っ込む
     */
    public class SaveLoadTraining {

        private String prefsName;
        private ArrayList<String> list;

        public SaveLoadTraining(String prefsName) {
            this.prefsName = prefsName;
        }

        public ArrayList<String> loadArray() {
            SharedPreferences file = mApplication.getSharedPreferences(prefsName, 0);
            list = new ArrayList<String>();
            int size = file.getInt("list_size", 0);

            for (int i = 0; i < size; i++) {
                String draft = file.getString("list_" + i, null);
                list.add(draft);
            }
            return list;
        }
    }
}
