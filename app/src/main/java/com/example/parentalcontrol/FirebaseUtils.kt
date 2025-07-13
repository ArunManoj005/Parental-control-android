package com.example.parentalcontrol

import com.google.firebase.database.FirebaseDatabase
import com.yourpackage.name.model.SmsData

object FirebaseUtils {
    private val database = FirebaseDatabase.getInstance()
    private val smsRef = database.getReference("sms_logs")

    fun uploadSms(sms: SmsData) {
        val key = smsRef.push().key ?: System.currentTimeMillis().toString()
        smsRef.child(key).setValue(sms)
    }
}
