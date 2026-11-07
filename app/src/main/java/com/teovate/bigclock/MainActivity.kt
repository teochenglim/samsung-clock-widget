package com.teovate.bigclock

import android.app.Activity
import android.app.AlertDialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast

/** Settings screen: live preview, second-city picker, size slider, "add widget" button. */
class MainActivity : Activity() {

    private lateinit var preview: FrameLayout
    private lateinit var cityButton: Button
    private lateinit var sizeLabel: TextView

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#121212"))
            setPadding(dp(20), dp(20), dp(20), dp(20))
        }
        root.setOnApplyWindowInsetsListener { v, insets ->
            val b = insets.getInsets(android.view.WindowInsets.Type.systemBars())
            v.setPadding(dp(20), b.top + dp(20), dp(20), b.bottom + dp(20))
            insets
        }

        fun text(s: String, sp: Float, color: String = "#FFFFFF") = TextView(this).apply {
            this.text = s; textSize = sp; setTextColor(Color.parseColor(color))
        }

        root.addView(text("Big World Clock", 28f).apply { setTypeface(typeface, android.graphics.Typeface.BOLD) })
        root.addView(text("Left follows your phone's location. Pick the city for the right.", 15f, "#AAAAAA"))

        // Preview on a wallpaper-ish backdrop
        preview = FrameLayout(this).apply {
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR, intArrayOf(Color.parseColor("#8A8578"), Color.parseColor("#3C4A55"))
            ).apply { cornerRadius = dp(16).toFloat() }
            setPadding(dp(8), dp(16), dp(8), dp(16))
        }
        root.addView(preview, LinearLayout.LayoutParams(MATCH_PARENT, dp(110)).apply { topMargin = dp(20) })

        root.addView(text("Second city", 14f, "#AAAAAA"), LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply { topMargin = dp(24) })
        cityButton = Button(this).apply { isAllCaps = false; textSize = 18f; setOnClickListener { pickCity() } }
        root.addView(cityButton, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))

        sizeLabel = text("", 14f, "#AAAAAA")
        root.addView(sizeLabel, LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply { topMargin = dp(16) })
        val seek = SeekBar(this).apply {
            max = ClockConfig.MAX_SIZE - ClockConfig.MIN_SIZE
            progress = ClockConfig.size(this@MainActivity) - ClockConfig.MIN_SIZE
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar, p: Int, fromUser: Boolean) {
                    if (fromUser) { ClockConfig.setSize(this@MainActivity, p + ClockConfig.MIN_SIZE); refresh() }
                }
                override fun onStartTrackingTouch(s: SeekBar) {}
                override fun onStopTrackingTouch(s: SeekBar) {}
            })
        }
        root.addView(seek, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))

        val add = Button(this).apply {
            text = "Add widget to home screen"; isAllCaps = false; textSize = 18f
            setOnClickListener { pinWidget() }
        }
        root.addView(add, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply { topMargin = dp(28) })
        root.addView(text("Tip: after adding, long-press the widget to resize it (5×1 is the default).", 13f, "#888888"),
            LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply { topMargin = dp(12); gravity = Gravity.START })

        setContentView(root)
        refresh()
    }

    override fun onResume() { super.onResume(); refresh() }

    private fun refresh() {
        cityButton.text = "${ClockConfig.cityName(this)}  ▾"
        sizeLabel.text = "Text size: ${ClockConfig.size(this)}"
        preview.removeAllViews()
        preview.addView(ClockWidgetProvider.buildViews(this).apply(this, preview))
        ClockWidgetProvider.updateAll(this)
    }

    private fun pickCity() {
        val names = ClockConfig.cities.map { it.first }.toTypedArray()
        AlertDialog.Builder(this).setTitle("Second city").setItems(names) { _, i ->
            val (n, z) = ClockConfig.cities[i]
            ClockConfig.setCity(this, n, z)
            refresh()
        }.show()
    }

    private fun pinWidget() {
        val mgr = AppWidgetManager.getInstance(this)
        if (mgr.isRequestPinAppWidgetSupported) {
            mgr.requestPinAppWidget(ComponentName(this, ClockWidgetProvider::class.java), null, null)
        } else {
            Toast.makeText(this, "Long-press home screen → Widgets → Big World Clock", Toast.LENGTH_LONG).show()
        }
    }
}
