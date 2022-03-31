package ir.mahdiparastesh.fortuna

import android.content.Context
import android.icu.util.Calendar
import android.os.Build
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class Vita : HashMap<String, Luna>() {

    fun find(key: String): Luna? = getOrElse(key) { null }

    fun save(c: Context) {
        save(c, this)
    }

    fun removeEmptyFields(c: Context) {
        val removal = arrayListOf<String>()
        forEach { key, luna -> if (luna.all { it == null }) removal.add(key) }
        removal.forEach { remove(it) }
        save(c)
    }

    companion object {
        const val MAX_RANGE = 3f
        val MIME_TYPE =
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) "application/octet-stream"
            else "application/json"

        fun load(c: Context): Vita = if (Stored(c).exists()) {
            val data: ByteArray
            FileInputStream(Stored(c)).use { data = it.readBytes() }
            Gson().fromJson(String(data), Vita::class.java)
        } else Vita()

        fun save(c: Context, vita: Vita) {
            FileOutputStream(Stored(c)).use { fos ->
                fos.write(
                    GsonBuilder().setPrettyPrinting().create().toJson(vita).encodeToByteArray()
                )
            }
        }

        fun emptyLuna() = Array<Float?>(31) { null }

        fun PersianCalendar.toKey(): String =
            "${z(this[Calendar.YEAR], 4)}.${z(this[Calendar.MONTH] + 1)}"

        fun String.toPersianCalendar(): PersianCalendar {
            val spl = split(".")
            return PersianCalendar(spl[0].toInt(), spl[1].toInt(), 1)
        }

        fun Float?.showScore(): String = if (this != 0f) this?.toString() ?: "_" else "0"

        fun z(n: Any?, ideal: Int = 2): String {
            var s = n.toString()
            while (s.length < ideal) s = "0$s"
            return s
        }
    }

    class Stored(c: Context) : File(c.filesDir, "vita.json")
}

typealias Luna = Array<Float?>
