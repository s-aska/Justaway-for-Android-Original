package info.justaway.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

import info.justaway.JustawayApplication;
import info.justaway.model.Row;
import info.justaway.util.StatusUtil;
import twitter4j.Status;
import twitter4j.UserMentionEntity;

public class MuteSettings {

    private static MuteSettingsData sMuteSettingsData;
    private static final String PREF_NAME = "mute_settings";
    private static final String PREF_KEY = "data";

    public static void init() {
        loadMuteSettings();
    }

    private static SharedPreferences getSharedPreferences() {
        return JustawayApplication.getApplication()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    @SuppressLint("UseSparseArrays")
    public static void loadMuteSettings() {
        String json = getSharedPreferences().getString(PREF_KEY, null);
        Gson gson = new Gson();
        if (json != null) {
            sMuteSettingsData = gson.fromJson(json, MuteSettingsData.class);
        }
        if (sMuteSettingsData == null) {
            sMuteSettingsData = new MuteSettingsData();
        }
        if (sMuteSettingsData.sourceMap == null) {
            sMuteSettingsData.sourceMap = new HashMap<>();
        }
        if (sMuteSettingsData.userMap == null) {
            sMuteSettingsData.userMap = new HashMap<>();
        }
        if (sMuteSettingsData.words == null) {
            sMuteSettingsData.words = new ArrayList<>();
        }
    }

    public static void saveMuteSettings() {
        Gson gson = new Gson();
        String exportJson = gson.toJson(sMuteSettingsData);
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(PREF_KEY, exportJson);
        editor.apply();
    }

    public static boolean isMute(Row row) {
        if (row.isStatus()) {
            return isMute(row.getStatus());
        } else {
            return false;
        }
    }

    public static Boolean isMute(Status status) {
        if (sMuteSettingsData.userMap.get(status.getUser().getId()) != null) {
            return true;

        }
        UserMentionEntity[] mentions = status.getUserMentionEntities();
        for (UserMentionEntity mention : mentions) {
            if (sMuteSettingsData.userMap.get(mention.getId()) != null) {
                return true;

            }
        }
        Status retweetedStatus = status.getRetweetedStatus();
        if (retweetedStatus != null) {
            if (sMuteSettingsData.userMap.get(retweetedStatus.getUser().getId()) != null) {
                return true;
            }
        }
        Status source = retweetedStatus != null ? retweetedStatus : status;
        if (sMuteSettingsData.sourceMap.get(StatusUtil.getClientName(source.getSource())) != null) {
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

    public static void addSource(String source) {
        sMuteSettingsData.sourceMap.put(source, true);
    }

    public static void removeSource(String source) {
        sMuteSettingsData.sourceMap.remove(source);
    }

    public static void addUser(Long userId, String screenName) {
        sMuteSettingsData.userMap.put(userId, screenName);
    }

    public static void removeUser(Long userId) {
        sMuteSettingsData.userMap.remove(userId);
    }

    public static void addWord(String word) {
        for (String muteWord : sMuteSettingsData.words) {
            if (muteWord.equals(word)) {
                return;
            }
        }
        sMuteSettingsData.words.add(word);
    }

    public static void removeWord(String word) {
        sMuteSettingsData.words.remove(word);
    }

    public static ArrayList<String> getSources() {
        ArrayList<String> sources = new ArrayList<String>();
        for (String key : sMuteSettingsData.sourceMap.keySet()) {
            sources.add(key);
        }
        return sources;
    }

    public static HashMap<Long, String> getUserMap() {
        return sMuteSettingsData.userMap;
    }

    public static ArrayList<String> getWords() {
        return sMuteSettingsData.words;
    }

    public static class MuteSettingsData {
        HashMap<String, Boolean> sourceMap;
        HashMap<Long, String> userMap; /* LongSparseArrayはJSON化するのに都合が悪いのでHashMapを使う */
        ArrayList<String> words;
    }
}
