package com.sample.geofencingapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.TextView
import com.sample.timereminderapp.R

class WelcomeActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_welcome)
//        val textmsg = findViewById<TextView>(R.id.textViewWelcome)
        onNewIntent(getIntent())
    }

    override fun onNewIntent(intent: Intent?) {

        println("---->onNewIntent")
        var typeStr: String = ""
        var msgStr: String = ""
        var textmsg:TextView? = null

        val sharedPreferences: SharedPreferences = this.getSharedPreferences("ReminderApp",Context.MODE_PRIVATE)
        val sharedNameValue = sharedPreferences.getString("name","defaultname")
        val username = sharedNameValue
        var extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey( "msgStr" ))
                msgStr = extras.getString("msgStr")!!
            if (extras.containsKey( "type" ))
                typeStr = extras.getString("type")!!
            }

        if (typeStr.equals("notification"))
        {
            setTheme(R.style.Theme_AppCompat_Light_NoActionBar);
            setContentView(R.layout.activity_welcome)
            textmsg = findViewById<TextView?>(R.id.textViewWelcome)
            textmsg?.setText(msgStr)
        }

        showInfoDialog(this,msgStr,typeStr)
    }

    fun showInfoDialog(applicationContext: Context,msgStr:String,typeStr:String)
    {
        val builder = AlertDialog.Builder(applicationContext)
        //set title for alert dialog
        builder.setTitle("Notification")
        //set message for alert dialog
        builder.setMessage(msgStr)
        builder.setIcon(R.drawable.icon)

        //performing positive action
        builder.setPositiveButton("OK"){dialogInterface, which ->
//            Toast.makeText(applicationContext,"clicked yes",Toast.LENGTH_LONG).show()
//            finish()
            if (!typeStr.equals("notification")){
                finish()
            }
        }
//        //performing negative action
//        builder.setNegativeButton("No"){dialogInterface, which ->
////            Toast.makeText(applicationContext,"clicked No",Toast.LENGTH_LONG).show()
//        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()

    }
}
