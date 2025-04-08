package ir.mahdiparastesh.fortuna

import ir.mahdiparastesh.fortuna.Vita.Companion.showScore
import ir.mahdiparastesh.fortuna.util.Numeral
import ir.mahdiparastesh.fortuna.util.RomanNumeral
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
import javafx.scene.layout.VBox
import java.time.temporal.ChronoField

class Main {
    private lateinit var c: Fortuna
    private lateinit var luna: Luna
    private var numeral: Numeral = RomanNumeral()
    private var maximumStats: Int? = null
    private val gridMaxCols = 5

    @FXML
    private lateinit var lunaBox: ComboBox<String>

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
        update()
    }

    fun update() {
        luna = c.vita[c.luna]
        updatePanel()
        updateGrid()
    }

    private fun setupPanel() {
        lunaBox.items = FXCollections.observableArrayList<String>(
            listOf(
                "Farvardin", "Ordibehesht", "Khordad",
                "Tir", "Mordad", "Shahrivar",
                "Mehr", "Aban", "Azar",
                "Dey", "Bahman", "Esfand"
            )
        )
    }

    private fun updatePanel() {
        lunaBox.selectionModel.select(c.date[ChronoField.MONTH_OF_YEAR] - 1)
        annus.text = c.date[ChronoField.YEAR].toString()
    }

    private fun setupGrid() {
        (0 until gridMaxCols).forEach {
            val columnConstraints = ColumnConstraints()
            columnConstraints.percentWidth = 100.0 / gridMaxCols
            columnConstraints.hgrow = Priority.ALWAYS
            grid.columnConstraints.add(columnConstraints)
        }
    }

    private fun updateGrid() {
        grid.children.clear()
        for (i in 0..(c.date.lengthOfMonth() - 1)) grid.add(
            createGridItem(i),
            i % gridMaxCols,
            i / gridMaxCols
        )
    }

    private fun createGridItem(i: Int): AnchorPane {
        val score: Float? =
            if (i < (maximumStats ?: 0)) luna[i] ?: luna.default else null
        val isEstimated = i < (maximumStats ?: 0) && luna[i] == null && luna.default != null

        return AnchorPane(
            VBox(
                Label(numeral.output(i + 1)),
                Label((if (isEstimated) "c. " else "") + score.showScore()),
            ).apply {
                AnchorPane.setTopAnchor(this, 0.0)
                AnchorPane.setRightAnchor(this, 0.0)
                AnchorPane.setBottomAnchor(this, 0.0)
                AnchorPane.setLeftAnchor(this, 0.0)
                alignment = Pos.CENTER
            }
        ).apply {
            styleClass.add(
                when {
                    score != null && score > 0f -> "pleasant"
                    score != null && score < 0f -> "painful"
                    else -> "mediocre"
                }
            )
        }
    }
}
