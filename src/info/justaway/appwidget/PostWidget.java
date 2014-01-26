package info.justaway.appwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import info.justaway.PostActivity;
import info.justaway.R;

/**
 *  ポストモードウィジェット
 */
public class PostWidget extends AppWidgetProvider{
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                      int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        // アクティビティの指定
        Intent intent = new Intent(context, PostActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        intent.putExtra("widget", true);
        // PendingIntentの取得
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_main);
        // インテントによるアクティビティ起動
        remoteViews.setOnClickPendingIntent(R.id.icon, pendingIntent);
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
        }
}
