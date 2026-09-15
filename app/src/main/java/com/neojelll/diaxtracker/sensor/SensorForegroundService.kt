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
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.data.DiaryDatabase
import com.neojelll.diaxtracker.data.SensorReadingLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

class SensorForegroundService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // This receiver is RECEIVER_EXPORTED, so any app on the device can send this
            // broadcast with malformed extras (wrong type -> ClassCastException). Never let
            // that crash the app.
            try {
                val reading = when (intent.action) {
                    ACTION_BG_ESTIMATE -> readXDripEstimate(intent)
                    ACTION_GLUCODATA_MINUTE -> readGlucodataMinute(intent)
                    else -> null
                } ?: return
                val (bloodSugar, timestamp) = reading
                SensorReadingStore(applicationContext).save(bloodSugar, timestamp)
                serviceScope.launch {
                    try {
                        DiaryDatabase.getDatabase(applicationContext).sensorReadingLogDao().insert(
                            SensorReadingLog(
                                timestamp = Instant.ofEpochMilli(timestamp)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDateTime(),
                                bloodSugar = bloodSugar
                            )
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to persist sensor reading", e)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ignoring malformed sensor broadcast", e)
            }
        }

        // xDrip+ / AndroidAPS-style broadcast (also what Juggluco sends in "xDrip broadcast" mode).
        private fun readXDripEstimate(intent: Intent): Pair<Float, Long>? {
            val mgdl = intent.getDoubleExtra(EXTRA_BG_ESTIMATE, -1.0)
            if (mgdl <= 0.0) return null
            val timestamp = intent.getLongExtra(EXTRA_TIME, System.currentTimeMillis())
            return mgdlToMmol(mgdl) to timestamp
        }

        // Juggluco's own native broadcast (also sent by the JugglucoNG fork, same protocol).
        private fun readGlucodataMinute(intent: Intent): Pair<Float, Long>? {
            val mgdl = intent.getIntExtra(EXTRA_GLUCODATA_MGDL, -1)
            if (mgdl <= 0) return null
            val timestamp = intent.getLongExtra(EXTRA_GLUCODATA_TIME, System.currentTimeMillis())
            return mgdlToMmol(mgdl.toDouble()) to timestamp
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        ContextCompat.registerReceiver(
            this,
            receiver,
            IntentFilter().apply {
                addAction(ACTION_BG_ESTIMATE)
                addAction(ACTION_GLUCODATA_MINUTE)
            },
            ContextCompat.RECEIVER_EXPORTED
        )
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        serviceScope.cancel()
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
        private const val TAG = "SensorForegroundService"
        private const val CHANNEL_ID = "sensor_listener"
        private const val NOTIFICATION_ID = 1
        const val ACTION_BG_ESTIMATE = "com.eveningoutpost.dexdrip.BgEstimate"
        private const val EXTRA_BG_ESTIMATE = "com.eveningoutpost.dexdrip.Extras.BgEstimate"
        private const val EXTRA_TIME = "com.eveningoutpost.dexdrip.Extras.Time"
        const val ACTION_GLUCODATA_MINUTE = "glucodata.Minute"
        private const val EXTRA_GLUCODATA_MGDL = "glucodata.Minute.mgdl"
        private const val EXTRA_GLUCODATA_TIME = "glucodata.Minute.Time"

        fun start(context: Context) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, SensorForegroundService::class.java)
            )
        }
    }
}
