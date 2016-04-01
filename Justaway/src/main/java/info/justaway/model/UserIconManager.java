package info.justaway.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

import info.justaway.JustawayApplication;
import info.justaway.settings.BasicSettings;
import info.justaway.util.ImageUtil;
import twitter4j.ResponseList;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.auth.AccessToken;

public class UserIconManager {

    private static final String PREF_NAME_USER_ICON_MAP = "user_icon_map";
    private static final String PREF_KEY_USER_ICON_MAP = "data/v2";
    private static HashMap<String, String> sUserIconMap = new HashMap<String, String>();

    private static final String PREF_KEY_USER_NAME_MAP = "data/name";
    private static HashMap<String, String> sUserNameMap = new HashMap<String, String>();

    private static SharedPreferences getSharedPreferences() {
        return JustawayApplication.getApplication()
                .getSharedPreferences(PREF_NAME_USER_ICON_MAP, Context.MODE_PRIVATE);
    }

    public static void displayUserIcon(User user, final ImageView view) {
        String url;
        String size = BasicSettings.getUserIconSize();
        switch (size) {
            case "bigger":
                url = user.getBiggerProfileImageURL();
                break;
            case "normal":
                url = user.getProfileImageURL();
                break;
            case "mini":
                url = user.getMiniProfileImageURL();
                break;
            default:
                view.setVisibility(View.GONE);
                return;
        }
        if (BasicSettings.getUserIconRoundedOn()) {
            ImageUtil.displayRoundedImage(url, view);
        } else {
            ImageUtil.displayImage(url, view);
        }
    }

    public static String getName(final long userId) {
        String name = sUserNameMap.get(String.valueOf(userId));
        if (name != null) {
            return name;
        }
        return "";
    }

    /**
     * userIdからアイコンを取得する
     */
    public static void displayUserIcon(final long userId, final ImageView view) {
        String url = sUserIconMap.get(String.valueOf(userId));
        if (url != null) {
            ImageUtil.displayRoundedImage(url, view);
            return;
        }

        // すぐにURLが取れない時は一旦消す
        view.setImageDrawable(null);
    }

    @SuppressWarnings("unchecked")
    public static void addUserIconMap(User user) {
        final SharedPreferences preferences = getSharedPreferences();
        final Gson gson = new Gson();
        String iconJson = getSharedPreferences().getString(PREF_KEY_USER_ICON_MAP, null);
        if (iconJson != null) {
            sUserIconMap = gson.fromJson(iconJson, sUserIconMap.getClass());
        }
        String nameJson = getSharedPreferences().getString(PREF_KEY_USER_NAME_MAP, null);
        if (nameJson != null) {
            sUserNameMap = gson.fromJson(nameJson, sUserNameMap.getClass());
        }
        sUserIconMap.put(String.valueOf(user.getId()), user.getBiggerProfileImageURL());
        sUserNameMap.put(String.valueOf(user.getId()), user.getName());
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.putString(PREF_KEY_USER_ICON_MAP, gson.toJson(sUserIconMap));
        editor.putString(PREF_KEY_USER_NAME_MAP, gson.toJson(sUserNameMap));
        editor.apply();
    }

    @SuppressWarnings("unchecked")
    public static void warmUpUserIconMap() {
        ArrayList<AccessToken> accessTokens = AccessTokenManager.getAccessTokens();
        if (accessTokens == null || accessTokens.size() == 0) {
            return;
        }

        final Gson gson = new Gson();
        String iconJson = getSharedPreferences().getString(PREF_KEY_USER_ICON_MAP, null);
        if (iconJson != null) {
            sUserIconMap = gson.fromJson(iconJson, sUserIconMap.getClass());
        }
        String nameJson = getSharedPreferences().getString(PREF_KEY_USER_NAME_MAP, null);
        if (nameJson != null) {
            sUserNameMap = gson.fromJson(nameJson, sUserNameMap.getClass());
        }

        final long userIds[] = new long[accessTokens.size()];
        int i = 0;
        for (AccessToken accessToken : accessTokens) {
            userIds[i] = accessToken.getUserId();
            i++;
        }

        new AsyncTask<Void, Void, ResponseList<User>>() {

            @Override
            protected ResponseList<User> doInBackground(Void... voids) {
                try {
                    return TwitterManager.getTwitter().lookupUsers(userIds);
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(ResponseList<User> users) {
                if (users == null) {
                    return;
                }
                sUserIconMap.clear();
                sUserNameMap.clear();
                for (User user : users) {
                    sUserIconMap.put(String.valueOf(user.getId()), user.getBiggerProfileImageURL());
                    sUserNameMap.put(String.valueOf(user.getId()), user.getName());
                }
                SharedPreferences.Editor editor = getSharedPreferences().edit();
                editor.clear();
                editor.putString(PREF_KEY_USER_ICON_MAP, gson.toJson(sUserIconMap));
                editor.putString(PREF_KEY_USER_NAME_MAP, gson.toJson(sUserNameMap));
                editor.apply();
            }
        }.execute();
    }
}
