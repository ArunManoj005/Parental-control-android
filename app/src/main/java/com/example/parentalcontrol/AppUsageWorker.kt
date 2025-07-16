package com.example.parentalcontrol

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.parentalcontrol.getTopUsedApps
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class AppUsageWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        val context = applicationContext
        val usageList = getTopUsedApps(context)

        val ref = FirebaseDatabase.getInstance().getReference("daily_app_usage")
        val phoneId = android.os.Build.SERIAL ?: "UnknownDevice"
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val key = "$phoneId/$date"

        ref.child(key).setValue(usageList)
        return Result.success()
    }
}
