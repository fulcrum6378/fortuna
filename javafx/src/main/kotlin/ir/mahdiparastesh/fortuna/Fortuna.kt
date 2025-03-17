package ir.mahdiparastesh.fortuna

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.stage.Stage

class Fortuna : Application() {
    override fun start(stage: Stage) {
        val fxmlLoader = FXMLLoader(Fortuna::class.java.getResource("main.fxml"))
        val scene = Scene(fxmlLoader.load(), 320.0, 240.0)
        stage.title = "Hello!"
        stage.scene = scene
        stage.show()
    }
}

fun main() {
    Application.launch(Fortuna::class.java)
}