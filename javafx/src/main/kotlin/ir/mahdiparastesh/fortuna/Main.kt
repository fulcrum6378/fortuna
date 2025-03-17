package ir.mahdiparastesh.fortuna

import javafx.fxml.FXML
import javafx.scene.control.Label

class Main {
    @FXML
    private lateinit var welcomeText: Label

    @FXML
    private fun onHelloButtonClick() {
        welcomeText.text = "Welcome to JavaFX Application!"
    }
}