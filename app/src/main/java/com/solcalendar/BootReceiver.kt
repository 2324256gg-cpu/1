package com.solcalendar
import android.content.*
class BootReceiver:BroadcastReceiver(){override fun onReceive(c:Context,i:Intent){SolWidgetProvider.refresh(c)}}