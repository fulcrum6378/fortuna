package ir.mahdiparastesh.fortuna

import android.content.Context
import android.icu.util.Calendar
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class Vita : HashMap<String, Luna>() {

    fun findByKey(key: String): Luna? = getOrElse(key) { null }

    fun findByCalendar(cal: PersianCalendar): Luna? = getOrElse(cal.toKey()) { null }

    fun save(c: Context) {
        save(c, this)
    }

    companion object {
        const val MAX_RANGE = 3f
        const val MIN_RANGE = -MAX_RANGE

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

        fun String.toPersianCalendar(): PersianCalendar =
            PersianCalendar(substring(0, 4).toInt(), substring(5, 7).toInt(), 1)

        fun z(n: Any?, ideal: Int = 2): String {
            var s = n.toString()
            while (s.length < ideal) s = "0$s"
            return s
        }
    }

    class Stored(c: Context) : File(c.filesDir, "vita.json")
}

typealias Luna = Array<Float?>
