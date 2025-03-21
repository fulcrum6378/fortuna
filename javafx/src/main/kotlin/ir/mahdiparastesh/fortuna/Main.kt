package ir.mahdiparastesh.fortuna

import javafx.fxml.FXML
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority

class Main {
    private val gridMaxCols = 5

    @FXML
    private lateinit var grid: GridPane

    @Suppress("unused")
    fun initialize() {
        setupGrid()
    }

    private fun setupGrid() {
        (0 until gridMaxCols).forEach {
            val columnConstraints = ColumnConstraints()
            columnConstraints.percentWidth = 100.0 / gridMaxCols
            columnConstraints.hgrow = Priority.ALWAYS
            grid.columnConstraints.add(columnConstraints)
        }
        populateGrid()
    }

    private fun populateGrid() {
        grid.children.clear()
        for (i in 0..(30 - 1)) grid.add(
            createGridItem(i),
            i % gridMaxCols,
            i / gridMaxCols
        )
    }

    private fun createGridItem(i: Int): AnchorPane =
        AnchorPane(
            Label((i + 1).toString()).apply {
                alignment = Pos.CENTER
                AnchorPane.setTopAnchor(this, 0.0)
                AnchorPane.setRightAnchor(this, 0.0)
                AnchorPane.setBottomAnchor(this, 0.0)
                AnchorPane.setLeftAnchor(this, 0.0)
            }
        ).apply {
            styleClass.add(listOf("pleasant", "mediocre", "painful").random())
        }
}
