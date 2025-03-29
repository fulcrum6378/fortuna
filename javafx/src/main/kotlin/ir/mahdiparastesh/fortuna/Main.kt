package ir.mahdiparastesh.fortuna

import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.geometry.Pos
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority

class Main {
    private lateinit var c: Fortuna

    private val gridMaxCols = 5

    @FXML
    private lateinit var luna: ComboBox<String>

    @FXML
    private lateinit var annus: TextField

    @FXML
    private lateinit var grid: GridPane

    /** Called when FXMLLoader.load() is invoked. */
    @Suppress("unused")
    fun initialize() {
    }

    fun prepare(c: Fortuna) {
        this.c = c
        setupPanel()
        setupGrid()
    }

    private fun setupPanel() {
        luna.items = FXCollections.observableArrayList<String>(
            listOf(
                "Farvardin", "Ordibehesht", "Khordad",
                "Tir", "Mordad", "Shahrivar",
                "Mehr", "Aban", "Azar",
                "Dey", "Bahman", "Esfand"
            )
        )
        luna.selectionModel.select(c.today.monthValue - 1)
        annus.text = c.today.year.toString()
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
