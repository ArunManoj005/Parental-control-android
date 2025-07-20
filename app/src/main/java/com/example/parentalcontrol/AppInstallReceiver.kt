package com.example.parentalcontrol.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import com.google.firebase.database.FirebaseDatabase

class AppInstallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val packageName = intent.data?.encodedSchemeSpecificPart ?: return
        val action = intent.action ?: return

        val pm: PackageManager = context.packageManager
        val appName = try {
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            "Unknown App"
        }

        val log = mapOf(
            "package" to packageName,
            "appName" to appName,
            "action" to action,
            "timestamp" to System.currentTimeMillis()
        )

        Log.d("AppInstallReceiver", "App installed: $appName ($packageName)")

        FirebaseDatabase.getInstance()
            .getReference("install_events")
            .push()
            .setValue(log)
    }
}
