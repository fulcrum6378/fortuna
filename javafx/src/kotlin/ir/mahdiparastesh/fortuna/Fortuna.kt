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
import java.time.chrono.HijrahChronology
import java.time.chrono.IsoChronology
import java.time.chrono.JapaneseChronology

class Fortuna : Application(), FortunaContext {

    override lateinit var vita: Vita
    override val stored: File by lazy { File("fortuna.vita") }
    override val backup: File by lazy { File("fortuna_backup.vita") }
    override lateinit var date: ChronoLocalDate
    override lateinit var luna: String
    override lateinit var todayDate: ChronoLocalDate
    override lateinit var todayLuna: String

    override val chronology: Chronology =
        IranianChronology.INSTANCE

    override val otherChronologies: List<Chronology> by lazy {
        arrayOf(
            IranianChronology.INSTANCE,
            IsoChronology.INSTANCE,
            HijrahChronology.INSTANCE,
            JapaneseChronology.INSTANCE,
        ).filter { it != chronology }
    }


    override fun start(stage: Stage) {
        val fxmlLoader = FXMLLoader(Fortuna::class.java.getResource("main.fxml"))
        val root = fxmlLoader.load<Parent>()

        // prepare the Vita
        onCreate()

        // prepare the controller
        val main = fxmlLoader.getController<Main>()
        main.prepare(this)

        // setup the scene
        val scene = Scene(root, 800.0, 600.0)
        scene.stylesheets.add(Fortuna::class.java.getResource("main.css")!!.toExternalForm())
        stage.title = "Fortuna"
        stage.scene = scene
        stage.show()
    }
}

fun main() {
    Application.launch(Fortuna::class.java)
}
