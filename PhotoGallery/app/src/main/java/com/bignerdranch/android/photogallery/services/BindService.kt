package com.bignerdranch.android.photogallery.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import kotlin.random.Random

class BindService : Service() {

    // Binder given to clients
    private val binder = SuperBinder()

    override fun onBind(intent: Intent?): IBinder? {
        Log.d("Services_LOGS", "BindService onBind")
        return binder
    }

    fun makeTest1Text(): String {
        return "Magic number for test 1 is ${Random(100).nextInt()}"
    }

    fun makeTest2Text(): String {
        return "Magic number for test 2 is ${Random(-100).nextInt()}"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("Services_LOGS", "BindService onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("Services_LOGS", "BindService onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        Log.d("Services_LOGS", "BindService onDestroy")
        super.onDestroy()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d("Services_LOGS", "BindService onUnbind")
        return super.onUnbind(intent)
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        Log.d("Services_LOGS", "BindService onRebind")
    }

    inner class SuperBinder : Binder() {

        fun getService(): BindService = this@BindService
    }
}