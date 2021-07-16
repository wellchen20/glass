package com.xylink.sdk.sample;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import java.util.UUID;

public class BackgroundCallService extends Service {
    public static final int NOTIFICATION_ONGOING_ID = 20;
    private static final String CHANNEL_ID_IN_CALL = "XYSDK_IN_CALL";
    private NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        showInCallNotification();
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hideInCallNotification();
    }

    private void showInCallNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(NOTIFICATION_ONGOING_ID, getInCallNotification());
        } else {
            notificationManager.notify(NOTIFICATION_ONGOING_ID, getInCallNotification());
        }
    }

    private void hideInCallNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true);
        } else {
            notificationManager.cancel(NOTIFICATION_ONGOING_ID);
        }
    }

    public Notification getInCallNotification() {
        Intent intent = new Intent(MyApplication.getContext(), XyCallActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(MyApplication.getContext(),
                UUID.randomUUID().hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(MyApplication.getContext(), getInCallChannelId())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setDefaults(NotificationCompat.FLAG_ONGOING_EVENT)
                .setSound(null)
                .setVibrate(new long[]{0})
                .setContentTitle(MyApplication.getContext().getString(R.string.app_name))
                .setContentText("XYSDK正在运行")
                .setContentIntent(pendingIntent);
        return builder.build();
    }

    public String getInCallChannelId() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_IN_CALL, "通话中",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("XYSDK正在通话中");
            channel.enableLights(false);
            channel.enableVibration(false);
            channel.setVibrationPattern(new long[]{0});
            channel.setSound(null, null);
            notificationManager.createNotificationChannel(channel);
        }
        return CHANNEL_ID_IN_CALL;
    }
}
