package com.teovate.bigclock

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.util.TypedValue
import android.widget.RemoteViews

class ClockWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        for (id in ids) manager.updateAppWidget(id, buildViews(context))
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        // Device zone changed (e.g. you landed in Tokyo): refresh the "local" city label.
        if (intent.action == Intent.ACTION_TIMEZONE_CHANGED) updateAll(context)
    }

    companion object {
        fun updateAll(context: Context) {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(ComponentName(context, ClockWidgetProvider::class.java))
            for (id in ids) mgr.updateAppWidget(id, buildViews(context))
        }

        /** Shared by the real widget and the in-app preview so they look identical. */
        fun buildViews(context: Context): RemoteViews {
            val v = RemoteViews(context.packageName, R.layout.clock_widget)
            val size = ClockConfig.size(context).toFloat()
            val zone = ClockConfig.cityZone(context)

            v.setTextViewText(R.id.left_city, ClockConfig.localName())
            v.setTextViewText(R.id.right_city, ClockConfig.cityName(context))

            // Left clocks have no zone set, so they follow the system time zone automatically.
            for (id in intArrayOf(R.id.right_time, R.id.right_ampm, R.id.right_date)) {
                v.setString(id, "setTimeZone", zone)
            }
            for (p in listOf("left", "right")) {
                val (t, a, d, c) = when (p) {
                    "left" -> listOf(R.id.left_time, R.id.left_ampm, R.id.left_date, R.id.left_city)
                    else -> listOf(R.id.right_time, R.id.right_ampm, R.id.right_date, R.id.right_city)
                }
                v.setTextViewTextSize(t, TypedValue.COMPLEX_UNIT_SP, size)
                v.setTextViewTextSize(a, TypedValue.COMPLEX_UNIT_SP, size * 0.45f)
                v.setTextViewTextSize(d, TypedValue.COMPLEX_UNIT_SP, size * 0.32f)
                v.setTextViewTextSize(c, TypedValue.COMPLEX_UNIT_SP, size * 0.32f)
            }

            val pi = PendingIntent.getActivity(
                context, 0, Intent(AlarmClock.ACTION_SHOW_ALARMS),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            v.setOnClickPendingIntent(R.id.widget_root, pi)
            return v
        }
    }
}
