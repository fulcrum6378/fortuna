package ir.mahdiparastesh.fortuna

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.StringReader

/**
 * Representation of the VITA file type as [HashMap]<String, Luna>
 *
 * We need the whole Vita loaded on startup for search and statistics;
 * so we put the whole data in a single file.
 */
class Vita(private val c: FortunaContext<*>, text: String) : HashMap<String, Luna>() {

    constructor(c: FortunaContext<*>) : this(c, c.stored)

    constructor(c: FortunaContext<*>, file: File) :
            this(c, String(FileInputStream(file).use { it.readBytes() }))

    init {
        load(text)
    }

    /** Loads Vita data from a given string. */
    private fun load(text: String) {
        var key: String? = null
        var dies = 0
        for (ln in StringReader(text).readLines()) try {
            if (key == null) {
                if (!ln.startsWith('@')) continue
                val sn = ln.split(";", limit = 3)
                val s = sn[0].split("~")
                key = s[0].substring(1)
                val splitKey = key.split(".")
                this[key] = Luna(
                    c.getMonthLength(splitKey[0].toInt(), splitKey[1].toInt()),
                    s.getOrNull(1)?.toFloat(),
                    sn.getOrNull(1)?.ifBlank { null },
                    sn.getOrNull(2)?.loadVitaText(),
                    ln.length + 1L
                )
                dies = 0
            } else {
                if (ln.isEmpty()) {
                    key = null
                    continue; }
                val sn = ln.split(";", limit = 3)
                val s = sn[0].split(":")
                if (s.size == 2) dies = s[0].toInt() - 1
                this[key]!!.diebus[dies] = (s.getOrNull(1) ?: s[0]).toFloat()
                this[key]!!.emojis[dies] = sn.getOrNull(1)?.ifBlank { null }
                this[key]!!.verba[dies] = sn.getOrNull(2)?.loadVitaText()
                this[key]!!.size += ln.length + 1L
                dies++
            }
        } catch (e: Exception) {
            throw IOException(
                "Luna $key Dies $dies threw ${e.javaClass.simpleName}\n${e.stackTraceToString()}"
            )
        }
    }

    /** Dumps Vita data into a string to be written in a *.vita file. */
    private fun dump(): String = StringBuilder().apply {
        var hasVerbum = false
        this@Vita.toSortedMap().forEach { (k, luna) ->
            append("@$k")
            luna.default?.also { score ->
                append("~${score.writeScore()}")
                hasVerbum = luna.verbum?.isNotBlank() == true
                if (luna.emoji?.isNotBlank() == true)
                    append(";${luna.emoji!!}")
                else if (hasVerbum) append(";")
                if (hasVerbum)
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
                append(luna.diebus[d]!!.writeScore())
                hasVerbum = luna.verba[d]?.isNotBlank() == true
                if (luna.emojis[d]?.isNotBlank() == true)
                    append(";${luna.emojis[d]!!}")
                else if (hasVerbum) append(";")
                if (hasVerbum)
                    append(";${luna.verba[d]!!.saveVitaText()}")
                append("\n")
            }
            append("\n")
        }
    }.toString()

    fun find(key: String): Luna? = getOrElse(key) { null }

    fun save() {
        FileOutputStream(c.stored).use { fos ->
            fos.write(dump().encodeToByteArray())
        }
    }

    /** Reforms the Vita data and dumps it to be exported. */
    fun export(): ByteArray {
        reform()
        return dump().encodeToByteArray()
    }

    /** Removes the empty entries. */
    fun reform() {
        val removal = arrayListOf<String>()
        forEach { key, luna -> if (luna.isEmpty()) removal.add(key) }
        removal.forEach { remove(it) }
        save()
    }

    companion object {
        const val MAX_RANGE = 3f

        // String Extensions
        private fun String.loadVitaText() = replace("\\n", "\n")

        fun String.saveVitaText() = replace("\n", "\\n")


        // Float Extensions
        fun Float?.showScore(): String = if (this != 0f) (this?.toString() ?: "_") else "0"

        fun Float.writeScore(): String = if (this % 1f == 0f) toInt().toString() else toString()
    }
}

/** Subset of [Vita] for managing months. */
class Luna(
    length: Int,
    var default: Float? = null,
    var emoji: String? = null,
    var verbum: String? = null,
    var size: Long = 0L,
) {
    val diebus: Array<Float?> = Array(length) { null }
    val emojis: Array<String?> = Array(length) { null }
    val verba: Array<String?> = Array(length) { null }

    operator fun get(index: Int): Float? = diebus[index]
    operator fun set(index: Int, value: Float?) {
        diebus[index] = value
    }

    fun isEmpty() = diebus.all { it == null } && default == null

    fun mean(maxDays: Int): Float {
        if (maxDays == 0) return 0f
        val scores = arrayListOf<Float>()
        for (v in 0 until maxDays) (this[v] ?: default)?.let { scores.add(it) }
        return if (scores.isEmpty()) 0f else scores.sum() / scores.size
    }
}
