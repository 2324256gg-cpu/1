package com.solcalendar

import android.Manifest
import android.app.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity() {
    private val store by lazy { ReminderStore(this) }
    private var chosen = System.currentTimeMillis() + 3600000L
    private val ink = Color.rgb(38, 52, 69)
    private val muted = Color.rgb(111, 129, 149)
    private val blue = Color.rgb(91, 125, 158)
    private val pale = Color.rgb(241, 246, 250)
    private val white = Color.WHITE

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        window.statusBarColor = Color.rgb(246, 249, 252)
        window.navigationBarColor = Color.rgb(246, 249, 252)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        val permissions = mutableListOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)
        if (android.os.Build.VERSION.SDK_INT >= 33) permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        val missing = permissions.filter { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }
        if (missing.isNotEmpty()) ActivityCompat.requestPermissions(this, missing.toTypedArray(), 1)
        render()
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun rounded(color: Int, radius: Int = 20, stroke: Int = 0): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
            if (stroke != 0) setStroke(dp(1), stroke)
        }

    private fun label(value: String, size: Float, color: Int, bold: Boolean = false): TextView =
        TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) setTypeface(typeface, Typeface.BOLD)
        }

    private fun render() {
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(18), dp(20), dp(28))
            setBackgroundColor(Color.rgb(246, 249, 252))
        }

        val top = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        top.addView(label("SOL  /  CALENDAR", 12f, blue, true))
        val today = Calendar.getInstance()
        top.addView(label(SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(today.time), 27f, ink, true).apply {
            setPadding(0, dp(8), 0, dp(3))
        })
        top.addView(label("把重要的日子，轻轻收好。", 14f, muted))
        page.addView(top, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(22) })

        val composer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(18))
            background = rounded(white, 22, Color.rgb(230, 237, 244))
            elevation = dp(2).toFloat()
        }
        composer.addView(label("新建提醒", 18f, ink, true))
        composer.addView(label("写下你不想忘记的事", 12f, muted).apply {
            setPadding(0, dp(4), 0, dp(14))
        })
        val name = EditText(this).apply {
            hint = "例如：给自己买一束花"
            textSize = 14f
            setTextColor(ink)
            setHintTextColor(Color.rgb(159, 172, 185))
            maxLines = 1
            setPadding(dp(14), 0, dp(14), 0)
            background = rounded(pale, 13)
        }
        composer.addView(name, LinearLayout.LayoutParams(-1, dp(50)))
        val dateButton = Button(this).apply {
            text = "◷   " + SimpleDateFormat("yyyy/MM/dd  HH:mm", Locale.getDefault()).format(Date(chosen))
            textSize = 13f
            isAllCaps = false
            setTextColor(blue)
            background = rounded(Color.rgb(238, 244, 249), 13)
            setOnClickListener {
                val c = Calendar.getInstance().apply { timeInMillis = chosen }
                DatePickerDialog(this@MainActivity, { _, y, m, d ->
                    c.set(y, m, d)
                    TimePickerDialog(this@MainActivity, { _, h, min ->
                        c.set(Calendar.HOUR_OF_DAY, h)
                        c.set(Calendar.MINUTE, min)
                        chosen = c.timeInMillis
                        text = "◷   " + SimpleDateFormat("yyyy/MM/dd  HH:mm", Locale.getDefault()).format(Date(chosen))
                    }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
            }
        }
        composer.addView(dateButton, LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(10) })
        val save = Button(this).apply {
            text = "保存提醒  →"
            textSize = 14f
            isAllCaps = false
            setTextColor(white)
            background = rounded(blue, 14)
            setOnClickListener {
                val title = name.text.toString().trim()
                if (title.isEmpty()) {
                    name.error = "先写下提醒内容"
                    return@setOnClickListener
                }
                val event = if (checkSelfPermission(Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED)
                    try { CalendarBridge.insert(this@MainActivity, title, chosen) } catch (_: Exception) { null } else null
                store.add(title, chosen, event)
                ReminderScheduler.schedule(this@MainActivity, title, chosen)
                SolWidgetProvider.refresh(this@MainActivity)
                Toast.makeText(this@MainActivity, if (event == null) "已保存到 Sol" else "已同步到手机日历", Toast.LENGTH_LONG).show()
                render()
            }
        }
        composer.addView(save, LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(12) })
        page.addView(composer, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(24) })

        val heading = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            addView(label("接下来", 19f, ink, true), LinearLayout.LayoutParams(0, -2, 1f))
            addView(label("UPCOMING", 10f, muted, true))
        }
        page.addView(heading, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) })

        val reminders = store.upcoming(30)
        if (reminders.isEmpty()) {
            val empty = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(16), dp(28), dp(16), dp(28))
                background = rounded(white, 20, Color.rgb(230, 237, 244))
                addView(label("✦", 26f, blue, true))
                addView(label("这里还很安静", 15f, ink, true).apply { setPadding(0, dp(8), 0, dp(4)) })
                addView(label("创建第一条提醒，让 Sol 帮你记着。", 12f, muted))
            }
            page.addView(empty, LinearLayout.LayoutParams(-1, -2))
        } else {
            reminders.forEach { r ->
                val item = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(dp(15), dp(14), dp(15), dp(14))
                    background = rounded(white, 17, Color.rgb(230, 237, 244))
                }
                val date = SimpleDateFormat("MM/dd\nHH:mm", Locale.getDefault()).format(Date(r.time))
                item.addView(label(date, 12f, blue, true), LinearLayout.LayoutParams(dp(58), -2))
                val divider = View(this).apply { setBackgroundColor(Color.rgb(222, 231, 239)) }
                item.addView(divider, LinearLayout.LayoutParams(dp(1), dp(34)).apply { marginEnd = dp(12) })
                item.addView(label(r.title, 14f, ink, true), LinearLayout.LayoutParams(0, -2, 1f))
                page.addView(item, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(9) })
            }
        }
        page.addView(label("SOL CALENDAR  ·  Made for your days", 10f, muted).apply {
            gravity = Gravity.CENTER
            setPadding(0, dp(24), 0, 0)
        })
        setContentView(ScrollView(this).apply {
            isFillViewport = true
            addView(page)
        })
    }
}
