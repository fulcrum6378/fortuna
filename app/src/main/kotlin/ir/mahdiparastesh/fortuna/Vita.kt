package ir.mahdiparastesh.fortuna

import android.content.Context
import android.icu.util.Calendar
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class Vita : HashMap<String, Luna>() {

    fun findByCalendar(cal: PersianCalendar): Luna? = getOrElse(cal.toKey()) { null }

    fun save(c: Context) {
        save(c, this)
    }

    companion object {
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

        fun PersianCalendar.toKey(): String =
            "${this[Calendar.YEAR]}.${z(this[Calendar.MONTH] + 1)}"

        fun z(n: Int): String {
            val s = n.toString()
            return if (s.length == 1) "0$s" else s
        }
    }

    class Stored(c: Context) : File(c.filesDir, "vita.json")
}

class Luna : ArrayList<Float?>(arrayOfNulls<Float?>(31).toList()) {
    /*override fun add(element: Float?): Boolean = false
    override fun add(index: Int, element: Float?) {}
    override fun addAll(elements: Collection<Float?>): Boolean = false
    override fun addAll(index: Int, elements: Collection<Float?>): Boolean = false
    override fun removeAt(index: Int): Float? = null
    override fun remove(element: Float?): Boolean = false
    override fun removeAll(elements: Collection<Float?>): Boolean = false
    override fun removeIf(filter: Predicate<in Float?>): Boolean = false
    override fun removeRange(fromIndex: Int, toIndex: Int) {}*/

    companion object {
        const val MAX_RANGE = 3f
        const val MIN_RANGE = -MAX_RANGE
    }
}
