package ir.mahdiparastesh.fortuna.base

import androidx.compose.material3.DrawerState
import java.time.chrono.ChronoLocalDate

interface FortunaStates {
    var date: ChronoLocalDate?
    var variabilis: Int?
    val drawerState: DrawerState
    var panelSwitch: Boolean
    var gridSwitch: Boolean
}
