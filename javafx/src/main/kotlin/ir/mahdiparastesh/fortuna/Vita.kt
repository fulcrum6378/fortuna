package ir.mahdiparastesh.fortuna

/*class Vita : HashMap<String, Luna>() {
}*/

/** Subset of [Vita] for managing months. */
/*class Luna(
    c: Fortuna,
    cal: Calendar = c.calType.create(),
    var default: Float? = null,
    var emoji: String? = null,
    var verbum: String? = null,
    var size: Long = 0L,
) {
    val diebus: Array<Float?>
    val emojis: Array<String?>
    val verba: Array<String?>

    init {
        val maxDies = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        diebus = Array(maxDies) { null }
        verba = Array(maxDies) { null }
        emojis = Array(maxDies) { null }
    }

    operator fun get(index: Int): Float? = diebus[index]
    operator fun set(index: Int, value: Float?) {
        diebus[index] = value
    }

    fun isEmpty() = diebus.all { it == null } && default == null
}*/
