package com.example.parentalcontrol

import android.Manifest
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.parentalcontrol.ui.theme.ParentalControlTheme
import com.google.firebase.FirebaseApp
import androidx.compose.ui.Alignment
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.Locale
import java.util.Date
import java.text.SimpleDateFormat
import com.example.parentalcontrol.AppUsageWorker
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.ExistingPeriodicWorkPolicy
import java.util.concurrent.TimeUnit
class MainActivity : ComponentActivity() {

    private val SMS_PERMISSION_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Firebase initialization
        FirebaseApp.initializeApp(this)

        if (!hasSmsPermission()) {
            requestSmsPermission()
        } else {
            // 🔁 Schedule periodic upload only if permission is granted
            schedulePeriodicUsageUpload(this)
        }

        setContent {
            ParentalControlTheme {
                UsageTrackerScreen(this)
            }
        }
        if (hasSmsPermission()) {
            schedulePeriodicUsageUpload(this)
        }


    }

    // ✅ Check SMS permissions
    private fun hasSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
    }

    // ✅ Request SMS permissions
    private fun requestSmsPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS),
            SMS_PERMISSION_CODE
        )
    }

    // ✅ Handle permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS permission granted!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "SMS permission is required to read messages.", Toast.LENGTH_LONG).show()
            }
        }
    }
}

@Composable
fun UsageTrackerScreen(context: Context) {
    val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    var usageData by remember { mutableStateOf(listOf<String>()) }
    var isLoggingEnabled by remember { mutableStateOf(prefs.getBoolean("log_sms", true)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Check App Usage Button
        Button(onClick = {
            if (!hasUsageAccessPermission(context)) {
                context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            } else {
                usageData = getTopUsedApps(context)
            }
        }) {
            Text("Check App Usage")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ SMS Logging Toggle Switch
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Enable SMS Logging")
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = isLoggingEnabled,
                onCheckedChange = {
                    isLoggingEnabled = it
                    prefs.edit().putBoolean("log_sms", it).apply()
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display App Usage Data
        usageData.forEach {
            Text(text = it)
        }
    }
}



fun hasUsageAccessPermission(context: Context): Boolean {
    val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    val endTime = System.currentTimeMillis()
    val startTime = endTime - 1000 * 60
    val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
    return stats != null && stats.isNotEmpty()
}



fun schedulePeriodicUsageUpload(context: Context) {
    val workRequest = PeriodicWorkRequestBuilder<AppUsageWorker>(3, TimeUnit.HOURS)
        .addTag("periodicUsageUpload")
        .build()


    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "periodicUsageUploader",
        androidx.work.ExistingPeriodicWorkPolicy.UPDATE,
        workRequest
    )
}
