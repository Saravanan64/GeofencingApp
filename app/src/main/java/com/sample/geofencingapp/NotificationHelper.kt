package com.sample.geofencingapp

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.sample.timereminderapp.R
import java.util.*


class NotificationHelper(base: Context?) : ContextWrapper(base)
{
    private val CHANNEL_NAME = "High priority channel"
    private val CHANNEL_ID = "com.sample.timereminderapp$CHANNEL_NAME"

    companion object {
        private const val TAG = "NotificationHelper"
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannels()
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun createChannels() {
        val notificationChannel =NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
        notificationChannel.enableLights(true)
        notificationChannel.enableVibration(true)
        notificationChannel.description = "This is the channel for geofence"
        notificationChannel.lightColor = Color.RED
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(notificationChannel)
    }

    fun sendHighPriorityNotification(title: String?, body: String?, activityName: Class<*>? ,type: String?) {
        val intent = Intent(applicationContext, activityName)
        intent.putExtra("type",type)
        intent.putExtra("msgStr",body)
        val uniqueInt = (System.currentTimeMillis() and 0xfffffff).toInt()
        val pendingIntent = PendingIntent.getActivity(applicationContext, uniqueInt, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.ic_icon_transparent1)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),
                    R.drawable.icon
                ))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColor(Color.RED)
                    .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()
        NotificationManagerCompat.from(this).notify(Random().nextInt(), notification)
    }
}