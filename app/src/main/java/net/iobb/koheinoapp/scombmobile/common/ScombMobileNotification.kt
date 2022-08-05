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

        // 通知の送信
        with (NotificationManagerCompat.from(context)) {
            notify(id, notification) // 通知ID (更新、削除で利用)
        }
    }

    companion object {
        fun setTaskAlerm(context: Context, task: Task){
            setAlarm(context, task.deadLineTime, task.title, task.taskId.hashCode())
        }

        fun setAlarm(context: Context, notifyTimeMillis: Long, content: String, id: Int){
            val alarmManager: AlarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
            val alarmIntent: PendingIntent = Intent(context, ScombMobileNotification::class.java).let { intent ->
                intent.putExtra("content", content)
                intent.putExtra("id", id)
                PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_IMMUTABLE)
            }
            alarmManager.set(
                AlarmManager.RTC,
                notifyTimeMillis,
                alarmIntent
            )
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