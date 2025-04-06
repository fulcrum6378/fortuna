package ir.mahdiparastesh.fortuna

import android.Manifest
import android.app.Application
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import ir.mahdiparastesh.chrono.IranianChronology
import ir.mahdiparastesh.fortuna.sect.TodayWidget
import ir.mahdiparastesh.fortuna.util.NumberUtils.toKey
import java.io.File
import java.time.chrono.ChronoLocalDate
import java.time.chrono.Chronology
import java.time.chrono.HijrahChronology
import java.time.chrono.IsoChronology
import java.time.chrono.JapaneseChronology
import java.time.temporal.ChronoField
import java.util.Locale

class Fortuna : Application(), FortunaContext {

    override lateinit var vita: Vita
    override val stored by lazy { File(filesDir, getString(R.string.export_file)) }
    override val backup by lazy { File(filesDir, getString(R.string.backup_file)) }
    override lateinit var date: ChronoLocalDate
    override lateinit var luna: String
    override lateinit var todayDate: ChronoLocalDate
    override lateinit var todayLuna: String


    @Suppress("KotlinConstantConditions")
    override val chronology: Chronology = when (BuildConfig.FLAVOR) {
        "iranian" -> IranianChronology.INSTANCE
        "gregorian" -> IsoChronology.INSTANCE
        else -> throw IllegalStateException("Unknown chronology type!")
    }

    override val otherCalendars: List<Chronology> = arrayOf(
        IranianChronology.INSTANCE,
        IsoChronology.INSTANCE,
        HijrahChronology.INSTANCE,
        JapaneseChronology.INSTANCE,
    ).filter { it != chronology }


    /** @return the main shared preferences instance; <code>settings.xml</code>. */
    val sp: SharedPreferences by lazy { getSharedPreferences("settings", MODE_PRIVATE) }

    val locale: Locale = Locale.UK  // never ever use SimpleDateFormat

    /**
     * List of all the required permissions.
     *
     * Note: Change [Main.reqPermLauncher] to RequestMultiplePermissions() if you wanna add more.
     */
    val requiredPermissions =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            arrayOf(Manifest.permission.POST_NOTIFICATIONS)
        else arrayOf()


    override fun onCreate() {
        super.onCreate()

        // prepare the Vita
        date = chronology.dateNow()
        luna = date.toKey()
        vita = Vita(this)
        updateToday()
        if (luna !in vita)
            vita[todayLuna] = Luna(date.lengthOfMonth())
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        TodayWidget.externalUpdate(this)
    }

    fun isLandscape() =
        resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}

/* TODO:
  * A new icon
  * New screenshots at /screenshots/
*/
