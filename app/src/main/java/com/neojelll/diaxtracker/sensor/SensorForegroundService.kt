package com.neojelll.diaxtracker.sensor

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.neojelll.diaxtracker.R

class SensorForegroundService : Service() {

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val mgdl = intent.getDoubleExtra(EXTRA_BG_ESTIMATE, -1.0)
            if (mgdl <= 0.0) return
            val timestamp = intent.getLongExtra(EXTRA_TIME, System.currentTimeMillis())
            SensorReadingStore(applicationContext).save(mgdlToMmol(mgdl), timestamp)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        ContextCompat.registerReceiver(
            this,
            receiver,
            IntentFilter(ACTION_BG_ESTIMATE),
            ContextCompat.RECEIVER_EXPORTED
        )
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.sensor_notification_channel),
            NotificationManager.IMPORTANCE_MIN
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.sensor_notification_text))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()

    companion object {
        private const val CHANNEL_ID = "sensor_listener"
        private const val NOTIFICATION_ID = 1
        const val ACTION_BG_ESTIMATE = "com.eveningoutpost.dexdrip.BgEstimate"
        private const val EXTRA_BG_ESTIMATE = "com.eveningoutpost.dexdrip.Extras.BgEstimate"
        private const val EXTRA_TIME = "com.eveningoutpost.dexdrip.Extras.Time"

        fun start(context: Context) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, SensorForegroundService::class.java)
            )
        }
    }
}
