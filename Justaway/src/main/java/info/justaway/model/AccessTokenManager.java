package info.justaway.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;

import info.justaway.JustawayApplication;
import twitter4j.auth.AccessToken;

public class AccessTokenManager {

    private static final String TOKENS = "tokens";
    private static final String PREF_NAME = "twitter_access_token";
    private AccessToken mAccessToken;

    private SharedPreferences getSharedPreferences() {
        return JustawayApplication.getApplication()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public Boolean hasAccessToken() {
        return getAccessToken() != null;
    }

    public ArrayList<AccessToken> getAccessTokens() {
        String json = getSharedPreferences().getString(TOKENS, null);
        if (json == null) {
            return null;
        }

        Gson gson = new Gson();
        AccountSettings accountSettings = gson.fromJson(json, AccountSettings.class);
        return accountSettings.accessTokens;
    }

    public AccessToken getAccessToken() {
        if (mAccessToken != null) {
            return mAccessToken;
        }

        String json = getSharedPreferences().getString(TOKENS, null);
        if (json == null) {
            return null;
        }

        Gson gson = new Gson();
        AccountSettings accountSettings = gson.fromJson(json, AccountSettings.class);
        mAccessToken = accountSettings.accessTokens.get(accountSettings.index);
        return mAccessToken;
    }

    public void setAccessToken(AccessToken accessToken) {

        mAccessToken = accessToken;

        JustawayApplication.getApplication().getTwitter().setOAuthAccessToken(mAccessToken);

        SharedPreferences preferences = getSharedPreferences();
        String json = preferences.getString(TOKENS, null);
        Gson gson = new Gson();

        AccountSettings accountSettings;
        if (json != null) {
            accountSettings = gson.fromJson(json, AccountSettings.class);

            boolean existUser = false;
            int i = 0;
            for (AccessToken sharedAccessToken : accountSettings.accessTokens) {
                if (accessToken.getUserId() == sharedAccessToken.getUserId()) {
                    accountSettings.accessTokens.set(i, accessToken);
                    accountSettings.index = i;
                    existUser = true;
                }
                i++;
            }

            if (!existUser) {
                accountSettings.index = accountSettings.accessTokens.size();
                accountSettings.accessTokens.add(mAccessToken);
            }
        } else {
            accountSettings = new AccountSettings();
            accountSettings.accessTokens = new ArrayList<AccessToken>();
            accountSettings.accessTokens.add(mAccessToken);
        }

        String exportJson = gson.toJson(accountSettings);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(TOKENS, exportJson);
        editor.commit();
    }

    public void removeAccessToken(AccessToken removeAccessToken) {
        SharedPreferences preferences = getSharedPreferences();
        String json = preferences.getString(TOKENS, null);
        Gson gson = new Gson();

        AccountSettings accountSettings = gson.fromJson(json, AccountSettings.class);
        AccessToken currentAccessToken = accountSettings.accessTokens.get(accountSettings.index);

        /**
         * 現在設定されているAccessTokenより先に削除すべきAccessTokenがある場合indexをデクリメントする
         * これをしないと位置がずれる
         */
        for (AccessToken accessToken : accountSettings.accessTokens) {
            if (accessToken.getUserId() == removeAccessToken.getUserId()) {
                accountSettings.index--;
                break;
            }
            if (accessToken.getUserId() == currentAccessToken.getUserId()) {
                break;
            }
        }
        accountSettings.accessTokens.remove(removeAccessToken);

        String exportJson = gson.toJson(accountSettings);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(TOKENS, exportJson);
        editor.commit();
    }

    public long getUserId() {
        if (mAccessToken == null) {
            return -1L;
        }
        return mAccessToken.getUserId();
    }

    public String getScreenName() {
        if (mAccessToken == null) {
            return "";
        }
        return mAccessToken.getScreenName();
    }

    public static class AccountSettings {
        int index;
        ArrayList<AccessToken> accessTokens;
    }
}
