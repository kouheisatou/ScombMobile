package net.iobb.koheinoapp.scombmobile.common

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import net.iobb.koheinoapp.scombmobile.R
import java.util.*

val CHANNEL_ID = "SCOMB_MOBILE_NOTIFICATION"

class ScombMobileNotification : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val content = intent.getStringExtra("content")
        Toast.makeText(context, content, Toast.LENGTH_SHORT).show()
        sendNotification(context, "Scomb課題締切り", content ?: "")
    }

    // 通知の送信
    private fun sendNotification(context: Context, title: String, content: String) {
        // 通知の生成
        val notification: Notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_sitelogo) // 小アイコン
            .setContentTitle(title) // タイトル
            .setContentText(content) // テキスト
            .setPriority(NotificationCompat.PRIORITY_DEFAULT) // 重要度
            .build()

        // 通知の送信
        with (NotificationManagerCompat.from(context)) {
            notify(1234, notification) // 通知ID (更新、削除で利用)
        }
    }

    companion object {
        fun setAlerm(context: Context, notifyAt: Calendar, content: String){
            val deltaT = Calendar.getInstance().timeInMillis - notifyAt.timeInMillis
            val alarmMgr: AlarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
            val alarmIntent: PendingIntent = Intent(context, ScombMobileNotification::class.java).let { intent ->
                intent.putExtra("content", content)
                PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            }
            alarmMgr.set(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + deltaT,
//                notifyTime.timeInMillis,
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