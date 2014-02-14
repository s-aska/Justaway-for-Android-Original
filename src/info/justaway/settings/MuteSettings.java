package info.justaway.settings;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

import info.justaway.JustawayApplication;
import twitter4j.Status;

public class MuteSettings {

    private MuteSettingsData mMuteSettingsData;
    private static final String PREF_NAME = "mute_settings";
    private static final String PREF_KEY = "data";
    private JustawayApplication mApplication;

    public MuteSettings() {
        mApplication = JustawayApplication.getApplication();
        loadMuteSettings();
    }

    public void loadMuteSettings() {
        SharedPreferences preferences = mApplication.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = preferences.getString(PREF_KEY, null);
        Gson gson = new Gson();
        if (json != null) {
            mMuteSettingsData = gson.fromJson(json, MuteSettingsData.class);
        } else {
            mMuteSettingsData = new MuteSettingsData();
            mMuteSettingsData.sourceMap = new HashMap<String, Boolean>();
            mMuteSettingsData.userMap = new HashMap<Long, String>();
            mMuteSettingsData.words = new ArrayList<String>();
        }
    }

    public void saveMuteSettings() {
        SharedPreferences preferences = mApplication.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String exportJson = gson.toJson(mMuteSettingsData);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREF_KEY, exportJson);
        editor.commit();
    }

    public Boolean isMute(Status status) {
        if (mMuteSettingsData.userMap.get(status.getUser().getId()) != null) {
            return true;

        }
        Status retweetedStatus = status.getRetweetedStatus();
        if (retweetedStatus != null) {
            if (mMuteSettingsData.userMap.get(retweetedStatus.getUser().getId()) != null) {
                return true;
            }
        }
        Status source = retweetedStatus != null ? retweetedStatus : status;
        if (mMuteSettingsData.sourceMap.get(mApplication.getClientName(source.getSource())) != null) {
            return true;
        }
        String text = source.getText();
        for (String word : getWords()) {
            if (text.contains(word)) {
                return true;
            }
        }
        return false;
    }

    public void addSource(String source) {
        mMuteSettingsData.sourceMap.put(source, true);
    }

    public void removeSource(String source) {
        mMuteSettingsData.sourceMap.remove(source);
    }

    public void addUser(Long userId, String screenName) {
        mMuteSettingsData.userMap.put(userId, screenName);
    }

    public void removeUser(Long userId) {
        mMuteSettingsData.userMap.remove(userId);
    }

    public void addWord(String word) {
        for (String muteWord : mMuteSettingsData.words) {
            if (muteWord.equals(word)) {
                return;
            }
        }
        mMuteSettingsData.words.add(word);
    }

    public void removeWord(String word) {
        mMuteSettingsData.words.remove(word);
    }

    public ArrayList<String> getSources() {
        ArrayList<String> sources = new ArrayList<String>();
        for (String key : mMuteSettingsData.sourceMap.keySet()) {
            sources.add(key);
        }
        return sources;
    }

    public HashMap<Long, String> getUserMap() {
        return mMuteSettingsData.userMap;
    }

    public ArrayList<String> getWords() {
        return mMuteSettingsData.words;
    }

    public static class MuteSettingsData {
        HashMap<String, Boolean> sourceMap;
        HashMap<Long, String> userMap; /* LongSparseArrayはJSON化するのに都合が悪いのでHashMapを使う */
        ArrayList<String> words;
    }
}
