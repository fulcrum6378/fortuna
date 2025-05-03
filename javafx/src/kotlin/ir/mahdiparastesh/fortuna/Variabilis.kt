package ir.mahdiparastesh.fortuna

import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control.ComboBox

class Variabilis {

    @FXML
    private lateinit var varPicker: ComboBox<String>

    @Suppress("unused")
    fun initialize() {
        varPicker.items = FXCollections.observableArrayList<String>(
            listOf(
                "-3.0", "-2.5", "-2.0", "-1.5", "-1.0", "-0.5",
                "0",
                "0.5", "1.0", "1.5", "2.0", "2.5", "3.0"
            )
        )
        varPicker.selectionModel.select(6)
    }
}
