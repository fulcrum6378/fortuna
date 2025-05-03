package ir.mahdiparastesh.fortuna

import ir.mahdiparastesh.fortuna.Vita.Companion.showScore
import ir.mahdiparastesh.fortuna.util.NumberUtils.toVariabilis
import ir.mahdiparastesh.fortuna.util.NumberUtils.write
import ir.mahdiparastesh.fortuna.util.NumberUtils.z
import ir.mahdiparastesh.fortuna.util.Numeral
import ir.mahdiparastesh.fortuna.util.RomanNumeral
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import javafx.scene.control.ComboBox
import javafx.scene.control.Dialog
import javafx.scene.control.DialogPane
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.control.TextFormatter
import javafx.scene.input.MouseEvent
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.CornerRadii
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.util.Callback
import javafx.util.converter.IntegerStringConverter
import java.time.temporal.ChronoField
import java.util.function.UnaryOperator

class Main : MainPage {
    override lateinit var c: Fortuna
    private lateinit var luna: Luna

    @FXML
    private lateinit var annusUp: Region

    @FXML
    private lateinit var prev: Region

    @FXML
    private lateinit var lunaBox: ComboBox<String>

    @FXML
    private lateinit var annus: TextField

    @FXML
    private lateinit var next: Region

    @FXML
    private lateinit var annusDown: Region

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
        updatePanel()
        updateGrid()
    }

    /* ---------------------------------PANEL---------------------------------- */

    private var rollingLuna = true  // "true" in order to trick the valueProperty listener

    private fun setupPanel() {

        // lunaBox
        lunaBox.items = FXCollections.observableArrayList<String>(
            listOf(
                "Farvardin", "Ordibehesht", "Khordad",
                "Tir", "Mordad", "Shahrivar",
                "Mehr", "Aban", "Azar",
                "Dey", "Bahman", "Esfand"
            )  // TODO localise
        )
        lunaBox.valueProperty().addListener {
            if (rollingLuna) {
                rollingLuna = false
                return@addListener; }

            c.luna = "${z(annus.text, 4)}.${z(lunaBox.selectionModel.selectedIndex + 1)}"
            c.date = c.lunaToDate(c.luna)
            updateGrid()
        }

        // annus
        annus.textProperty().addListener { observable, oldText, newText ->
            if (newText.length != 4) return@addListener
            c.luna = "${z(newText, 4)}.${z(c.date[ChronoField.MONTH_OF_YEAR])}"
            c.date = c.lunaToDate(c.luna)
            updateGrid()
        }
        annus.textFormatter = TextFormatter<Int?>(
            IntegerStringConverter(), 0, UnaryOperator { change: TextFormatter.Change? ->
                val newText = change!!.controlNewText
                if (newText.length <= 4 && newText.matches("\\d*".toRegex()))
                    return@UnaryOperator change
                null
            }
        )

        // calendar rollers
        prev.onMouseClicked = EventHandler<MouseEvent> { event -> moveInMonths(false) }
        next.onMouseClicked = EventHandler<MouseEvent> { event -> moveInMonths(true) }
        annusUp.onMouseClicked = EventHandler<MouseEvent> { event -> moveInYears(1) }
        annusDown.onMouseClicked = EventHandler<MouseEvent> { event -> moveInYears(-1) }
        // TODO create long click handlers
    }

    override fun updatePanel() {
        lunaBox.selectionModel.select(c.date[ChronoField.MONTH_OF_YEAR] - 1)
        annus.text = c.date[ChronoField.YEAR].toString()
    }

    override fun moveInYears(to: Int) {
        annus.text = (annus.text.toInt() + to).toString()
    }

    override fun onDateChanged() {
        rollingLuna = true
        super.onDateChanged()
    }

    /* ----------------------------------GRID---------------------------------- */

    private var numeral: Numeral? = null
    private var maximumStats: Int? = null
    private val gridMaxCols = 5

    private fun setupGrid() {
        (0 until gridMaxCols).forEach {
            grid.columnConstraints.add(ColumnConstraints().apply {
                percentWidth = 100.0 / gridMaxCols
                hgrow = Priority.ALWAYS
            })
        }
    }

    override fun updateGrid() {
        luna = c.vita[c.luna]
        numeral = RomanNumeral()  // TODO determine using settings
        maximumStats = c.maximaForStats(c.date, c.luna)

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
                Label(numeral.write(i + 1)),
                Label((if (isEstimated) "c. " else "") + score.showScore()),
            ).apply {
                AnchorPane.setTopAnchor(this, 0.0)
                AnchorPane.setRightAnchor(this, 0.0)
                AnchorPane.setBottomAnchor(this, 0.0)
                AnchorPane.setLeftAnchor(this, 0.0)
                alignment = Pos.CENTER
            }
        ).apply {
            val cssClass: String
            val bgColour: Color
            when {
                score != null && score > 0f -> {
                    cssClass = "pleasant"
                    bgColour = Color.rgb(76, 175, 80, (score / Vita.MAX_RANGE).toDouble())
                }

                score != null && score < 0f -> {
                    cssClass = "painful"
                    bgColour = Color.rgb(244, 67, 54, (-score / Vita.MAX_RANGE).toDouble())
                }

                else -> {
                    cssClass = "mediocre"
                    bgColour = Color.TRANSPARENT
                }
            }
            styleClass.add(cssClass)
            background = Background(BackgroundFill(bgColour, CornerRadii.EMPTY, Insets.EMPTY))

            // highlight the cell if it indicates today
            if (c.luna == c.todayLuna && c.todayDate[ChronoField.DAY_OF_MONTH] == i + 1)
                styleClass.add("today")

            // clicks
            onMouseClicked = EventHandler<MouseEvent> { event -> changeVar(i) }
        }
    }

    fun changeVar(i: Int) {
        val dialog = Dialog<Variabilis.Result>()
        dialog.title = "Variabilis"

        // load the layout and apply its CSS on it
        val fxmlLoader = FXMLLoader(Variabilis::class.java.getResource("variabilis.fxml"))
        val root = fxmlLoader.load<DialogPane>()
        root.stylesheets.add(
            Variabilis::class.java.getResource("variabilis.css")!!.toExternalForm()
        )

        // prepare the controller
        val variabilis = fxmlLoader.getController<Variabilis>()
        variabilis.prepare(
            (if (i != -1) luna[i]?.toVariabilis() else null) ?: luna.default?.toVariabilis() ?: 6,
            (if (i != -1) luna.emojis[i] else luna.emoji)?.toString() ?: "",
            (if (i != -1) luna.verba[i] else luna.verbum) ?: ""
        )

        // attach the layout to the dialog
        dialog.dialogPane = root
        dialog.headerText =  // set this after setting the DialogPane
            if (i != -1) "${c.luna}.${z(i + 1)}" else "DEFAULT"

        // display the dialog and handle its result
        dialog.resultConverter = Callback<ButtonType, Variabilis.Result> { buttonType ->
            if (buttonType.buttonData != ButtonBar.ButtonData.CANCEL_CLOSE)
                variabilis.result(buttonType.buttonData == ButtonBar.ButtonData.APPLY)
            else null
        }
        dialog.showAndWait().ifPresent { result ->
            if (result.saveOrDelete)
                saveDies(luna, i, result.score.toFloat(), result.emoji, result.verbum)
            else
                saveDies(luna, i, null, null, null)
        }
    }
}
