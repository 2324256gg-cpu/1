package com.solcalendar
import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import java.util.TimeZone
object CalendarBridge {
 fun insert(c:Context,title:String,start:Long):Long? {
  val cur=c.contentResolver.query(CalendarContract.Calendars.CONTENT_URI,arrayOf(CalendarContract.Calendars._ID),"visible=1",null,null)?:return null
  val cal=cur.use{if(it.moveToFirst())it.getLong(0) else return null}
  val v=ContentValues().apply{put(CalendarContract.Events.CALENDAR_ID,cal);put(CalendarContract.Events.TITLE,title);put(CalendarContract.Events.DTSTART,start);put(CalendarContract.Events.DTEND,start+1800000L);put(CalendarContract.Events.EVENT_TIMEZONE,TimeZone.getDefault().id)}
  return c.contentResolver.insert(CalendarContract.Events.CONTENT_URI,v)?.lastPathSegment?.toLongOrNull()
 }
 fun read(c:Context):List<String>{val now=System.currentTimeMillis();val out= mutableListOf<String>();val u=CalendarContract.Instances.CONTENT_URI.buildUpon().appendPath(now.toString()).appendPath((now+604800000L).toString()).build();c.contentResolver.query(u,arrayOf(CalendarContract.Instances.TITLE,CalendarContract.Instances.BEGIN),null,null,"begin ASC")?.use{q->while(q.moveToNext()&&out.size<8)out.add("• "+android.text.format.DateFormat.format("HH:mm",q.getLong(1))+" "+(q.getString(0)?:"(untitled)"))};return out}
}