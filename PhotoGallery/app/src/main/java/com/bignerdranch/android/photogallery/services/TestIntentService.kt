package com.bignerdranch.android.photogallery.services

import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService

class TestIntentService : JobIntentService() {
    override fun onHandleWork(intent: Intent) {
        Log.d("Services_LOGS", "TestIntentService onHandleWork")
        TODO("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("Services_LOGS", "TestIntentService onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("Services_LOGS", "TestIntentService onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("Services_LOGS", "TestIntentService onDestroy")
    }
}