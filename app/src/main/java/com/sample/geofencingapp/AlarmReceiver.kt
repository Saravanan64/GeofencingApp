package com.sample.geofencingapp

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.SystemClock
import java.text.SimpleDateFormat
import java.util.*


class AlarmReceiver : BroadcastReceiver() {

    var notificationHelper: NotificationHelper? = null
    var msgStr: String? = null;

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.

        notificationHelper =
            NotificationHelper(context);
//        println("---->isAppIsInBackground: "+isAppIsInBackground(context))

        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("ReminderApp", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val sharedNameValue = sharedPreferences.getString("name", "defaultname")

        val username = sharedNameValue
        val time = SystemClock.elapsedRealtime()
        val cal: Calendar = Calendar.getInstance()
        val tz: TimeZone = cal.getTimeZone()
        val dateInMillis = System.currentTimeMillis()
        val format = "hh:mm:ss aa"
        val sdf = SimpleDateFormat(format)
        val dateString: String = sdf.format(Date(dateInMillis))
        msgStr = "Hi $username, The time is $dateString"

        if (isAppIsInBackground(context)) {
            sendNotification(context)
        } else {
            val trIntent = Intent("android.intent.action.MAIN")
            trIntent.setClass(context, WelcomeActivity::class.java)
            trIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            trIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            trIntent.putExtra("msgStr", msgStr)
            trIntent.putExtra("type", "alert")
            trIntent.putExtra("timeStr", dateString)
            context.startActivity(trIntent)
        }

    }

    fun sendNotification(context: Context) {
        notificationHelper!!.sendHighPriorityNotification(
            "Time Reminder", msgStr,
            WelcomeActivity::class.java, "notification"
        )
    }

    @SuppressLint("NewApi")
    private fun isAppIsInBackground(context: Context): Boolean {
        var isInBackground = true
        val am =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            val runningProcesses =
                am.runningAppProcesses
            for (processInfo in runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (activeProcess in processInfo.pkgList) {
                        if (activeProcess == context.packageName) {
                            isInBackground = false
                        }
                    }
                }
            }
        } else {
            val taskInfo = am.getRunningTasks(1)
            val componentInfo = taskInfo[0].topActivity
            if (componentInfo!!.packageName == context.packageName) {
                isInBackground = false
            }
        }
        return isInBackground
    }
}
