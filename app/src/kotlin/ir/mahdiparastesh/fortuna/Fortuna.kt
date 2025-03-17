package ir.mahdiparastesh.fortuna

import android.Manifest
import android.app.Application
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import ir.mahdiparastesh.fortuna.sect.TodayWidget
import ir.mahdiparastesh.fortuna.util.HumanistIranianCalendar
import java.util.Locale

class Fortuna : Application() {

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

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        TodayWidget.externalUpdate(this)
    }

    fun isLandscape() =
        resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}

/* TODO:
  * A new icon
*/