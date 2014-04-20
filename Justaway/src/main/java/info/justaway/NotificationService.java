package info.justaway;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.nostra13.universalimageloader.core.ImageLoader;

import de.greenrobot.event.EventBus;
import info.justaway.event.model.NotificationEvent;
import info.justaway.model.Row;
import twitter4j.Status;

public class NotificationService extends Service {
    public NotificationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEvent(NotificationEvent event) {
        SharedPreferences preferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
        JustawayApplication application = JustawayApplication.getApplication();

        long userId = application.getUserId();

        Row row = event.getRow();
        Status status = row.getStatus();
        Status retweet = status.getRetweetedStatus();

        String url;
        String title;
        String text;
        String ticker;
        int smallIcon;
        if (row.isFavorite()) {
            if (!preferences.getBoolean("notification_favorite_on", true)) {
                return;
            }
            url = row.getSource().getBiggerProfileImageURL();
            title = row.getSource().getScreenName();
            text = getString(R.string.notification_favorite) + status.getText();
            ticker = title + getString(R.string.notification_favorite_ticker) + status.getText();
            smallIcon = R.drawable.ic_notification_star;
        } else if (status.getInReplyToUserId() == userId) {
            if (!preferences.getBoolean("notification_reply_on", true)) {
                return;
            }
            url = status.getUser().getBiggerProfileImageURL();
            title = status.getUser().getScreenName();
            text = status.getText();
            ticker = text;
            smallIcon = R.drawable.ic_notification_at;
        } else if (retweet != null && retweet.getUser().getId() == userId) {
            if (!preferences.getBoolean("notification_retweet_on", true)) {
                return;
            }
            url = status.getUser().getBiggerProfileImageURL();
            title = status.getUser().getScreenName();
            text = getString(R.string.notification_retweet) + status.getText();
            ticker = title + getString(R.string.notification_retweet_ticker) + status.getText();
            smallIcon = R.drawable.ic_notification_rt;
        } else {
            return;
        }

        Resources resources = application.getResources();
        int width = (int) resources.getDimension(android.R.dimen.notification_large_icon_width) / 3 * 2;
        int height = (int) resources.getDimension(android.R.dimen.notification_large_icon_height) / 3 * 2;

        Bitmap icon = ImageLoader.getInstance().loadImageSync(url);
        icon = Bitmap.createScaledBitmap(icon, width, height, true);

        Intent mainIntent = new Intent(this, MainActivity.class);
        PendingIntent mainPendingIntent = PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(mainPendingIntent)
                .setSmallIcon(smallIcon)
                .setLargeIcon(icon)
                .setTicker(ticker)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis());

        boolean vibrate = preferences.getBoolean("notification_vibrate_on", true);
        boolean sound = preferences.getBoolean("notification_sound_on", true);
        if (vibrate && sound) {
            builder.setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND);
        } else if (vibrate) {
            builder.setDefaults(Notification.DEFAULT_VIBRATE);
        } else if (sound) {
            builder.setDefaults(Notification.DEFAULT_SOUND);
        }

        if (status.getInReplyToUserId() == userId) {
            Intent statusIntent = new Intent(this, StatusActivity.class);
            statusIntent.putExtra("status", status);
            statusIntent.putExtra("notification", true);
            builder.addAction(R.drawable.ic_notification_twitter,
                    getString(R.string.menu_open),
                    PendingIntent.getActivity(this, 1, statusIntent, PendingIntent.FLAG_UPDATE_CURRENT));
            Intent replyIntent = new Intent(this, PostActivity.class);
            replyIntent.putExtra("inReplyToStatus", status);
            replyIntent.putExtra("notification", true);
            replyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            builder.addAction(R.drawable.ic_notification_at,
                    getString(R.string.context_menu_reply),
                    PendingIntent.getActivity(this, 1, replyIntent, PendingIntent.FLAG_CANCEL_CURRENT));
            Intent favoriteIntent = new Intent(this, FavoriteActivity.class);
            favoriteIntent.putExtra("statusId", status.getId());
            favoriteIntent.putExtra("notification", true);
            builder.addAction(R.drawable.ic_notification_star,
                    getString(R.string.context_menu_create_favorite),
                    PendingIntent.getActivity(this, 1, favoriteIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        }

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1, builder.build());
    }
}
