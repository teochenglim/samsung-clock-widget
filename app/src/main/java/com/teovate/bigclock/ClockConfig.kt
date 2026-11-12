package com.teovate.bigclock

import android.content.Context
import java.util.TimeZone

/** Stored settings plus the list of cities offered for the second clock. */
object ClockConfig {
    const val MIN_SIZE = 28
    const val MAX_SIZE = 60
    private const val DEFAULT_SIZE = 44

    val cities: List<Pair<String, String>> = listOf(
        "San Francisco" to "America/Los_Angeles",
        "New York" to "America/New_York",
        "Chicago" to "America/Chicago",
        "Denver" to "America/Denver",
        "Vancouver" to "America/Vancouver",
        "Toronto" to "America/Toronto",
        "Mexico City" to "America/Mexico_City",
        "São Paulo" to "America/Sao_Paulo",
        "London" to "Europe/London",
        "Dublin" to "Europe/Dublin",
        "Paris" to "Europe/Paris",
        "Berlin" to "Europe/Berlin",
        "Amsterdam" to "Europe/Amsterdam",
        "Zurich" to "Europe/Zurich",
        "Madrid" to "Europe/Madrid",
        "Rome" to "Europe/Rome",
        "Istanbul" to "Europe/Istanbul",
        "Moscow" to "Europe/Moscow",
        "Cairo" to "Africa/Cairo",
        "Johannesburg" to "Africa/Johannesburg",
        "Dubai" to "Asia/Dubai",
        "Mumbai" to "Asia/Kolkata",
        "Bangkok" to "Asia/Bangkok",
        "Jakarta" to "Asia/Jakarta",
        "Kuala Lumpur" to "Asia/Kuala_Lumpur",
        "Singapore" to "Asia/Singapore",
        "Hong Kong" to "Asia/Hong_Kong",
        "Shanghai" to "Asia/Shanghai",
        "Taipei" to "Asia/Taipei",
        "Manila" to "Asia/Manila",
        "Seoul" to "Asia/Seoul",
        "Tokyo" to "Asia/Tokyo",
        "Perth" to "Australia/Perth",
        "Sydney" to "Australia/Sydney",
        "Auckland" to "Pacific/Auckland",
        "Honolulu" to "Pacific/Honolulu",
        "UTC" to "UTC",
        // Fillers so every whole-hour offset from UTC-12 to UTC+14 has at least one city
        "Baker Island" to "Etc/GMT+12",
        "Pago Pago" to "Pacific/Pago_Pago",
        "Anchorage" to "America/Anchorage",
        "Halifax" to "America/Halifax",
        "Fernando de Noronha" to "America/Noronha",
        "Azores" to "Atlantic/Azores",
        "Reykjavik" to "Atlantic/Reykjavik",
        "Karachi" to "Asia/Karachi",
        "Dhaka" to "Asia/Dhaka",
        "Vladivostok" to "Asia/Vladivostok",
        "Noumea" to "Pacific/Noumea",
        "Fiji" to "Pacific/Fiji",
        "Tonga" to "Pacific/Tongatapu",
        "Kiritimati" to "Pacific/Kiritimati",
    )

    private fun offsetMin(zone: String): Int = TimeZone.getTimeZone(zone).getOffset(System.currentTimeMillis()) / 60000

    /** Cities sorted west to east by their current UTC offset (DST-aware), then by name. */
    val sortedCities: List<Pair<String, String>>
        get() = cities.sortedWith(compareBy({ offsetMin(it.second) }, { it.first }))

    /** e.g. "UTC+5:30  ·  Mumbai" */
    fun label(city: Pair<String, String>): String {
        val m = offsetMin(city.second)
        val sign = if (m < 0) "-" else "+"
        val a = Math.abs(m)
        val off = if (a % 60 == 0) "${a / 60}" else "${a / 60}:%02d".format(a % 60)
        return "UTC$sign$off  ·  ${city.first}"
    }

    private fun prefs(c: Context) = c.getSharedPreferences("bigclock", Context.MODE_PRIVATE)

    fun cityName(c: Context): String = prefs(c).getString("city_name", "Vancouver")!!
    fun cityZone(c: Context): String = prefs(c).getString("city_zone", "America/Vancouver")!!
    fun size(c: Context): Int = prefs(c).getInt("size", DEFAULT_SIZE)

    fun setCity(c: Context, name: String, zone: String) {
        prefs(c).edit().putString("city_name", name).putString("city_zone", zone).apply()
    }

    fun setSize(c: Context, sp: Int) {
        prefs(c).edit().putInt("size", sp.coerceIn(MIN_SIZE, MAX_SIZE)).apply()
    }

    /** Label for the device's current (GPS/network driven) time zone. */
    fun localName(): String {
        val id = TimeZone.getDefault().id
        cities.firstOrNull { it.second == id }?.let { return it.first }
        return id.substringAfterLast('/').replace('_', ' ')
    }
}
