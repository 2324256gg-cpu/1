package com.solcalendar
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
object ReminderScheduler{fun schedule(c:Context,title:String,time:Long){val a=c.getSystemService(Context.ALARM_SERVICE) as AlarmManager;val i=Intent(c,ReminderAlarmReceiver::class.java).putExtra("title",title);val p=PendingIntent.getBroadcast(c,title.hashCode(),i,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE);try{a.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,time,p)}catch(_:Exception){a.set(AlarmManager.RTC_WAKEUP,time,p)}}}