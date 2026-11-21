package com.solcalendar
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
data class Reminder(val id:Long,val title:String,val time:Long,val eventId:Long?)
class ReminderStore(c:Context):SQLiteOpenHelper(c,"sol_calendar.db",null,1){
 override fun onCreate(db:SQLiteDatabase){db.execSQL("CREATE TABLE reminders (id INTEGER PRIMARY KEY AUTOINCREMENT,title TEXT NOT NULL,time INTEGER NOT NULL,event_id INTEGER)")}
 override fun onUpgrade(db:SQLiteDatabase,o:Int,n:Int){}
 fun add(title:String,time:Long,eventId:Long?){val v=ContentValues().apply{put("title",title);put("time",time);if(eventId!=null)put("event_id",eventId)};writableDatabase.insert("reminders",null,v)}
 fun upcoming(limit:Int=5):List<Reminder>{val a= mutableListOf<Reminder>();readableDatabase.query("reminders",null,"time>=?",arrayOf(System.currentTimeMillis().toString()),null,null,"time ASC",limit.toString()).use{c->while(c.moveToNext())a+=Reminder(c.getLong(0),c.getString(1),c.getLong(2),if(c.isNull(3))null else c.getLong(3))};return a}
}