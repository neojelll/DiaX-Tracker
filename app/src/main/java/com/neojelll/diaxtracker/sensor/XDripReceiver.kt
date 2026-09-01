package com.neojelll.diaxtracker.sensor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class XDripReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val mgdl = intent.getDoubleExtra(EXTRA_BG_ESTIMATE, -1.0)
        if (mgdl <= 0.0) return

        val timestamp = intent.getLongExtra(EXTRA_TIME, System.currentTimeMillis())
        val mmol = (mgdl / MG_DL_PER_MMOL_L).toFloat()
        SensorReadingStore(context).save(mmol, timestamp)
    }

    companion object {
        const val ACTION_BG_ESTIMATE = "com.eveningoutpost.dexdrip.BgEstimate"
        private const val EXTRA_BG_ESTIMATE = "com.eveningoutpost.dexdrip.Extras.BgEstimate"
        private const val EXTRA_TIME = "com.eveningoutpost.dexdrip.Extras.Time"
        private const val MG_DL_PER_MMOL_L = 18.0182
    }
}
