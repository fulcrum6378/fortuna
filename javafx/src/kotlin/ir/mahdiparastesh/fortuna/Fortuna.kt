package ir.mahdiparastesh.fortuna

import ir.mahdiparastesh.fortuna.time.PersianDate
import ir.mahdiparastesh.fortuna.util.Kit.toKey
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import java.io.File

class Fortuna : Application(), FortunaContext<PersianDate> {

    override var vita: Vita? = null
    override val stored: File by lazy { File("fortuna.vita") }
    override val backup: File by lazy { File("fortuna_backup.vita") }
    override var calendar: PersianDate = PersianDate.now()
    override var luna: String? = null
    override var todayCalendar: PersianDate = PersianDate.now()
    override var todayLuna: String = todayCalendar.toKey()

    override fun getMonthLength(year: Int, month: Int): Int =
        PersianDate.of(year, month, 1).lengthOfMonth()

    override fun start(stage: Stage) {
        val fxmlLoader = FXMLLoader(Fortuna::class.java.getResource("main.fxml"))
        val root = fxmlLoader.load<Parent>()

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
