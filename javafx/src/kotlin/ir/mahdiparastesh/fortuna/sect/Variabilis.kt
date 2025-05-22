package ir.mahdiparastesh.fortuna.sect

import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control.ComboBox
import javafx.scene.control.TextArea
import javafx.scene.control.TextField

class Variabilis {

    @FXML
    private lateinit var varPicker: ComboBox<String>

    @FXML
    private lateinit var emoji: TextField

    @FXML
    private lateinit var verbum: TextArea

    @Suppress("unused")
    fun initialize() {
        varPicker.items = FXCollections.observableArrayList<String>(
            listOf(
                "3.0", "2.5", "2.0", "1.5", "1.0", "0.5",
                "0",
                "-0.5", "-1.0", "-1.5", "-2.0", "-2.5", "-3.0"
            )
        )
    }

    fun prepare(scorePos: Int, emoji: String, verbum: String) {
        varPicker.selectionModel.select(scorePos)
        this.emoji.text = emoji
        this.verbum.text = verbum
    }

    fun result(saveOrDelete: Boolean): Result = Result(
        saveOrDelete,
        varPicker.selectionModel.selectedItem,
        emoji.text,
        verbum.text
    )

    data class Result(
        val saveOrDelete: Boolean,
        val score: String,
        val emoji: String,
        val verbum: String,
    )
}