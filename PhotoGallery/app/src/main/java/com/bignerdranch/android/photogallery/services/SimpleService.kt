package com.bignerdranch.android.photogallery.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.*
import okhttp3.Dispatcher
import kotlin.coroutines.CoroutineContext

class SimpleService : Service() {
    override fun onBind(p0: Intent?): IBinder? {
        Log.d("Services_LOGS", "SimpleService onBind")
        return null
    }

    override fun onCreate() {
        Log.d("Services_LOGS", "SimpleService onCreate")
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("Services_LOGS", "SimpleService onStartCommand")

        CoroutineScope(Dispatchers.IO).launch {
            doSomeLongWork()
        }
        Log.d("Services_LOGS", "SimpleService onStartCommand return")
        return super.onStartCommand(intent, START_FLAG_REDELIVERY, startId)
    }

    private suspend fun doSomeLongWork() {
        for (i in 1..10) {
            delay(1000)
            Log.d("Services_LOGS", "TIK-TAK $i")
        }
        stopSelf()
    }

    override fun onDestroy() {
        Log.d("Services_LOGS", "SimpleService onDestroy")
        super.onDestroy()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d("Services_LOGS", "SimpleService onUnbind")
        return super.onUnbind(intent)
    }
}