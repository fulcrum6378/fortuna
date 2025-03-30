package ir.mahdiparastesh.fortuna

import ir.mahdiparastesh.fortuna.time.PersianDate
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import java.time.LocalDate
import java.time.chrono.ChronoLocalDate

class Fortuna : Application(), FortunaContext<ChronoLocalDate> {

    override var todayCalendar: ChronoLocalDate = PersianDate.now()
    //override var todayLuna: String =
    override var calendar: ChronoLocalDate = todayCalendar

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
