package ir.mahdiparastesh.fortuna

import android.app.Application
import android.content.res.Configuration
import ir.mahdiparastesh.fortuna.misc.TodayWidget

class Fortuna : Application() {
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        TodayWidget.externalUpdate(applicationContext)
    }
}
