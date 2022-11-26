package com.example.migratingandroid13

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

// Notification ID.
private const val NOTIFICATION_ID = 0

//Kolin Ext Calass For Send Notification
fun NotificationManager.sendNotification(
    titleBody: String,
    messageBody: String,
    applicationContext: Context
) {

    if (Build.VERSION.SDK_INT >= 33) {

        if (NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()) {

            Log.d("myNotification", "NotificationManager is  Allow  in Android 33 and Above")

            val contentIntent = Intent(applicationContext, MainActivity::class.java)

            val contentPendingIntent = PendingIntent.getActivity(
                applicationContext,
                NOTIFICATION_ID,
                contentIntent,
                getPendingInt
            )

            // Build the notification
            val builder = NotificationCompat.Builder(
                applicationContext, applicationContext.getString(R.string.notification_channel_id)
            ).setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(titleBody)
                .setContentText(messageBody)
                .setContentIntent(contentPendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)

            notify(NOTIFICATION_ID, builder.build())

        } else {
            Log.d("myNotification", "NotificationManager is  not Allow  in Android 33 and Above")
        }

    } else {

        Log.d("myNotification", "NotificationManager is Allow  in Android 12 and belove")

        val contentIntent = Intent(applicationContext, MainActivity::class.java)

        val contentPendingIntent = PendingIntent.getActivity(
            applicationContext,
            NOTIFICATION_ID,
            contentIntent,
            getPendingInt
        )

        // Build the notification
        val builder = NotificationCompat.Builder(
            applicationContext, applicationContext.getString(R.string.notification_channel_id)
        ).setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(titleBody)
            .setContentText(messageBody)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notify(NOTIFICATION_ID, builder.build())

    }

}

//This method return the PendingIntent Flag
val getPendingInt: Int
    get() {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
    }

//This method calling First to Send an other notification with Same Channel
fun NotificationManager.cancelNotifications() {
    cancelAll()
}

