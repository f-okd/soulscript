package com.example.soulscript.backend;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.soulscript.R;

public class DailyVerseService extends Service {
    private static final String CHANNEL_ID = "daily_verse_channel";
    private static final int NOTIFICATION_ID = 1;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // The onStartCommand() method is called when the service is started.
	// This method creates a notification channel and builds a notification.
	// The service then runs in the foreground by calling startForeground() with the notification ID.
	// The service then calls sendDailyVerseNotification() to send a notification to the user.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        Notification notification = buildDailyVerseNotification();
        startForeground(NOTIFICATION_ID, notification);

        createNotificationChannel();
        sendDailyVerseNotification();
        return START_NOT_STICKY;
    }

    // The createNotificationChannel() method creates a notification channel for the app.
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Daily Verse Channel";
            String description = "Channel for daily verse notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // The sendDailyVerseNotification() method creates an intent that opens the daily verse website.
    private void sendDailyVerseNotification() {
        Intent notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.verseoftheday.com/"));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Daily Verse")
                .setContentText("Checkout today's daily verse!")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    // The buildDailyVerseNotification() method builds the notification thats used in onStart */
    private Notification buildDailyVerseNotification() {
        Intent notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.verseoftheday.com/"));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Daily Verse")
                .setContentText("Checkout today's daily verse!")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        return builder.build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
