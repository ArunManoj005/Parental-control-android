package com.example.parentalcontrol.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.firebase.database.FirebaseDatabase

class AppInstallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val packageName = intent.data?.encodedSchemeSpecificPart ?: return
        val action = intent.action ?: return

        val log = mapOf(
            "package" to packageName,
            "action" to action,
            "timestamp" to System.currentTimeMillis()
        )

        FirebaseDatabase.getInstance()
            .getReference("install_events")
            .push()
            .setValue(log)
    }
}
