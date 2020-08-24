package com.sample.geofencingapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.sample.timereminderapp.R

class RegisterActivity : AppCompatActivity() {
    var notificationHelper: NotificationHelper? = null
    val NOTIFICATION_ID:Int = 0
    val PRIMARY_CHANNEL_ID:String = "primary_notification_channel"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val txt_name = findViewById<EditText>(R.id.fullName)
        val txt_email = findViewById<EditText>(R.id.userEmailId)
        val txt_phone = findViewById<EditText>(R.id.mobileNumber)
        val txt_pw = findViewById<EditText>(R.id.password)
        val txt_cpw = findViewById<EditText>(R.id.confirmPassword)
        val button = findViewById<Button>(R.id.signUpBtn)
        val sharedPreferences: SharedPreferences = this.getSharedPreferences("ReminderApp",Context.MODE_PRIVATE)

//        println("---->user: "+sharedPreferences.getString("name","").isNullOrEmpty())
        if(!sharedPreferences.getString("name","").isNullOrEmpty())
        {
            val sharedNameValue = sharedPreferences.getString("name","defaultname")
            intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }
        button.setOnClickListener(View.OnClickListener {
            val name:String = txt_name.text.toString()
            val email:String = txt_email.text.toString()
            val phone:String = txt_phone.text.toString()
            val editor:SharedPreferences.Editor =  sharedPreferences.edit()
            editor.putString("name",name)
            editor.putString("email",email)
            editor.putString("phone",phone)
            editor.apply()
            editor.commit()
            Toast.makeText(this,"Registered Successfully",Toast.LENGTH_LONG).show()
            scheduleReminder()
            intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        })
    }

    fun scheduleReminder()
    {
        val intent = Intent(this,  AlarmReceiver::class.java)
        val notifyPendingIntent = PendingIntent.getBroadcast(
            this,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val repeatInterval = AlarmManager.INTERVAL_FIFTEEN_MINUTES
        val triggerTime = (SystemClock.elapsedRealtime())
        alarmManager.setInexactRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            triggerTime, repeatInterval, notifyPendingIntent
        )

//        alarmManager.setRepeating(
//            AlarmManager.ELAPSED_REALTIME_WAKEUP,
//            SystemClock.elapsedRealtime(),
//            2*60*1000,
//            notifyPendingIntent);
    }

}
