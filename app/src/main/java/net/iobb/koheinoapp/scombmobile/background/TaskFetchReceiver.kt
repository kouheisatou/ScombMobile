package net.iobb.koheinoapp.scombmobile.background

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import java.util.*

class TasksFetchReceiver : BroadcastReceiver() {

    companion object {
        fun resumeBackgroundTask(context: Context, sessionId: String?, executeTime: Long, interval: Long){
            val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val alarmIntent: PendingIntent = Intent(context, TasksFetchReceiver::class.java).let { intent ->
                intent.putExtra("session_id", sessionId)
                PendingIntent.getBroadcast(context, 2, intent, PendingIntent.FLAG_IMMUTABLE)
            }
//            alarmManager.setInexactRepeating(
//                AlarmManager.RTC_WAKEUP,
//                executeTime,
//                interval,
//                alarmIntent
//            )
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(Calendar.getInstance().timeInMillis + 1000, alarmIntent),
                alarmIntent
            )
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val sessionId = intent?.getStringExtra("session_id")
        Toast.makeText(context, "${this::class.simpleName}#onReceive()", Toast.LENGTH_SHORT).show()
        Toast.makeText(context, "session_id=${sessionId}", Toast.LENGTH_SHORT).show()

        val startServiceIntent = Intent(context, TasksFetchDemon::class.java)
        startServiceIntent.putExtra("session_id", sessionId)
        context?.startService(startServiceIntent)
    }




}