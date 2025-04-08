package ir.mahdiparastesh.fortuna

import android.Manifest
import android.app.Application
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import ir.mahdiparastesh.chrono.IranianChronology
import ir.mahdiparastesh.fortuna.sect.TodayWidget
import java.io.File
import java.time.chrono.ChronoLocalDate
import java.time.chrono.Chronology
import java.time.chrono.HijrahChronology
import java.time.chrono.IsoChronology
import java.time.chrono.JapaneseChronology
import java.util.Locale

class Fortuna : Application(), FortunaContext {

    override lateinit var vita: Vita
    override val stored: File by lazy { File(filesDir, getString(R.string.export_file)) }
    override val backup: File by lazy { File(filesDir, getString(R.string.backup_file)) }
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

    override val otherChronologies: List<Chronology> by lazy {
        arrayOf(
            IranianChronology.INSTANCE,
            IsoChronology.INSTANCE,
            HijrahChronology.INSTANCE,
            JapaneseChronology.INSTANCE,
        ).filter { it != chronology }
    }


    /** @return the main shared preferences instance; <code>settings.xml</code>. */
    val sp: SharedPreferences by lazy { getSharedPreferences("settings", MODE_PRIVATE) }

    val locale: Locale = Locale.UK  // never ever use SimpleDateFormat

    /**
     * List of all the required permissions.
     *
     * Note: Change [Main.reqPermLauncher] to RequestMultiplePermissions() if you wanna add more.
     */
    val requiredPermissions: Array<String> by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            arrayOf(Manifest.permission.POST_NOTIFICATIONS)
        else arrayOf()
    }


    override fun onCreate() {
        super<Application>.onCreate()
        super<FortunaContext>.onCreate()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        TodayWidget.externalUpdate(this)
    }

    fun isLandscape() =
        resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE


    companion object {
        const val VITA_MIME_TYPE = "application/octet-stream"

        /* --- Keys of all shared preferences --- */
        const val SP_NUMERAL_TYPE = "numeral_type"
        const val SP_NUMERAL_TYPE_DEF = "0"  // defaults to Arabic
        const val SP_SEARCH_INCLUSIVE = "search_inclusive"
        const val SP_DROPBOX_CREDENTIAL = "dropbox_credential"
    }
}

/* TODO:
  * A new icon
  * New screenshots at /screenshots/
*/
