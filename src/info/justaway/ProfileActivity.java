package info.justaway;

import info.justaway.util.TwitterUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import twitter4j.Twitter;
import twitter4j.User;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

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

        twitter = TwitterUtils.getTwitterInstance(this);
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
                ProgressBar wait = (ProgressBar) findViewById(R.id.WaitBar);
                String iconUrl = user.getBiggerProfileImageURL();
                String bannerUrl = user.getProfileBannerMobileRetinaURL();
                icon.setTag(iconUrl);
                banner.setTag(bannerUrl);
                new ImageGetTask(icon, wait).execute(iconUrl);
                if (bannerUrl != null) {
                    new ImageGetTask(banner, wait).execute(bannerUrl);
                }
            }
        }
    }

    class ImageGetTask extends AsyncTask<String, Void, Bitmap> {
        private ImageView image;
        private String tag;
        private ProgressBar bar;

        public ImageGetTask(ImageView image, ProgressBar bar) {
            // 対象の項目を保持しておく
            this.image = image;
            this.bar = bar;
            this.tag = image.getTag().toString();
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            // ここでHttp経由で画像を取得します。取得後Bitmapで返します。
            synchronized (context) {
                try {
                    URL imageUrl = new URL(params[0]);
                    InputStream imageIs;
                    imageIs = imageUrl.openStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(imageIs);
                    return bitmap;
                } catch (MalformedURLException e) {
                    return null;
                } catch (IOException e) {
                    return null;
                }
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            // Tagが同じものか確認して、同じであれば画像を設定する
            // （Tagの設定をしないと別の行に画像が表示されてしまう）
            if (tag.equals(image.getTag())) {
                if (bitmap != null) {
                    // 画像の設定
                    image.setImageBitmap(bitmap);
                } else {
                    // エラーの場合は×印を表示
                    // image.setImageDrawable(context.getResources().getDrawable(
                    // R.drawable.x));
                }
                // プログレスバーを隠し、取得した画像を表示
                image.setVisibility(View.VISIBLE);
                if (bar != null) {
                    bar.setVisibility(View.GONE);
                }
            }
        }
    }

}