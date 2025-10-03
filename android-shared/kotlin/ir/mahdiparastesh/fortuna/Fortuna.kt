package ir.mahdiparastesh.fortuna

import android.app.Application
import android.content.SharedPreferences
import android.content.res.Configuration
import ir.mahdiparastesh.chrono.IranianChronology
import ir.mahdiparastesh.fortuna.base.FortunaContext
import ir.mahdiparastesh.fortuna.sect.TodayWidget
import java.io.File
import java.time.chrono.ChronoLocalDate
import java.time.chrono.Chronology
import java.time.chrono.IsoChronology

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

    /** Main settings of this application (<code>settings.xml</code>) */
    val sp: SharedPreferences by lazy { getSharedPreferences("settings", MODE_PRIVATE) }


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

        /* ----- keys of all shared preferences ----- */
        const val SP_NUMERAL_TYPE = "numeral_type"
        const val SP_NUMERAL_TYPE_DEF = "0"  // defaults to Arabic
        const val SP_VARIABILIS_LUNA = "variabilis_luna"
        const val SP_VARIABILIS_DIES = "variabilis_dies"
        const val SP_VARIABILIS_SCORE = "variabilis_score"
        const val SP_VARIABILIS_EMOJI = "variabilis_emoji"
        const val SP_VARIABILIS_VERBUM = "variabilis_verbum"
        const val SP_SEARCH_INCLUSIVE = "search_inclusive"
        const val SP_DROPBOX_CREDENTIAL = "dropbox_credential"
    }
}
