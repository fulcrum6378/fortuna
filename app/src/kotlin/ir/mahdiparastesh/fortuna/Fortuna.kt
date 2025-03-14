package ir.mahdiparastesh.fortuna

import android.app.Application
import android.content.SharedPreferences
import android.content.res.Configuration
import ir.mahdiparastesh.fortuna.sect.TodayWidget

class Fortuna : Application() {

    /** @return the main shared preferences instance; <code>settings.xml</code>. */
    val sp: SharedPreferences by lazy { getSharedPreferences("settings", MODE_PRIVATE) }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        TodayWidget.externalUpdate(this)
    }

    fun isLandscape() =
        resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}

/* TODO:
  * A new icon
  * Search count
  * Creation date for fictional characters
  * Estimated dates
  * -
  * JavaFX
*/