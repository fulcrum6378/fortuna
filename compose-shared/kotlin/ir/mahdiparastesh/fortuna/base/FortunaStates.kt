package ir.mahdiparastesh.fortuna.base

import java.time.chrono.ChronoLocalDate

interface FortunaStates {
    var date: ChronoLocalDate?
    var variabilis: Int?
    var drawerState: Boolean
    var panelSwitch: Boolean
    var gridSwitch: Boolean
}
