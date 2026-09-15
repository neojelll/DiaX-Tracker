package com.neojelll.diaxtracker.sensor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

private const val TAG = "GlucodataReceiver"
private const val EXTRA_GLUCODATA_MGDL = "glucodata.Minute.mgdl"
private const val EXTRA_GLUCODATA_TIME = "glucodata.Minute.Time"

// Manifest-declared, not runtime-registered: Juggluco/JugglucoNG's "Glucodata" recipient
// picker only lists apps PackageManager.queryBroadcastReceivers can find, which excludes
// runtime-only registrations like SensorForegroundService's.
class GlucodataReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        try {
            val mgdl = intent.getIntExtra(EXTRA_GLUCODATA_MGDL, -1)
            if (mgdl <= 0) return
            val timestamp = intent.getLongExtra(EXTRA_GLUCODATA_TIME, System.currentTimeMillis())
            persistSensorReading(context.applicationContext, mgdlToMmol(mgdl.toDouble()), timestamp)
        } catch (e: Exception) {
            Log.e(TAG, "Ignoring malformed glucodata broadcast", e)
        }
    }
}
