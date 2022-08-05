package net.iobb.koheinoapp.scombmobile.common

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import net.iobb.koheinoapp.scombmobile.R
import net.iobb.koheinoapp.scombmobile.ui.task.Task
import java.util.*

val CHANNEL_ID = "SCOMB_MOBILE_NOTIFICATION"

class ScombMobileNotification : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val content = intent.getStringExtra("content")
        val id = intent.getIntExtra("id", 0)
        sendNotification(context, "Scomb課題締切り", content ?: "", id)
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
            val alarmManager: AlarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
            val alarmIntent: PendingIntent = Intent(context, ScombMobileNotification::class.java).let { intent ->
                intent.putExtra("content", task.title)
                intent.putExtra("id", task.taskId)
                PendingIntent.getBroadcast(context, task.taskId, intent, PendingIntent.FLAG_IMMUTABLE)
            }
            alarmManager.set(
                AlarmManager.RTC,
                task.deadLineTime,
                alarmIntent
            )
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
                "ScombMobile通知", // チャンネル名
                NotificationManager.IMPORTANCE_DEFAULT) // 重要度

            // システムに通知チャンネルを登録
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}