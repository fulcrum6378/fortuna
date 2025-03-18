package ir.mahdiparastesh.fortuna

import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox

class Main {
    val maxCols = 5

    @FXML
    private lateinit var grid: GridPane

    /*@FXML
    private fun onHelloButtonClick() {
        welcomeText.text = "Welcome to JavaFX Application!"
    }*/

    @Suppress("unused")
    fun initialize() {
        setupGrid()
    }

    fun setupGrid() {
        (0 until maxCols).forEach {
            val columnConstraints = ColumnConstraints()
            columnConstraints.percentWidth = 100.0 / maxCols
            columnConstraints.hgrow = Priority.ALWAYS
            grid.columnConstraints.add(columnConstraints)
        }
        populateGrid()
    }

    fun populateGrid() {
        grid.children.clear()
        for (i in 0..(30 - 1)) grid.add(
            createGridItem(i),
            i % maxCols,
            i / maxCols
        )
    }

    fun createGridItem(i: Int): AnchorPane =
        AnchorPane(
            Label((i + 1).toString())
        ).apply {
            styleClass.add("grid-item")
        }
}