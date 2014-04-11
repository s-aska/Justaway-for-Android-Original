package info.justaway;

import android.app.Notification;
import android.app.NotificationManager;
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

        /*
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Justaway")
                .setContentText("通知チェッカー")
                .setSmallIcon(R.drawable.ic_notification_star)
                .build();

        startForeground(1, notification);
        */

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
        Log.i("Justaway", "[onEvent] NotificationEvent");

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
            url = row.getSource().getOriginalProfileImageURL();
            title = row.getSource().getScreenName();
            text = getString(R.string.notification_favorite) + status.getText();
            ticker = title + getString(R.string.notification_favorite_ticker) + status.getText();
            smallIcon = R.drawable.ic_notification_star;
        } else if (status.getInReplyToUserId() == userId) {
            url = status.getUser().getOriginalProfileImageURL();
            title = status.getUser().getScreenName();
            text = status.getText();
            ticker = text;
            smallIcon = R.drawable.ic_notification_at;
        } else if (retweet != null && retweet.getUser().getId() == userId) {
            url = status.getUser().getOriginalProfileImageURL();
            title = status.getUser().getScreenName();
            text = getString(R.string.notification_retweet) + status.getText();
            ticker = title + getString(R.string.notification_retweet_ticker) + status.getText();
            smallIcon = R.drawable.ic_notification_rt;
        } else {
            return;
        }

        Resources resources = application.getResources();
        int width = (int) resources.getDimension(android.R.dimen.notification_large_icon_width);
        int height = (int) resources.getDimension(android.R.dimen.notification_large_icon_height);

        Bitmap icon = ImageLoader.getInstance().loadImageSync(url);
        icon = Bitmap.createScaledBitmap(icon, width, height, false);

        Notification notification = new NotificationCompat.Builder(this)
                .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(smallIcon)
                .setLargeIcon(icon)
                .setTicker(ticker)
                .setWhen(System.currentTimeMillis())
                .build();

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(2, notification);
    }
}
