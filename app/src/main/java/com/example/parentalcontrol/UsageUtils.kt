package com.example.parentalcontrol

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context

fun getTopUsedApps(context: Context): List<String> {
    val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    val endTime = System.currentTimeMillis()
    val startTime = endTime - 1000 * 60 * 60 // last 1 hour

    val events = usm.queryEvents(startTime, endTime)
    val usageMap = mutableMapOf<String, Long>()
    val lastUsedMap = mutableMapOf<String, Long>()

    val event = UsageEvents.Event()
    while (events.hasNextEvent()) {
        events.getNextEvent(event)
        if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
            lastUsedMap[event.packageName] = event.timeStamp
        } else if (event.eventType == UsageEvents.Event.MOVE_TO_BACKGROUND) {
            val fgTime = lastUsedMap[event.packageName] ?: continue
            val duration = event.timeStamp - fgTime
            if (duration in 1..1000 * 60 * 60) {
                usageMap[event.packageName] = usageMap.getOrDefault(event.packageName, 0) + duration
            }
        }
    }

    return usageMap.entries
        .sortedByDescending { it.value } // sort all by usage time
        .map {
            val minutes = it.value / 1000 / 60
            "${it.key}: $minutes min"
        }
}

