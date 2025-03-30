package ir.mahdiparastesh.fortuna.util

import ir.mahdiparastesh.fortuna.time.PersianDate
import ir.mahdiparastesh.fortuna.util.NumberUtils.z

object Kit {

    fun PersianDate.toKey(): String = "${z(year, 4)}.${z(month + 1)}"
}