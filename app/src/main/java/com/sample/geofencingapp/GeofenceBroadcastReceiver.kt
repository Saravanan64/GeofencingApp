package com.sample.geofencingapp

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.*
import android.os.Build
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import java.text.SimpleDateFormat
import java.util.*


class GeofenceBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "GeofenceBroadcastReceiv"
    }

    var msgStr: String? = null;

    override fun onReceive(context: Context, intent: Intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.

        Toast.makeText(context, "Geofence triggered...", Toast.LENGTH_SHORT).show();

        val notificationHelper =
            NotificationHelper(context)
        val geofencingEvent: GeofencingEvent = GeofencingEvent.fromIntent(intent)
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences("ReminderApp", Context.MODE_PRIVATE)
        val sharedNameValue = sharedPreferences.getString("name", "defaultname")
        val username = sharedNameValue
        val temp = getSystemtime()
        msgStr = "Hi $username, The time is $temp"

        if (geofencingEvent.hasError()) {
            Log.d(TAG, "onReceive: Error receiving geofence event...")
            return
        }
        val geofenceList: List<Geofence> = geofencingEvent.getTriggeringGeofences()
        for (geofence in geofenceList) {
            Log.d(TAG, "onReceive: " + geofence.getRequestId())
        }
        //        Location location = geofencingEvent.getTriggeringLocation();
        val transitionType: Int = geofencingEvent.getGeofenceTransition()
        when (transitionType) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                Toast.makeText(context, "GEOFENCE_TRANSITION_ENTER", Toast.LENGTH_SHORT).show()
                println("---->" + "GEOFENCE_TRANSITION_ENTER")
                if (isAppIsInBackground(context)) {
                    notificationHelper.sendHighPriorityNotification(
                        "QWQER",
                        "Hi $username, Welcome to QWQER",
                        WelcomeActivity::class.java,
                        "notification"
                    )
                } else {
                    val trIntent = Intent("android.intent.action.MAIN")
                    trIntent.setClass(context, WelcomeActivity::class.java)
                    trIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    trIntent.putExtra("msgStr", "Hi $username, Welcome to QWQER")
                    trIntent.putExtra("type", "alert")
                    context.startActivity(trIntent)
                }

            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                Toast.makeText(context, "GEOFENCE_TRANSITION_EXIT", Toast.LENGTH_SHORT).show()
                if (isAppIsInBackground(context)) {
                    notificationHelper.sendHighPriorityNotification(
                        "QWQER", "Hi $username, Thank you for visiting QWQER",
                        WelcomeActivity::class.java, "notification"
                    )

                } else {
                    val trIntent = Intent("android.intent.action.MAIN")
                    trIntent.setClass(context, WelcomeActivity::class.java)
                    trIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    trIntent.putExtra("msgStr", "Hi $username, Thank you for visiting QWQER")
                    trIntent.putExtra("type", "alert")
                    context.startActivity(trIntent)
                }
            }
//            Geofence.GEOFENCE_TRANSITION_DWELL -> {
//                Toast.makeText(context, "GEOFENCE_TRANSITION_DWELL", Toast.LENGTH_SHORT).show()
//                if(isAppIsInBackground(context))
//                {
//                    notificationHelper.sendHighPriorityNotification("GEOFENCE_TRANSITION_DWELL","",
//                        WelcomeActivity::class.java)
//                }
//                else
//                {
//                    val trIntent = Intent("android.intent.action.MAIN")
//                    trIntent.setClass(context,WelcomeActivity::class.java)
//                    trIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                    trIntent.putExtra("type","dwell")
//                    context.startActivity(trIntent)
//                }
//            }
        }
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

    private fun getSystemtime(): String {
        val time = SystemClock.elapsedRealtime()
        val cal: Calendar = Calendar.getInstance()
        val tz: TimeZone = cal.getTimeZone()
        val dateInMillis = System.currentTimeMillis()
        val format = "HH:mm:ss"
        val sdf = SimpleDateFormat(format)
        val dateString: String = sdf.format(Date(dateInMillis))
        return dateString
    }
}