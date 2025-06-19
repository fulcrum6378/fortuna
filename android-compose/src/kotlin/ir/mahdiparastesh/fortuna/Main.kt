package ir.mahdiparastesh.fortuna

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.lifecycle.ViewModel
import ir.mahdiparastesh.fortuna.base.MainPage
import ir.mahdiparastesh.fortuna.util.NumberUtils.toKey
import java.time.chrono.ChronoLocalDate
import java.time.temporal.ChronoField

class Main : ComponentActivity(), MainPage {
    override val c: Fortuna get() = applicationContext as Fortuna
    val m: Model by viewModels()

    val night: Boolean by lazy {
        resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }
    val cpl: FloatArray = floatArrayOf(0.296875f, 0.68359375f, 0.3125f)  // #4CAF50
    val cp: FloatArray by lazy {
        if (!night) cpl else floatArrayOf(0.01171875f, 0.296875f, 0.0234375f)  // #034C06
    }
    val csl: FloatArray = floatArrayOf(0.953125f, 0.26171875f, 0.2109375f)  // #F44336
    val cs: FloatArray by lazy {
        if (!night) csl else floatArrayOf(0.40234375f, 0.05078125f, 0.0234375f)  // #670D06
    }

    class Model : ViewModel() {
        var date by mutableStateOf<ChronoLocalDate?>(null, structuralEqualityPolicy())
        var variabilis by mutableStateOf<Int?>(null)
        val drawerState = DrawerState(DrawerValue.Closed)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tp = android.graphics.Color.TRANSPARENT
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(tp, tp),
            navigationBarStyle =
                if (!night) SystemBarStyle.light(tp, tp) else SystemBarStyle.dark(tp)
        )
        if (m.date == null) m.date = c.date
        setContent { FortunaTheme { MainRoot() } }
    }

    override fun updatePanel() {}
    override fun updateGrid() {}

    fun setDate(field: ChronoField, value: Int) {
        c.date = c.date.with(field, value.toLong())
        onDateChanged()
    }

    override fun moveInYears(to: Int) {
        setDate(ChronoField.YEAR, c.date[ChronoField.YEAR] + to)
        onDateChanged()
    }

    override fun onDateChanged() {
        c.luna = c.date.toKey()
        m.date = c.date
    }

    /** Proper implementation of vibration in across different supported APIs */
    @Suppress("DEPRECATION")
    fun shake(dur: Long = 40L) {
        (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            (getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        else getSystemService(VIBRATOR_SERVICE) as Vibrator)
            .vibrate(VibrationEffect.createOneShot(dur, 100))
    }
}
