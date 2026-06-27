package com.mohamed.expenseguard;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class DebtReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "debt_reminders";

    @Override public void onReceive(Context context, Intent intent) {
        String name = intent.getStringExtra("name");
        String amount = intent.getStringExtra("amount");
        String direction = intent.getStringExtra("direction");
        boolean owe = "OWE_TO_OTHERS".equals(direction);
        String title = owe ? "ميعاد سداد فلوس" : "ميعاد تحصيل فلوس";
        String body = owe ? "عليك تسدد " + amount + " لـ " + safe(name) : "ليك تستلم " + amount + " من " + safe(name);

        createChannel(context);
        Intent open = new Intent(context, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(context, 9001, open, PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0));

        NotificationCompat.Builder b = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pi);

        if (Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return;
        NotificationManagerCompat.from(context).notify((int)(System.currentTimeMillis() % 100000), b.build());
    }

    private static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT < 26) return;
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;
        NotificationChannel ch = new NotificationChannel(CHANNEL_ID, "تذكيرات الديون", NotificationManager.IMPORTANCE_HIGH);
        ch.setDescription("إشعارات مواعيد السداد والتحصيل");
        nm.createNotificationChannel(ch);
    }

    private static String safe(String s) { return s == null || s.trim().isEmpty() ? "شخص" : s; }
}
