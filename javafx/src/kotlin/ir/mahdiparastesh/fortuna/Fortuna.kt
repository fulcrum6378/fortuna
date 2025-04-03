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

    override lateinit var vita: Vita
    override val stored: File by lazy { File("fortuna.vita") }
    override val backup: File by lazy { File("fortuna_backup.vita") }
    override lateinit var calendar: PersianDate
    override lateinit var luna: String
    override lateinit var todayCalendar: PersianDate
    override lateinit var todayLuna: String

    override fun start(stage: Stage) {
        val fxmlLoader = FXMLLoader(Fortuna::class.java.getResource("main.fxml"))
        val root = fxmlLoader.load<Parent>()

        // prepare the Vita
        calendar = PersianDate.now()
        luna = calendar.toKey()
        vita = Vita(this)
        updateToday()
        if (luna !in vita) vita[todayLuna] = Luna(calendar.lengthOfMonth())
        vita.save()

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

    override fun updateToday() {
        todayCalendar = PersianDate.now()
        todayLuna = todayCalendar.toKey()
    }

    override fun getMonthLength(year: Int, month: Int): Int =
        PersianDate.of(year - 5000, month, 1).lengthOfMonth()

    override fun maximaForStats(cal: PersianDate, key: String): Int? =
        if (cal == todayCalendar) // this month
            todayCalendar.dayOfMonth
        else if (cal.isBefore(todayCalendar)) // past months
            cal.lengthOfMonth()
        else // future months
            null
}

fun main() {
    Application.launch(Fortuna::class.java)
}
