package com.example.parentalcontrol

import android.app.usage.UsageStatsManager
import android.content.Context

fun getTopUsedApps(context: Context): List<String> {
    val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    val endTime = System.currentTimeMillis()
    val startTime = endTime - 1000 * 60 * 60 * 24 // last 24 hours
    val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)

    return stats
        .filter { it.totalTimeInForeground > 0 }
        .sortedByDescending { it.totalTimeInForeground }
        .take(5)
        .map {
            val minutes = it.totalTimeInForeground / 1000 / 60
            "${it.packageName}: ${minutes} min"
        }
}
