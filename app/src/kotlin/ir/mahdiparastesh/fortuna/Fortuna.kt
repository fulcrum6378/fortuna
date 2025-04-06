package ir.mahdiparastesh.fortuna

import android.Manifest
import android.app.Application
import android.content.SharedPreferences
import android.content.res.Configuration
import android.icu.util.Calendar
import android.os.Build
import ir.mahdiparastesh.fortuna.sect.TodayWidget
import ir.mahdiparastesh.fortuna.util.HumanistIranianCalendar
import ir.mahdiparastesh.fortuna.util.Kit.create
import ir.mahdiparastesh.fortuna.util.Kit.lunaMaxima
import ir.mahdiparastesh.fortuna.util.Kit.resetHours
import ir.mahdiparastesh.fortuna.util.Kit.toKey
import java.io.File
import java.util.Locale

class Fortuna : Application(), FortunaContext<Calendar> {

    override lateinit var vita: Vita
    override val stored by lazy { File(filesDir, getString(R.string.export_file)) }
    override val backup by lazy { File(filesDir, getString(R.string.backup_file)) }
    override lateinit var calendar: Calendar
    override lateinit var luna: String
    override lateinit var todayCalendar: Calendar
    override lateinit var todayLuna: String


    /** @return the main shared preferences instance; <code>settings.xml</code>. */
    val sp: SharedPreferences by lazy { getSharedPreferences("settings", MODE_PRIVATE) }

    /**
     * Default Calendar Type
     * This is a very important constant containing the class type of our default calendar,
     * which must be a subclass of android.icu.util.Calendar.
     *
     * Do NOT use Lunisolar calendars here!
     *
     * @see android.icu.util.Calendar
     * @see <a href="https://en.wikipedia.org/wiki/Lunisolar_calendar">Lunisolar calendar - Wikipedia</a>
     */
    @Suppress("KotlinConstantConditions")
    val calType = when (BuildConfig.FLAVOR) {
        "iranian" -> HumanistIranianCalendar::class.java
        "gregorian" -> android.icu.util.GregorianCalendar::class.java
        else -> throw Exception("Unknown calendar type!")
    }

    /** Other supported Calendar types */
    val otherCalendars = arrayOf(
        HumanistIranianCalendar::class.java,
        // GregorianCalendar does not show a negative number in BCE, which is correct!
        android.icu.util.GregorianCalendar::class.java,
        android.icu.util.IndianCalendar::class.java,
        android.icu.util.ChineseCalendar::class.java,
        android.icu.util.IslamicCalendar::class.java,
        android.icu.util.HebrewCalendar::class.java,
        android.icu.util.CopticCalendar::class.java,
    ).filter { it != calType }

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

    /** A single calendar for all calculations to be performed on. */
    private val calendarForCalculation: Calendar = calType.create()


    override fun onCreate() {
        super.onCreate()

        // prepare the Vita
        calendar = calType.create()
        luna = calendar.toKey()
        vita = Vita(this)
        updateToday()
        if (luna !in vita) vita[todayLuna] =
            Luna(calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        TodayWidget.externalUpdate(this)
    }

    override fun updateToday() {
        todayCalendar = calType.create().resetHours()
        todayLuna = todayCalendar.toKey()
    }

    override fun getMonthLength(year: Int, month: Int): Int {
        calendarForCalculation.set(Calendar.YEAR, year)
        calendarForCalculation.set(Calendar.MONTH, month - 1)
        return calendarForCalculation.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    override fun maximaForStats(cal: Calendar, key: String): Int? =
        if (key == todayLuna)  // this month
            todayCalendar[Calendar.DAY_OF_MONTH]
        else if (cal.timeInMillis < todayCalendar.timeInMillis)  // past months
            cal.lunaMaxima()
        else  // future months
            null

    /** Converts a [Luna] key to a [Calendar] instance. */
    fun lunaToCalendar(luna: String): Calendar {
        val spl = luna.split(".")
        return calType.create().apply {
            this[Calendar.YEAR] = spl[0].toInt()
            this[Calendar.MONTH] = spl[1].toInt() - 1
            this[Calendar.DAY_OF_MONTH] = 1
        }
    }

    fun isLandscape() =
        resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}

/* TODO:
  * A new icon
  * New screenshots at /screenshots/
*/
