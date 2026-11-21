package com.solcalendar
import android.app.PendingIntent
import android.appwidget.*
import android.content.*
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.*
class SolWidgetProvider:AppWidgetProvider(){
 override fun onUpdate(c:Context,m:AppWidgetManager,ids:IntArray){
  val own=ReminderStore(c).upcoming(4).map{"• "+SimpleDateFormat("HH:mm",Locale.getDefault()).format(Date(it.time))+" "+it.title}
  val ext=if(c.checkSelfPermission(android.Manifest.permission.READ_CALENDAR)==android.content.pm.PackageManager.PERMISSION_GRANTED)try{CalendarBridge.read(c).take(3)}catch(_:Exception){emptyList()}else emptyList()
  val rows=(own+ext).distinct().take(5)
  ids.forEach{id->val v=RemoteViews(c.packageName,R.layout.widget_sol);v.setTextViewText(R.id.widget_date,SimpleDateFormat("MM/dd E",Locale.getDefault()).format(Date()));v.setTextViewText(R.id.widget_count,"接下来 · "+rows.size+" 项");v.setTextViewText(R.id.widget_items,if(rows.isEmpty())"还没有安排\n点击添加提醒" else rows.joinToString("\n"));val pi=PendingIntent.getActivity(c,0,Intent(c,MainActivity::class.java),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE);v.setOnClickPendingIntent(R.id.widget_root,pi);m.updateAppWidget(id,v)}
 }
 companion object{fun refresh(c:Context){val m=AppWidgetManager.getInstance(c);SolWidgetProvider().onUpdate(c,m,m.getAppWidgetIds(ComponentName(c,SolWidgetProvider::class.java)))}}
}