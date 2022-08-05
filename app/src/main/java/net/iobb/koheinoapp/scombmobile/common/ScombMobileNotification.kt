package net.iobb.koheinoapp.scombmobile.common

import android.R
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import java.util.*


class ScombMobileNotification : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val content = intent.getStringExtra("content")
        Toast.makeText(context, content, Toast.LENGTH_SHORT).show()
    }

    companion object {
        fun setAlerm(context: Context, notifyAfter: Long, content: String){
            val alarmMgr: AlarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
            val alarmIntent: PendingIntent = Intent(context, ScombMobileNotification::class.java).let { intent ->
                intent.putExtra("content", content)
                PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_MUTABLE)
            }
            alarmMgr.set(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + notifyAfter,
//                notifyTime.timeInMillis,
                alarmIntent
            )
        }
    }
}