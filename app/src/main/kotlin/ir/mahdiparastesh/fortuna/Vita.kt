package ir.mahdiparastesh.fortuna

import android.content.Context
import android.icu.util.Calendar
import java.io.*

class Vita : HashMap<String, Luna>() {

    fun find(key: String): Luna? = getOrElse(key) { null }

    fun save(c: Context) {
        save(c, this)
    }

    fun export(c: Context): ByteArray {
        reform(c)
        return dump().encodeToByteArray()
    }

    fun dump(): String = StringBuilder().apply {
        this@Vita.toSortedMap().forEach { (k, luna) ->
            append("@$k")
            luna.default?.also { score ->
                append("~$score")
                if (luna.emoji?.isNotBlank() == true)
                    append(";${luna.emoji!!}")
                else if (luna.verbum?.isNotBlank() == true)
                    append(";")
                if (luna.verbum?.isNotBlank() == true)
                    append(";${luna.verbum!!.saveVitaText()}")
            }
            append("\n")
            var skipped = false
            for (d in luna.diebus.indices) {
                if (luna.diebus[d] == null) {
                    skipped = true
                    continue; }
                if (skipped) {
                    append("${d + 1}:")
                    skipped = false
                }
                append(luna.diebus[d])
                if (luna.emojis[d]?.isNotBlank() == true)
                    append(";${luna.emojis[d]!!}")
                else if (luna.verba[d]?.isNotBlank() == true)
                    append(";")
                if (luna.verba[d]?.isNotBlank() == true)
                    append(";${luna.verba[d]!!.saveVitaText()}")
                append("\n")
            }
            append("\n")
        }
    }.toString()

    fun reform(c: Context) {
        val removal = arrayListOf<String>()
        forEach { key, luna -> if (luna.isEmpty()) removal.add(key) }
        removal.forEach { remove(it) }
        save(c)
    }

    companion object {
        const val MAX_RANGE = 3f
        const val MIME_TYPE = "application/octet-stream"

        fun load(c: Context): Vita {
            val stored = Stored(c)
            return if (stored.exists()) {
                val data: ByteArray
                FileInputStream(stored).use { data = it.readBytes() }
                loads(String(data))
            } else Vita()
        }

        fun loads(text: String): Vita {
            val vita = Vita()
            val cal = Main.calType.newInstance()
            var key: String? = null
            var dies = 0
            for (ln in StringReader(text).readLines()) try {
                if (key == null) {
                    if (!ln.startsWith('@')) continue
                    val sn = ln.split(";", limit = 3)
                    val s = sn[0].split("~")
                    key = s[0].substring(1)
                    val splitKey = key.split(".")
                    cal.set(Calendar.YEAR, splitKey[0].toInt())
                    cal.set(Calendar.MONTH, splitKey[1].toInt() - 1)
                    vita[key] = Luna(
                        cal,
                        s.getOrNull(1)?.toFloat(),
                        sn.getOrNull(1)?.ifBlank { null },
                        sn.getOrNull(2)?.loadVitaText()
                    )
                    dies = 0
                } else {
                    if (ln.isEmpty()) {
                        key = null
                        continue; }
                    val sn = ln.split(";", limit = 3)
                    val s = sn[0].split(":")
                    if (s.size == 2) dies = s[0].toInt() - 1
                    vita[key]!!.diebus[dies] = (s.getOrNull(1) ?: s[0]).toFloat()
                    vita[key]!!.emojis[dies] = sn.getOrNull(1)?.ifBlank { null }
                    vita[key]!!.verba[dies] = sn.getOrNull(2)?.loadVitaText()
                    dies++
                }
            } catch (e: Exception) {
                throw IOException(
                    "Luna $key Dies $dies threw ${e.javaClass.simpleName}\n${e.stackTraceToString()}"
                )
            }
            return vita
        }

        private fun String.loadVitaText() = replace("\\n", "\n")
        fun String.saveVitaText() = replace("\n", "\\n")

        fun save(c: Context, vita: Vita) {
            FileOutputStream(Stored(c)).use { fos ->
                fos.write(vita.dump().encodeToByteArray())
            }
        }

        fun Calendar.toKey(): String =
            "${z(this[Calendar.YEAR], 4)}.${z(this[Calendar.MONTH] + 1)}"

        fun <CAL> String.toCalendar(klass: Class<CAL>): CAL where CAL : Calendar {
            val spl = split(".")
            return klass.newInstance().apply {
                this[Calendar.YEAR] = spl[0].toInt()
                this[Calendar.MONTH] = spl[1].toInt() - 1
                this[Calendar.DAY_OF_MONTH] = 1
            }
        }

        fun Float?.showScore(): String = if (this != 0f) (this?.toString() ?: "_") else "0"

        fun z(n: Any?, ideal: Int = 2): String {
            var s = n.toString()
            while (s.length < ideal) s = "0$s"
            return s
        }

        fun Calendar.lunaMaxima() = getActualMaximum(Calendar.DAY_OF_MONTH)

        fun Luna.saveDies(c: Main, i: Int, score: Float?, emoji: String?, verbum: String?) {
            if (i != -1) {
                diebus[i] = score
                verba[i] = verbum?.ifBlank { null }
                emojis[i] = emoji?.ifBlank { null }
            } else {
                default = score
                this.verbum = verbum?.ifBlank { null }
                this.emoji = emoji?.ifBlank { null }
            }
            c.m.vita!![c.m.luna!!] = this
            c.m.vita!!.save(c.c)
            c.updateGrid()
        }

        fun Luna.mean(maxDays: Int): Float {
            val scores = arrayListOf<Float>()
            for (v in 0 until maxDays) (this[v] ?: this.default)?.let { scores.add(it) }
            return if (scores.isEmpty()) 0f else scores.sum() / scores.size
        }
    }

    class Stored(c: Context) : File(c.filesDir, c.getString(R.string.export_file))
}

class Luna(
    cal: Calendar = Main.calType.newInstance(),
    var default: Float? = null,
    var emoji: String? = null,
    var verbum: String? = null,
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
}
