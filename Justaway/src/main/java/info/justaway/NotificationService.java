package info.justaway;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

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
        Log.i("Justaway", "[onCreate]");
        super.onCreate();
        EventBus.getDefault().register(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("Justaway", "[onStartCommand]");

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i("Justaway", "[onBind]");
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i("Justaway", "[onUnbind]");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.i("Justaway", "[onRebind]");
        super.onRebind(intent);
    }

    @Override
    public void onTrimMemory(int level) {
        Log.i("Justaway", "[onTrimMemory]");
        super.onTrimMemory(level);
    }

    @Override
    public void onLowMemory() {
        Log.i("Justaway", "[onLowMemory]");
        super.onLowMemory();
    }

    @Override
    public void onDestroy() {
        Log.i("Justaway", "[onDestroy]");
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEvent(NotificationEvent event) {
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
            url = row.getSource().getBiggerProfileImageURL();
            title = row.getSource().getScreenName();
            text = getString(R.string.notification_favorite) + status.getText();
            ticker = title + getString(R.string.notification_favorite_ticker) + status.getText();
            smallIcon = R.drawable.ic_notification_star;
        } else if (status.getInReplyToUserId() == userId) {
            url = status.getUser().getBiggerProfileImageURL();
            title = status.getUser().getScreenName();
            text = status.getText();
            ticker = text;
            smallIcon = R.drawable.ic_notification_at;
        } else if (retweet != null && retweet.getUser().getId() == userId) {
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
        icon = Bitmap.createScaledBitmap(icon, width, height, false);

        Intent mainIntent = new Intent(this, MainActivity.class);
        PendingIntent mainPendingIntent = PendingIntent.getActivity(this, 0, mainIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(mainPendingIntent)
                .setSmallIcon(smallIcon)
                .setLargeIcon(icon)
                .setTicker(ticker)
                .setWhen(System.currentTimeMillis());

        if (status.getInReplyToUserId() == userId) {
            Intent statusIntent = new Intent(this, StatusActivity.class);
            statusIntent.putExtra("status", status);
            builder.addAction(R.drawable.ic_notification_twitter,
                    getString(R.string.menu_open),
                    PendingIntent.getActivity(this, 0, statusIntent, PendingIntent.FLAG_UPDATE_CURRENT));
            Intent replyIntent = new Intent(this, PostActivity.class);
            replyIntent.putExtra("inReplyToStatus", status);
            builder.addAction(R.drawable.ic_notification_at,
                    getString(R.string.context_menu_reply),
                    PendingIntent.getActivity(this, 0, replyIntent, PendingIntent.FLAG_UPDATE_CURRENT));
            Intent favoriteIntent = new Intent(this, FavoriteActivity.class);
            favoriteIntent.putExtra("statusId", status.getId());
            builder.addAction(R.drawable.ic_notification_star,
                    getString(R.string.context_menu_create_favorite),
                    PendingIntent.getActivity(this, 0, favoriteIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        }

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1, builder.build());
    }
}
