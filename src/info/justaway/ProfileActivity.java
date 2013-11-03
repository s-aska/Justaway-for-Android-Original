package info.justaway;

import info.justaway.model.Profile;
import info.justaway.task.FollowTask;
import info.justaway.task.ShowUserLoader;
import info.justaway.task.UnfollowTask;
import twitter4j.Relationship;
import twitter4j.Twitter;
import twitter4j.User;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class ProfileActivity extends FragmentActivity implements
        LoaderManager.LoaderCallbacks<Profile> {

    private Context context;
    private Twitter twitter;
    private ImageView icon;
    private ImageView banner;
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
        icon = (ImageView) findViewById(R.id.icon);
        banner = (ImageView) findViewById(R.id.banner);
        urlIcon = (TextView) findViewById(R.id.url_icon);
        urlIcon.setTypeface(Typeface.createFromAsset(context.getAssets(), "fontello.ttf"));
        urlIcon.setText(R.string.fontello_sdd);
        locationIcon = (TextView) findViewById(R.id.location_icon);
        locationIcon.setTypeface(Typeface.createFromAsset(context.getAssets(), "fontello.ttf"));
        locationIcon.setText(R.string.fontello_location);

        Intent intent = getIntent();
        Bundle args = new Bundle(1);
        String screenName = null;
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            screenName = intent.getData().getLastPathSegment();
        } else {
            screenName = intent.getStringExtra("screenName");
        }
        args.putString("screenName", screenName);
        getSupportLoaderManager().initLoader(0, args, this);
    }

    @Override
    public Loader<Profile> onCreateLoader(int arg0, Bundle args) {
        String screenName = args.getString("screenName");
        return new ShowUserLoader(this, screenName);
    }

    @Override
    public void onLoadFinished(Loader<Profile> arg0, Profile profile) {
        final User user = profile.getUser();
        if (user != null) {
            ((TextView) findViewById(R.id.screenName)).setText("@" + user.getScreenName());
            ((TextView) findViewById(R.id.name)).setText(user.getName());
            if (user.getLocation() != null) {
                ((TextView) findViewById(R.id.location)).setText(user.getLocation());
            } else {
                ((TextView) findViewById(R.id.location)).setText("");
            }
            if (user.getURL() != null) {
                ((TextView) findViewById(R.id.url)).setText(String.valueOf(user.getURL()));
            } else {
                ((TextView) findViewById(R.id.url)).setText("");
            }
            if (user.getDescription() != null) {
                ((TextView) findViewById(R.id.description)).setText(user.getDescription());
            } else {
                ((TextView) findViewById(R.id.description)).setText("");
            }
            ((TextView) findViewById(R.id.favouritesCount)).setText(String.valueOf(user
                    .getFavouritesCount()));
            ((TextView) findViewById(R.id.statusesCount)).setText(String.valueOf(user
                    .getStatusesCount()));
            ((TextView) findViewById(R.id.friendsCount)).setText(String.valueOf(user
                    .getFriendsCount()));
            ((TextView) findViewById(R.id.followersCount)).setText(String.valueOf(user
                    .getFollowersCount()));
            ((TextView) findViewById(R.id.listedCount)).setText(String.valueOf(user
                    .getListedCount()));
            ((TextView) findViewById(R.id.createdAt)).setText(user.getCreatedAt().toString());
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
            Relationship relationship = profile.getRelationship();
            if (relationship.isSourceFollowedByTarget()) {
                ((TextView) findViewById(R.id.followedBy)).setText("フォローされています");
            }
            if (relationship.isSourceFollowingTarget()) {
                ((TextView) findViewById(R.id.follow)).setText("フォローを解除");
                findViewById(R.id.follow).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new UnfollowTask().execute(user.getId());
                    }
                });
            } else {
                ((TextView) findViewById(R.id.follow)).setText("フォローする");
                findViewById(R.id.follow).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new FollowTask().execute(user.getId());
                    }
                });
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Profile> arg0) {
    }
}