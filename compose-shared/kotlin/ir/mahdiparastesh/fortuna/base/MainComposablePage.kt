package ir.mahdiparastesh.fortuna.base

import androidx.compose.runtime.Composable
import ir.mahdiparastesh.fortuna.util.NumberUtils.toKey
import ir.mahdiparastesh.fortuna.util.Numeral
import java.time.temporal.ChronoField

interface MainComposablePage : MainPage {
    val m: FortunaStates

    override fun updatePanel() {
        m.panelSwitch = !m.panelSwitch
    }

    override fun updateGrid() {
        m.gridSwitch = !m.gridSwitch
    }

    fun setDate(field: ChronoField, value: Int) {
        c.date = c.date.with(field, value.toLong())
        onDateChanged()
    }

    override fun moveInYears(to: Int) {
        setDate(ChronoField.YEAR, c.date[ChronoField.YEAR] + to)
        onDateChanged()
    }

    override fun onDateChanged() {
        c.luna = c.date.toKey()
        m.date = c.date
    }


    @Composable
    fun isWideScreen(): Boolean

    @Composable
    fun str(ref: Any): String

    @Composable
    fun strArr(ref: Any): Array<String>

    var numeralType: String?

    fun buildNumeral(numeralType: String?): Numeral?
}
