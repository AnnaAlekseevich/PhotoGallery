package com.bignerdranch.android.photogallery.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import com.bignerdranch.android.photogallery.PhotoGalleryActivity
import com.bignerdranch.android.photogallery.R
import kotlinx.coroutines.*

class ForegroundService : Service() {
    override fun onBind(p0: Intent?): IBinder? {
        Log.d("Services_LOGS", "ForegroundService onBind")
        return null
    }

    override fun onCreate() {
        Log.d("Services_LOGS", "ForegroundService onCreate")
        super.onCreate()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("Services_LOGS", "ForegroundService onStartCommand")

        // If the notification supports a direct reply action, use
// PendingIntent.FLAG_MUTABLE instead.
        val pendingIntent: PendingIntent =
            Intent(this, PhotoGalleryActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(
                    this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
                )
            }

        val chan = NotificationChannel("CHANNEL_ID",
            "ANNA_CHANEL", NotificationManager.IMPORTANCE_NONE)
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)

        val notification: Notification = Notification.Builder(this, chan.id)
                .setContentTitle("Content TITLE")
                .setContentText("DESCRIPTION").setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent).setTicker("TICKER")
            .build()

// Notification ID cannot be 0.
        startForeground(10, notification)

        CoroutineScope(Dispatchers.IO).launch {
            async {
                doSomeLongWork()
            }
        }
        Log.d("Services_LOGS", "ForegroundService onStartCommand return")
        return super.onStartCommand(intent, START_FLAG_REDELIVERY, startId)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun doSomeLongWork() {
        for (i in 1..10) {
            delay(1000)
            Log.d("Services_LOGS", "TIK-TAK $i")
        }
    }

    override fun onDestroy() {
        Log.d("Services_LOGS", "ForegroundService onDestroy")
        super.onDestroy()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d("Services_LOGS", "ForegroundService onUnbind")
        return super.onUnbind(intent)
    }
}