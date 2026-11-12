package com.teovate.bigclock

import android.app.Activity
import android.app.AlertDialog
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle

/** Shown when a widget is added (or reconfigured): pick the second city, then the widget is placed. */
class WidgetConfigActivity : Activity() {

    private var finished = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = intent?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID
        setResult(RESULT_CANCELED, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id))
        if (id == AppWidgetManager.INVALID_APPWIDGET_ID) { finish(); return }

        val list = ClockConfig.sortedCities
        val names = list.map { ClockConfig.label(it) }.toTypedArray()
        val current = list.indexOfFirst { it.first == ClockConfig.cityName(this) }
        AlertDialog.Builder(this)
            .setTitle("Second city")
            .setSingleChoiceItems(names, current) { d, i ->
                val (n, z) = list[i]
                ClockConfig.setCity(this, n, z)
                ClockWidgetProvider.updateAll(this)
                setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id))
                finished = true
                d.dismiss()
                finish()
            }
            .setOnCancelListener { finish() }
            .show()
    }
}
