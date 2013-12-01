package info.justaway;

import info.justaway.adapter.TwitterAdapter;
import info.justaway.model.Profile;
import info.justaway.model.Row;
import info.justaway.task.FollowTask;
import info.justaway.task.ShowUserLoader;
import info.justaway.task.UnfollowTask;
import twitter4j.Relationship;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.User;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ProfileActivity extends FragmentActivity implements
        LoaderManager.LoaderCallbacks<Profile> {

    private Context context;
    private Twitter twitter;
    private ImageView icon;
    private ImageView banner;
    private TextView urlIcon;
    private TextView locationIcon;
    private User user;
    private JustawayApplication application;
    private TwitterAdapter adapter;

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

        application = JustawayApplication.getApplication();

        twitter = application.getTwitter();
        icon = (ImageView) findViewById(R.id.icon);
        banner = (ImageView) findViewById(R.id.banner);
        banner.setImageResource(R.drawable.suzuri);
        urlIcon = (TextView) findViewById(R.id.url_icon);
        urlIcon.setTypeface(Typeface.createFromAsset(context.getAssets(), "fontello.ttf"));
        urlIcon.setText(R.string.fontello_sdd);
        locationIcon = (TextView) findViewById(R.id.location_icon);
        locationIcon.setTypeface(Typeface.createFromAsset(context.getAssets(), "fontello.ttf"));
        locationIcon.setText(R.string.fontello_location);
        TextView favouritesCount = (TextView) findViewById(R.id.favouritesCount_icon);
        favouritesCount.setTypeface(Typeface.createFromAsset(context.getAssets(), "fontello.ttf"));
        favouritesCount.setText(R.string.fontello_star);


        ListView listView = (ListView) findViewById(R.id.listView);

        // コンテキストメニューを使える様にする為の指定、但しデフォルトではロングタップで開く
        registerForContextMenu(listView);

        // Status(ツイート)をViewに描写するアダプター
        adapter = new TwitterAdapter(context, R.layout.row_tweet);
        listView.setAdapter(adapter);

        // シングルタップでコンテキストメニューを開くための指定
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.showContextMenu();
            }
        });

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
        new UserTimelineTask().execute(screenName);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        JustawayApplication application = JustawayApplication.getApplication();
        application.onCreateContextMenuForStatus(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        JustawayApplication application = JustawayApplication.getApplication();
        return application.onContextItemSelected(this, item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile, menu);
        return true;
    }

    @Override
    public Loader<Profile> onCreateLoader(int arg0, Bundle args) {
        String screenName = args.getString("screenName");
        return new ShowUserLoader(this, screenName);
    }

    @Override
    public void onLoadFinished(Loader<Profile> arg0, Profile profile) {
        user = profile.getUser();
        if (user != null) {
            ((TextView) findViewById(R.id.screenName)).setText("@" + user.getScreenName());
            ((TextView) findViewById(R.id.name)).setText(user.getName());
            if (user.getLocation() != null) {
                ((TextView) findViewById(R.id.location)).setText(user.getLocation());
            } else {
                ((TextView) findViewById(R.id.location)).setText("");
            }
            if (user.getURL() != null) {
                ((TextView) findViewById(R.id.url)).setText(user.getURLEntity().getExpandedURL());
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
            SimpleDateFormat date_format = new SimpleDateFormat("yyyy年MM月dd日",
                    Locale.ENGLISH);
            String createdAt = date_format.format(user.getCreatedAt()).toString();
            ((TextView) findViewById(R.id.createdAt)).setText(createdAt);
            final String iconUrl = user.getBiggerProfileImageURL();
            String bannerUrl = user.getProfileBannerMobileRetinaURL();
            JustawayApplication.getApplication().displayRoundedImage(iconUrl, icon);
            // 画像タップで拡大表示（ピンチイン・ピンチアウトいつかちゃんとやる）
            icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), ScaleImageActivity.class);
                    intent.putExtra("url", iconUrl);
                    startActivity(intent);
                }
            });

            if (bannerUrl != null) {
                JustawayApplication.getApplication().displayImage(bannerUrl, banner);
            }
            Relationship relationship = profile.getRelationship();
            if (relationship.isSourceFollowedByTarget()) {
                ((TextView) findViewById(R.id.followedBy)).setText("フォローされています");
            }
            if (user.getId() == application.getUser().getId()) {
                ((TextView) findViewById(R.id.follow)).setText("プロフィールを編集する");
                ((TextView) findViewById(R.id.follow)).setTextSize(13);
                findViewById(R.id.follow).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, EditProfileActivity.class);
                        startActivity(intent);
                    }
                });
            } else if (relationship.isSourceFollowingTarget()) {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = null;
        switch (item.getItemId()) {
            case R.id.open_twitter:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/"
                        + user.getScreenName()));
                startActivity(intent);
                break;
            case R.id.open_favstar:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://ja.favstar.fm/users/"
                        + user.getScreenName() + "/recent"));
                startActivity(intent);
                break;
            case R.id.open_twilog:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://twilog.org/"
                        + user.getScreenName()));
                startActivity(intent);
                break;
        }
        return true;
    }

    private class UserTimelineTask extends AsyncTask<String, Void, ResponseList<twitter4j.Status>> {
        @Override
        protected ResponseList<twitter4j.Status> doInBackground(String... params) {
            try {
                ResponseList<twitter4j.Status> statuses = JustawayApplication.getApplication().getTwitter().getUserTimeline(params[0]);
                return statuses;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ResponseList<twitter4j.Status> statuses) {
            adapter.clear();
            for (twitter4j.Status status : statuses) {
                adapter.add(Row.newStatus(status));
            }
        }
    }
}