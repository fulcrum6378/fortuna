package ir.mahdiparastesh.fortuna

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import ir.mahdiparastesh.fortuna.base.FortunaStates
import ir.mahdiparastesh.fortuna.base.MainComposablePage
import ir.mahdiparastesh.fortuna.util.Numeral
import ir.mahdiparastesh.fortuna.util.Numerals
import java.time.chrono.ChronoLocalDate

class Main : ComponentActivity(), MainComposablePage {
    override val c: Fortuna get() = applicationContext as Fortuna
    override val m: Model by viewModels()
    override val isAndroid: Boolean = true

    val night: Boolean by lazy {
        resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }
    override val cp: FloatArray by lazy { if (!night) cpl else createCPD() }
    override val cpl: FloatArray = createCPL()
    override val cs: FloatArray by lazy { if (!night) csl else createCSD() }
    override val csl: FloatArray = createCSL()

    class Model : ViewModel(), FortunaStates {
        override var date by mutableStateOf<ChronoLocalDate?>(
            null, structuralEqualityPolicy()
        )
        override var variabilis by mutableStateOf<Int?>(null)
        override var drawerState by mutableStateOf(false)
        override var panelSwitch by mutableStateOf(false)
        override var gridSwitch by mutableStateOf(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tp = android.graphics.Color.TRANSPARENT
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(tp, tp),
            navigationBarStyle = if (!night)
                SystemBarStyle.light(tp, tp)
            else
                SystemBarStyle.dark(tp)
        )
        if (m.date == null) m.date = c.date
        Theme.init(night)
        setContent { MainPage() }

        // runtime permission(s)
        val requiredPermissions =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                arrayOf(Manifest.permission.POST_NOTIFICATIONS)
            else
                arrayOf()
        // note: change reqPermLauncher to RequestMultiplePermissions() if you wanna add more.
        for (prm in requiredPermissions)
            if (checkSelfPermission(prm) != PackageManager.PERMISSION_GRANTED)
                reqPermLauncher.launch(prm)
    }


    @Composable
    override fun isWideScreen(): Boolean =
        LocalConfiguration.current.smallestScreenWidthDp >= 600

    @Composable
    override fun str(ref: Any): String =
        stringResource(ref as Int)

    @Composable
    override fun strArr(ref: Any): Array<String> =
        stringArrayResource(ref as Int)

    override var numeralType: String?
        get() = c.sp.getString(Fortuna.SP_NUMERAL_TYPE, null)
        set(newValue) {
            c.sp.edit {
                putString(
                    Fortuna.SP_NUMERAL_TYPE,
                    newValue ?: Fortuna.SP_NUMERAL_TYPE_DEF
                )
            }
            shake()
        }

    override fun buildNumeral(numeralType: String?): Numeral? =
        Numerals.build(numeralType?.let { if (it == Fortuna.SP_NUMERAL_TYPE_DEF) null else it })


    /** Requests all the required permissions. (currently only for notifications in Android 13+) */
    private val reqPermLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    /** Proper implementation of vibration in across different supported APIs */
    @Suppress("DEPRECATION")
    fun shake(dur: Long = 40L) {
        (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            (getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        else getSystemService(VIBRATOR_SERVICE) as Vibrator)
            .vibrate(VibrationEffect.createOneShot(dur, 100))
    }
}

@get:Composable
val c: Main get() = LocalActivity.current as Main
