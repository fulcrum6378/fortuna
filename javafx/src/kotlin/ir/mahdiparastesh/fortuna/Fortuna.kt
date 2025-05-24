package ir.mahdiparastesh.fortuna

import ir.mahdiparastesh.chrono.IranianChronology
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import java.io.File
import java.time.chrono.ChronoLocalDate
import java.time.chrono.Chronology

class Fortuna : Application(), FortunaContext {

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


    override fun start(stage: Stage) {
        val fxmlLoader = FXMLLoader(Main::class.java.getResource("main.fxml"))
        val root = fxmlLoader.load<Parent>()

        // prepare the Vita
        if (!appDir.exists()) appDir.mkdirs()
        onCreate()

        // prepare the controller
        val main = fxmlLoader.getController<Main>()
        main.prepare(this)

        // setup the scene
        val scene = Scene(root, 800.0, 600.0)
        scene.stylesheets.add(Main::class.java.getResource("main.css")!!.toExternalForm())
        stage.title = "Fortuna"
        stage.scene = scene
        stage.show()
    }
}

fun main() {
    Application.launch(Fortuna::class.java)
}
