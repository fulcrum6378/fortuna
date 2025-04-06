package ir.mahdiparastesh.fortuna.util

import ir.mahdiparastesh.fortuna.time.PersianDate
import ir.mahdiparastesh.fortuna.util.NumberUtils.z

object Kit {

    fun PersianDate.toKey(): String = "${z(year + 5000, 4)}.${z(monthValue)}"
}
