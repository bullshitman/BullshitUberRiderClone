package com.bullshitman.bullshituberriderclone.common

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.bullshitman.bullshituberriderclone.R
import com.bullshitman.bullshituberriderclone.model.RiderModel
import java.lang.StringBuilder

const val NOTIFICATION_CHANNEL_ID = "bullshitUberClone"

object Common {
    var currentRider: RiderModel? = null
    val TOKEN_REFERENCE = "Token"
    val RIDER_INFO_REFERENCE = "Rider"
    val NOTIF_BODY = "body"
    val NOTIF_TITLE = "title"

    fun showNotification(context: Context, id: Int, title: String?, body: String?, intent: Intent?) {
        var pendingIntent: PendingIntent? = null
        if (intent != null) {
            pendingIntent = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "BullShit Uber", NotificationManager.IMPORTANCE_HIGH)
            with(notificationChannel) {
                description = "BullShit Uber"
                enableLights(true)
                lightColor = Color.RED
                vibrationPattern = longArrayOf(0, 1000, 500, 1000)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        builder.setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(Notification.DEFAULT_VIBRATE)
            .setSmallIcon(R.drawable.ic_baseline_directions_car_24)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_baseline_directions_car_24))

        if (pendingIntent != null) {
            builder.setContentIntent(pendingIntent)
        }
        val notification = builder.build()
        notificationManager.notify(id, notification)
    }

    fun buildWelcomeMessage() = StringBuilder("Welcome, ${currentRider?.firstName} ${currentRider?.lastName}")
}