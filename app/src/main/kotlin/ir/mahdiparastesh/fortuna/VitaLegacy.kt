package ir.mahdiparastesh.fortuna

import android.content.Context
import com.google.gson.Gson
import java.io.File
import java.io.FileInputStream

class VitaLegacy : HashMap<String, Array<Float?>>() {
    fun migrate(c: Context): Vita = Vita().apply {
        this@VitaLegacy.forEach { k, luna ->
            this@apply[k] = Luna().apply {
                for (d in diebus.indices) diebus[d] = luna[d]
                default = luna.last()
            }
        }
        save(c)
    }

    companion object {
        fun load(c: Context): VitaLegacy? {
            val stored = File(c.filesDir, "vita.json")
            return if (stored.exists()) {
                val data: ByteArray
                FileInputStream(stored).use { data = it.readBytes() }
                Gson().fromJson(String(data), VitaLegacy::class.java)
            } else null
        }
    }
}
