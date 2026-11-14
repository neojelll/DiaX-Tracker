package com.neojelll.diaxtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import androidx.work.WorkManager
import com.neojelll.diaxtracker.sensor.SensorForegroundService
import com.neojelll.diaxtracker.ui.navigation.NavGraph
import com.neojelll.diaxtracker.ui.theme.DiaXTrackerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        SensorForegroundService.start(this)
        cancelStalePostMealWorkOnce()

        setContent {
            DiaXTrackerTheme {
                Surface {
                    val navController = rememberNavController()
                    NavGraph(navController = navController)
                }
            }
        }
    }

    private fun cancelStalePostMealWorkOnce() {
        val prefs = getSharedPreferences("migrations", MODE_PRIVATE)
        if (prefs.getBoolean(KEY_CANCELLED_STALE_WORK, false)) return
        WorkManager.getInstance(this).cancelAllWork()
        prefs.edit().putBoolean(KEY_CANCELLED_STALE_WORK, true).apply()
    }

    private companion object {
        const val KEY_CANCELLED_STALE_WORK = "cancelled_stale_post_meal_work"
    }
}
