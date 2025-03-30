package ir.mahdiparastesh.fortuna

import android.Manifest
import android.app.Application
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import ir.mahdiparastesh.fortuna.sect.TodayWidget
import ir.mahdiparastesh.fortuna.time.PersianDate
import ir.mahdiparastesh.fortuna.time.PersianDateTime
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.chrono.ChronoLocalDate
import java.util.Locale

class Fortuna : Application(), FortunaContext {

    override lateinit var date: ChronoLocalDate
    override lateinit var todayDate: ChronoLocalDate
    override lateinit var todayLuna: String
    override var vita: Vita? = null
    override var luna: String? = null

    override val stored by lazy { File(filesDir, getString(R.string.export_file)) }
    override val backup by lazy { File(filesDir, getString(R.string.backup_file)) }

    @Suppress("KotlinConstantConditions")
    override val calType = when (BuildConfig.FLAVOR) {
        "iranian" -> Pair(PersianDate::class.java, PersianDateTime::class.java)
        "gregorian" -> Pair(LocalDate::class.java, LocalDateTime::class.java)
        else -> throw Exception("Unknown calendar type!")
    }

    override val otherCalendars = arrayOf(
        PersianDate::class.java,
        // Todo GregorianCalendar does not show a negative number in BCE, which is correct!
        LocalDate::class.java,
        /*android.icu.util.IndianCalendar::class.java,
        android.icu.util.ChineseCalendar::class.java,
        android.icu.util.IslamicCalendar::class.java,
        android.icu.util.HebrewCalendar::class.java,
        android.icu.util.CopticCalendar::class.java,*/
    ).filter { it != calType.first }


    /** @return the main shared preferences instance; <code>settings.xml</code>. */
    val sp: SharedPreferences by lazy { getSharedPreferences("settings", MODE_PRIVATE) }

    val locale: Locale = Locale.UK // never ever use SimpleDateFormat

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
        vita = Vita(this)
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
  * Trim memory
*/
