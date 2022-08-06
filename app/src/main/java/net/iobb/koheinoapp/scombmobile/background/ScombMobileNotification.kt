package net.iobb.koheinoapp.scombmobile.background

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.room.Room
import net.iobb.koheinoapp.scombmobile.R
import net.iobb.koheinoapp.scombmobile.common.AppDatabase
import net.iobb.koheinoapp.scombmobile.ui.task.Task
import java.util.*

val CHANNEL_ID = "SCOMB_MOBILE_NOTIFICATION"

class ScombMobileNotification : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val db = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "ScombMobileDB"
        ).allowMainThreadQueries().build()

        val enabledNotification = (db.settingDao().getSetting("task_notification")?.settingValue ?: "true") == "true"
        if(!enabledNotification){
            return
        }

        val content = intent.getStringExtra("content")
        val id = intent.getIntExtra("id", 0)

        val notificationReservationTime = intent.getLongExtra("deadline_time", 0)
        val now = Calendar.getInstance()
        val deltaT = (notificationReservationTime - now.timeInMillis) / 1000 / 60

        if(deltaT > 0){
            sendNotification(context, "Scomb課題締切り", "$content (${deltaT}分前)", id)
        }
    }

    // 通知の送信
    private fun sendNotification(context: Context, title: String, content: String, id: Int) {
        // 通知の生成
        val notification: Notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_sitelogo) // 小アイコン
            .setContentTitle(title) // タイトル
            .setContentText(content) // テキスト
            .setGroup("SCOMB_TASK_GROUP_KEY")
            .setPriority(NotificationCompat.PRIORITY_HIGH) // 重要度
            .build()

        with (NotificationManagerCompat.from(context)) {
            notify(id, notification)
        }
    }

    companion object {

        fun setTaskAlarm(context: Context, task: Task){
            val db = Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "ScombMobileDB"
            ).allowMainThreadQueries().build()

            // load notify timing setting
            val notifyTimeMillis = when(db.settingDao().getSetting("task_notify_time")?.settingValue){
                "0" -> 60000 * 10
                "1" -> 60000 * 30
                "2" -> 60000 * 60
                "3" -> 60000 * 60 * 2
                "4" -> 60000 * 60 * 3
                "5" -> 60000 * 60 * 24
                else -> 0
            }

            val alarmManager: AlarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
            val alarmIntent: PendingIntent = Intent(context, ScombMobileNotification::class.java).let { intent ->
                intent.putExtra("content", task.title)
                intent.putExtra("id", task.taskId)
                intent.putExtra("deadline_time", task.deadLineTime)
                PendingIntent.getBroadcast(context, task.taskId, intent, PendingIntent.FLAG_IMMUTABLE)
            }
            alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(task.deadLineTime - notifyTimeMillis, null), alarmIntent)
        }

        fun cancelTaskAlerm(context: Context, task: Task){
            val alarmManager: AlarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
            val alarmIntent: PendingIntent = Intent(context, ScombMobileNotification::class.java).let { intent ->
                intent.putExtra("content", task.title)
                intent.putExtra("id", task.taskId)
                PendingIntent.getBroadcast(context, task.taskId, intent, PendingIntent.FLAG_IMMUTABLE)
            }
            alarmManager.cancel(alarmIntent)
        }

        // 通知チャンネルの生成
        fun createNotificationChannel(context: Context) {
            // 通知チャンネルの生成
            val channel = NotificationChannel(
                CHANNEL_ID, // チャンネルID
                "Scomb課題締切通知", // チャンネル名
                NotificationManager.IMPORTANCE_DEFAULT) // 重要度

            // システムに通知チャンネルを登録
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}