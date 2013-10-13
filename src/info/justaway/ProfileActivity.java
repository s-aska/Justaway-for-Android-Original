package info.justaway;


import twitter4j.Twitter;
import twitter4j.User;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class ProfileActivity extends Activity {

    private Context context;
    private Twitter twitter;
    private TextView screenName;
    private ImageView icon;
    private ImageView banner;
    private TextView name;
    private TextView location;
    private TextView url;
    private TextView description;
    private TextView statusesCount;
    private TextView favouritesCount;
    private TextView friendsCount;
    private TextView followersCount;
    private TextView listedCount;
    private TextView createdAt;
    private TextView urlIcon;
    private TextView locationIcon;

    // private TextView addedToTwitter;

    /**
     * Twitter REST API用のインスタンス
     */
    public Twitter getTwitter() {
        return twitter;
    }

    public void setTwitter(Twitter twitter) {
        this.twitter = twitter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        context = this;

        JustawayApplication application = JustawayApplication.getApplication();

        twitter = application.getTwitter();
        screenName = (TextView) findViewById(R.id.screenName);
        name = (TextView) findViewById(R.id.name);
        location = (TextView) findViewById(R.id.location);
        url = (TextView) findViewById(R.id.url);
        description = (TextView) findViewById(R.id.description);
        statusesCount = (TextView) findViewById(R.id.statusesCount);
        favouritesCount = (TextView) findViewById(R.id.favouritesCount);
        friendsCount = (TextView) findViewById(R.id.friendsCount);
        followersCount = (TextView) findViewById(R.id.followersCount);
        listedCount = (TextView) findViewById(R.id.listedCount);
        createdAt = (TextView) findViewById(R.id.createdAt);
        icon = (ImageView) findViewById(R.id.icon);
        banner = (ImageView) findViewById(R.id.banner);

        urlIcon = (TextView) findViewById(R.id.url_icon);
        urlIcon.setTypeface(Typeface.createFromAsset(context.getAssets(), "fontello.ttf"));
        urlIcon.setText(R.string.fontello_sdd);
        locationIcon = (TextView) findViewById(R.id.location_icon);
        locationIcon.setTypeface(Typeface.createFromAsset(context.getAssets(), "fontello.ttf"));
        locationIcon.setText(R.string.fontello_location);

        Intent intent = getIntent();
        Long userId = intent.getLongExtra("userId", 0);
        new ProfileTask().execute(userId);

    }

    private class ProfileTask extends AsyncTask<Long, Void, User> {

        @Override
        protected User doInBackground(Long... params) {
            try {
                User user = getTwitter().showUser(params[0]);
                return user;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(User user) {
            if (user != null) {
                screenName.setText("@" + user.getScreenName());
                name.setText(user.getName());
                if (user.getLocation() != null) {
                    location.setText(user.getLocation());
                } else {
                    location.setText("");
                }
                if (user.getURL() != null) {
                    url.setText(String.valueOf(user.getURL()));
                } else {
                    url.setText("");
                }
                if (user.getDescription() != null) {
                    description.setText(user.getDescription());
                } else {
                    description.setText("");
                }
                favouritesCount.setText(String.valueOf(user.getFavouritesCount()));
                statusesCount.setText(String.valueOf(user.getStatusesCount()));
                friendsCount.setText(String.valueOf(user.getFriendsCount()));
                followersCount.setText(String.valueOf(user.getFollowersCount()));
                listedCount.setText(String.valueOf(user.getListedCount()));
                createdAt.setText(user.getCreatedAt().toString());
                String iconUrl = user.getBiggerProfileImageURL();
                String bannerUrl = user.getProfileBannerMobileRetinaURL();
                icon.setTag(iconUrl);
                banner.setTag(bannerUrl);
                Picasso.with(context).load(iconUrl).into(icon);
                if (bannerUrl != null) {
                    Picasso.with(context).load(bannerUrl).placeholder(R.drawable.suzuri).into(banner);
                } else {
                    banner.setImageResource(R.drawable.suzuri);
                }
            }
        }
    }
}