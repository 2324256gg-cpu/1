package com.solcalendar
import android.Manifest
import android.app.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.*
import androidx.core.app.ActivityCompat
import java.text.SimpleDateFormat
import java.util.*
class MainActivity:Activity(){
 private val store by lazy{ReminderStore(this)}
 private var chosen=System.currentTimeMillis()+3600000L
 override fun onCreate(b:Bundle?){super.onCreate(b);val ps= mutableListOf(Manifest.permission.READ_CALENDAR,Manifest.permission.WRITE_CALENDAR);if(android.os.Build.VERSION.SDK_INT>=33)ps.add(Manifest.permission.POST_NOTIFICATIONS);val missing=ps.filter{checkSelfPermission(it)!=PackageManager.PERMISSION_GRANTED};if(missing.isNotEmpty())ActivityCompat.requestPermissions(this,missing.toTypedArray(),1);render()}
 private fun render(){
  val box=LinearLayout(this).apply{orientation=1;setPadding(24,32,24,24);setBackgroundColor(0xFFF8FAFC.toInt())}
  box.addView(TextView(this).apply{text="SOL CALENDAR";textSize=24f;setTextColor(0xFF263445.toInt())})
  box.addView(TextView(this).apply{text="独立提醒 · 手机日历";textSize=13f;setTextColor(0xFF718096.toInt())})
  val name=EditText(this).apply{hint="提醒名称"};box.addView(name)
  box.addView(Button(this).apply{text="选择日期和时间";setOnClickListener{val c=Calendar.getInstance().apply{timeInMillis=chosen};DatePickerDialog(this@MainActivity,{_,y,m,d->c.set(y,m,d);TimePickerDialog(this@MainActivity,{_,h,mi->c.set(Calendar.HOUR_OF_DAY,h);c.set(Calendar.MINUTE,mi);chosen=c.timeInMillis},c.get(Calendar.HOUR_OF_DAY),c.get(Calendar.MINUTE),true).show()},c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show()}})
  box.addView(Button(this).apply{text="保存提醒并同步系统日历";setOnClickListener{val t=name.text.toString().trim();if(t.isEmpty())return@setOnClickListener;val event=if(checkSelfPermission(Manifest.permission.WRITE_CALENDAR)==PackageManager.PERMISSION_GRANTED)try{CalendarBridge.insert(this@MainActivity,t,chosen)}catch(_:Exception){null}else null;store.add(t,chosen,event);ReminderScheduler.schedule(this@MainActivity,t,chosen);SolWidgetProvider.refresh(this@MainActivity);Toast.makeText(this@MainActivity,if(event==null)"已保存到 Sol" else "已写入系统日历",Toast.LENGTH_LONG).show();render()}})
  box.addView(TextView(this).apply{text="我的提醒";textSize=18f;setPadding(0,24,0,8);setTextColor(0xFF263445.toInt())})
  store.upcoming(30).forEach{r->box.addView(TextView(this).apply{text=SimpleDateFormat("MM/dd HH:mm",Locale.getDefault()).format(Date(r.time))+"  "+r.title;textSize=14f;setPadding(0,8,0,8);setTextColor(0xFF263445.toInt())})}
  setContentView(ScrollView(this).apply{addView(box)})
 }
}