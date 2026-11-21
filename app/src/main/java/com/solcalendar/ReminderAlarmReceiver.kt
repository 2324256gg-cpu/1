package com.solcalendar
import android.app.*
import android.content.*
import android.os.Build
import androidx.core.app.NotificationCompat
class ReminderAlarmReceiver:BroadcastReceiver(){override fun onReceive(c:Context,i:Intent){val n=c.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager;if(Build.VERSION.SDK_INT>=26)n.createNotificationChannel(NotificationChannel("sol_reminders","Sol reminders",NotificationManager.IMPORTANCE_HIGH));n.notify((System.currentTimeMillis()%Int.MAX_VALUE).toInt(),NotificationCompat.Builder(c,"sol_reminders").setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle("Sol Calendar").setContentText(i.getStringExtra("title")?:"提醒时间到了").setAutoCancel(true).build())}}