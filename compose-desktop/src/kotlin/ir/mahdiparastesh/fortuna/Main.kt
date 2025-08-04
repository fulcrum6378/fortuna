package ir.mahdiparastesh.fortuna

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ir.mahdiparastesh.chrono.IranianChronology
import ir.mahdiparastesh.fortuna.base.FortunaContext
import ir.mahdiparastesh.fortuna.base.FortunaStates
import ir.mahdiparastesh.fortuna.base.MainComposablePage
import ir.mahdiparastesh.fortuna.util.Numeral
import org.jetbrains.compose.resources.StringArrayResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import java.io.File
import java.time.chrono.ChronoLocalDate
import java.time.chrono.Chronology

object Context : FortunaContext, MainComposablePage {

    private val osName: String = System.getProperty("os.name")
    val appDir: File = File(
        System.getProperty("user.home") + when {
            osName.startsWith("Windows") -> "\\AppData\\Roaming\\Fortuna"
            osName.startsWith("Mac OS") -> "/Library/Application Support/Fortuna"
            else -> "/.config/Fortuna"  // "Linux", "FreeBSD", "SunOS"
        }
    )

    override lateinit var vita: Vita
    override val stored: File = File(appDir, "fortuna.vita")
    override val backup: File = File(appDir, "fortuna_backup.vita")
    override lateinit var date: ChronoLocalDate
    override lateinit var luna: String
    override lateinit var todayDate: ChronoLocalDate
    override lateinit var todayLuna: String

    override val chronology: Chronology =
        IranianChronology.INSTANCE

    override fun onCreate() {
        if (!appDir.exists()) appDir.mkdirs()
        if (m.date == null) {
            super.onCreate()
            m.date = c.date
        }
    }


    override val c: FortunaContext get() = this
    override val m: FortunaStates get() = Model

    object Model : FortunaStates {
        override var date by mutableStateOf<ChronoLocalDate?>(
            null, structuralEqualityPolicy()
        )
        override var variabilis by mutableStateOf<Int?>(null)
        override var drawerState by mutableStateOf(false)
        override var panelSwitch by mutableStateOf(false)
        override var gridSwitch by mutableStateOf(false)
    }


    var night = false
    override val cp: FloatArray by lazy { if (!night) cpl else createCPD() }
    override val cpl: FloatArray = createCPL()
    override val cs: FloatArray by lazy { if (!night) csl else createCSD() }
    override val csl: FloatArray = createCSL()


    @Composable
    override fun isWideScreen() = true

    @Composable
    override fun str(ref: Any): String =
        stringResource(ref as StringResource)

    @Composable
    override fun strArr(ref: Any): Array<String> =
        stringArrayResource(ref as StringArrayResource).toTypedArray()

    override var numeralType: String? = null  // FIXME

    override fun buildNumeral(numeralType: String?): Numeral? = null  // FIXME
}

@get:Composable
val c get() = Context

fun main() = application {
    c.onCreate()

    Window(
        onCloseRequest = ::exitApplication,
        title = c.str(R.string.app_name),
        //icon = TODO Painter,
        //undecorated = true,
    ) {
        c.night = isSystemInDarkTheme()
        Theme.init(c.night)

        Box(Modifier.background(Theme.palette.window)) {
            MainPage()
        }
    }
}
