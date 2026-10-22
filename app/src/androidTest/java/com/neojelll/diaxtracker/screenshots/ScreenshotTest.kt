package com.neojelll.diaxtracker.screenshots

import android.app.Activity
import android.os.ParcelFileDescriptor
import androidx.annotation.StringRes
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.neojelll.diaxtracker.MainActivity
import com.neojelll.diaxtracker.R
import com.neojelll.diaxtracker.time.AppClock
import java.io.File
import org.junit.After
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The screenshot stand (#191): every feature on the demo diary ([DemoFixtures]), one test per
 * scenario - its own data, a fresh start, one or more shots. Shots land in the app's external files
 * (`screenshots/<locale>/NN-name.png`); `scripts/screenshots.sh` pulls them into `docs/screenshots/`.
 *
 * A new feature gets a scenario here - that's how "every feature on the screenshots" stays true.
 * The locale comes from the `screenshotLocale` instrumentation argument ("ru" by default).
 */
@RunWith(AndroidJUnit4::class)
class ScreenshotTest {

    @get:Rule
    val compose = createEmptyComposeRule()

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val device = UiDevice.getInstance(instrumentation)
    private var scenario: ActivityScenario<MainActivity>? = null
    private var activity: Activity? = null

    @After
    fun tearDown() {
        scenario?.close()
        AppClock.reset()
    }

    // ---- Home ----

    @Test
    fun home() = start {
        shot("01-home")
    }

    @Test
    fun homeFilledAndSaved() = start {
        // The sensor button, then a preset from the list - the form as it's filled in real use.
        compose.onNodeWithText(text(R.string.sugar_fill_from_sensor, "").trim(), substring = true).performClick()
        compose.onNodeWithText(text(R.string.food_pick_preset)).performClick()
        shot("02-home-presets")
        compose.onAllNodesWithText("Сырники").onFirst().performClick()
        shot("03-home-filled")
        // The toast comes once the database has the entry (off the main thread) and goes after a few
        // seconds. The test clock would skip that delay while waiting, so step it frame by frame instead.
        compose.mainClock.autoAdvance = false
        compose.onNodeWithText(text(R.string.save_entry_button)).performClick()
        val deadline = System.currentTimeMillis() + TOAST_TIMEOUT_MILLIS
        while (compose.onAllNodesWithText(text(R.string.toast_record_saved)).fetchSemanticsNodes().isEmpty()) {
            check(System.currentTimeMillis() < deadline) { "no toast after saving" }
            Thread.sleep(FRAME_MILLIS)
            compose.mainClock.advanceTimeByFrame()
        }
        compose.mainClock.advanceTimeBy(TOAST_IN_MILLIS)
        shot("04-home-saved-toast")
        compose.mainClock.autoAdvance = true
    }

    @Test
    fun homeSensorStale() = start(DemoFixtures.Sensor.STALE) {
        shot("05-home-sensor-stale")
    }

    @Test
    fun homeNoSensor() = start(DemoFixtures.Sensor.NONE) {
        shot("06-home-no-sensor")
    }

    @Test
    fun activeInsulinAndNotifications() = start {
        compose.onNodeWithText(text(R.string.insulin_units_suffix), substring = true).performClick()
        shot("07-active-insulin")
        compose.onNodeWithContentDescription(text(R.string.notif_panel_title)).performClick()
        shot("08-notifications")
    }

    // ---- History ----

    @Test
    fun history() = start {
        tab(R.string.nav_history)
        shot("10-history")
        // Breakfast: preset, photo, after-meal line, note - everything an opened record can hold.
        compose.onNodeWithText("08:20").performClick()
        shot("11-history-open-record")
        // Lunch: two after-meal checks in, two still to come.
        compose.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText("13:35"))
        compose.onNodeWithText("12:20").performClick()
        shot("12-history-checks-pending")
    }

    @Test
    fun historyYesterday() = start {
        tab(R.string.nav_history)
        // Scrolled to the end: today's header stays pinned on top, yesterday's shows its totals with the goal reached.
        compose.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText(text(R.string.history_no_earlier)))
        shot("13-history-scrolled")
    }

    // ---- Food ----

    @Test
    fun food() = start {
        tab(R.string.nav_meal_presets)
        shot("20-food")
        compose.onNodeWithText("Гречка с курицей").performClick()
        shot("21-preset")
        compose.onNodeWithText(text(R.string.action_edit)).performClick()
        shot("22-preset-edit")
    }

    // ---- Settings ----

    @Test
    fun settings() = start {
        tab(R.string.nav_settings)
        shot("30-settings")
        // The card after the goal, so the goal card stands whole above the dock.
        compose.onAllNodes(hasScrollAction()).onFirst().performScrollToNode(hasText(text(R.string.data_section_title)))
        shot("31-settings-carb-goal")
    }

    // ---- Plumbing ----

    /** Fresh demo data, the locale, then the app - the way a person opens it. */
    private fun start(sensor: DemoFixtures.Sensor = DemoFixtures.Sensor.FRESH, block: () -> Unit) {
        DemoFixtures.install(context, sensor)
        // The app's own language, set by the system before it starts - AppCompatDelegate applies it too late for the first frame.
        shell("cmd locale set-app-locales ${context.packageName} --locales $locale")
        scenario = ActivityScenario.launch(MainActivity::class.java).also { s -> s.onActivity { activity = it } }
        compose.waitForIdle()
        block()
    }

    private fun tab(@StringRes label: Int) {
        compose.onNodeWithContentDescription(text(label)).performClick()
    }

    /** A string as the running app shows it - in the locale it was started with. */
    private fun text(@StringRes id: Int, vararg args: Any): String = activity!!.getString(id, *args)

    /** The whole screen, status bar included (demo mode keeps it at 14:30 with a full battery). */
    private fun shot(name: String) {
        compose.waitForIdle()
        // Photos decode off the main thread; give them a moment so a shot never catches a blank tile.
        Thread.sleep(SETTLE_MILLIS)
        val dir = File(context.getExternalFilesDir(null), "screenshots/$locale").apply { mkdirs() }
        check(device.takeScreenshot(File(dir, "$name.png"))) { "screenshot $name failed" }
    }

    companion object {
        private const val SETTLE_MILLIS = 600L
        private const val TOAST_TIMEOUT_MILLIS = 5_000L
        private const val FRAME_MILLIS = 16L
        private const val TOAST_IN_MILLIS = 400L

        private val locale: String
            get() = InstrumentationRegistry.getArguments().getString("screenshotLocale") ?: "ru"

        @JvmStatic
        @BeforeClass
        fun quietDevice() {
            // No animations (no frame caught mid-slide) and the status bar in demo mode: 14:30, full battery, no notifications.
            shell(
                "settings put global window_animation_scale 0",
                "settings put global transition_animation_scale 0",
                "settings put global animator_duration_scale 0",
                "settings put global sysui_demo_allowed 1",
                "am broadcast -a com.android.systemui.demo -e command enter",
                "am broadcast -a com.android.systemui.demo -e command clock -e hhmm 1430",
                "am broadcast -a com.android.systemui.demo -e command battery -e level 100 -e plugged false",
                "am broadcast -a com.android.systemui.demo -e command network -e wifi show -e level 4 -e mobile show -e level 4 -e datatype none",
                "am broadcast -a com.android.systemui.demo -e command notifications -e visible false"
            )
        }

        @JvmStatic
        @AfterClass
        fun restoreDevice() {
            shell(
                "am broadcast -a com.android.systemui.demo -e command exit",
                "settings put global window_animation_scale 1",
                "settings put global transition_animation_scale 1",
                "settings put global animator_duration_scale 1"
            )
            val app = InstrumentationRegistry.getInstrumentation().targetContext.packageName
            shell("cmd locale set-app-locales $app --locales \"\"")
        }

        /** Runs each command and waits for it to finish (reading its output to the end). */
        private fun shell(vararg commands: String) {
            val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
            commands.forEach { command ->
                ParcelFileDescriptor.AutoCloseInputStream(automation.executeShellCommand(command)).use { it.readBytes() }
            }
        }
    }
}
