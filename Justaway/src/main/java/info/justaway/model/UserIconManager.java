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
import twitter4j.ResponseList;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.auth.AccessToken;

public class UserIconManager {

    private static final String PREF_NAME_USER_ICON_MAP = "user_icon_map";
    private static final String PREF_KEY_USER_ICON_MAP = "data/v2";
    private JustawayApplication mApplication;
    private HashMap<String, String> mUserIconMap = new HashMap<String, String>();

    public UserIconManager() {
        mApplication = JustawayApplication.getApplication();
    }

    public void displayUserIcon(User user, final ImageView view) {
        String url;
        String size = mApplication.getBasicSettings().getUserIconSize();
        if (size.equals("bigger")) {
            url = user.getBiggerProfileImageURL();
        } else if (size.equals("normal")) {
            url = user.getProfileImageURL();
        } else if (size.equals("mini")) {
            url = user.getMiniProfileImageURL();
        } else {
            view.setVisibility(View.GONE);
            return;
        }
        if (mApplication.getBasicSettings().getUserIconRoundedOn()) {
            mApplication.displayRoundedImage(url, view);
        } else {
            mApplication.displayImage(url, view);
        }
    }

    /**
     * userIdからアイコンを取得する
     */
    public void displayUserIcon(final long userId, final ImageView view) {
        String url = mUserIconMap.get(String.valueOf(userId));
        if (url != null) {
            mApplication.displayRoundedImage(url, view);
            return;
        }

        // すぐにURLが取れない時は一旦消す
        view.setImageDrawable(null);
    }

    @SuppressWarnings("unchecked")
    public void addUserIconMap(User user) {
        final SharedPreferences preferences = mApplication.getSharedPreferences(PREF_NAME_USER_ICON_MAP, Context.MODE_PRIVATE);
        final Gson gson = new Gson();
        String json = preferences.getString(PREF_KEY_USER_ICON_MAP, null);
        if (json != null) {
            mUserIconMap = gson.fromJson(json, mUserIconMap.getClass());
        }
        mUserIconMap.put(String.valueOf(user.getId()), user.getBiggerProfileImageURL());
        String exportJson = gson.toJson(mUserIconMap);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.putString(PREF_KEY_USER_ICON_MAP, exportJson);
        editor.commit();
    }

    @SuppressWarnings("unchecked")
    public void warmUpUserIconMap() {
        ArrayList<AccessToken> accessTokens = mApplication.getAccessTokenManager().getAccessTokens();
        if (accessTokens == null || accessTokens.size() == 0) {
            return;
        }

        final SharedPreferences preferences = mApplication.getSharedPreferences(PREF_NAME_USER_ICON_MAP, Context.MODE_PRIVATE);
        final Gson gson = new Gson();
        String json = preferences.getString(PREF_KEY_USER_ICON_MAP, null);
        if (json != null) {
            mUserIconMap = gson.fromJson(json, mUserIconMap.getClass());
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
                    return mApplication.getTwitterManager().getTwitter().lookupUsers(userIds);
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
                mUserIconMap.clear();
                for (User user : users) {
                    mUserIconMap.put(String.valueOf(user.getId()), user.getBiggerProfileImageURL());
                }
                String exportJson = gson.toJson(mUserIconMap);
                SharedPreferences.Editor editor = preferences.edit();
                editor.clear();
                editor.putString(PREF_KEY_USER_ICON_MAP, exportJson);
                editor.commit();
            }
        }.execute();
    }
}
