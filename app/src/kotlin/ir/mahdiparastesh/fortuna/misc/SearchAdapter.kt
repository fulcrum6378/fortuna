package ir.mahdiparastesh.fortuna.misc

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.emoji2.text.EmojiCompat
import androidx.recyclerview.widget.RecyclerView
import ir.mahdiparastesh.fortuna.Kit
import ir.mahdiparastesh.fortuna.Main
import ir.mahdiparastesh.fortuna.Vita
import ir.mahdiparastesh.fortuna.databinding.SearchItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchAdapter(private val c: Main) :
    RecyclerView.Adapter<Kit.AnyViewHolder<SearchItemBinding>>() {
    private var lastQ: String? = null
    private var results = ArrayList<Result>()
    private val sampleRadius = 20
    private val sampleMore = "..."

    @SuppressLint("NotifyDataSetChanged")
    fun search(q: CharSequence?) {
        if (q == lastQ) return
        lastQ = q.toString()
        results.clear()
        if (q == null || c.m.vita == null) {
            notifyDataSetChanged(); return; }

        CoroutineScope(Dispatchers.IO).launch {
            val vita = (c.m.vita!!.clone() as Vita).toSortedMap { a, b -> b.compareTo(a) }

            // Analyze the query text
            val emojis = arrayListOf<CharSequence>()
            val words = arrayListOf<CharSequence>()
            val numSym =
                arrayListOf<CharSequence>() // except that numbers and symbols are separated
            val ec = EmojiCompat.get()
            var end: Int
            var x = 0
            while (x < q.length) {
                if (ec.getEmojiStart(q, x) != -1) { // it is an emoji
                    end = ec.getEmojiEnd(q, x)
                    emojis.add(q.subSequence(x, end))
                } else if (q[x].isLetter()) {
                    end = x + 1
                    while (end < q.length && q[end].isLetter()) end++
                    words.add(q.subSequence(x, end))
                } else if (q[x].isDigit()) {
                    end = x + 1
                    while (end < q.length && q[end].isDigit()) end++
                    numSym.add(q.subSequence(x, end))
                } else if (q[x].isWhitespace()) {
                    x++; continue
                } else {
                    end = x + 1
                    while (end < q.length && ec.getEmojiStart(q, end) == -1
                        && !q[end].isLetter() && !q[end].isDigit() && !q[end].isWhitespace()
                    ) end++
                    numSym.add(q.subSequence(x, end))
                }
                x = end
            }
            words.addAll(numSym)

            for (luna in vita.entries) { // FIXME THIS IS AN any ALGORITHM NOT all!!
                //luna.value.emoji
                //luna.value.verbum  // SKIPPED FOR NOW
                for (d in luna.value.verba.indices) {
                    var dies = emojis.indexOfFirst { it == luna.value.emojis[d] }
                    var sample = ""
                    if (dies != -1) sample = luna.value.emojis[dies]!!
                    else for (word in words) {
                        val foundIn = luna.value.verba[d]
                            ?.indexOf(word.toString(), 0, true)
                        if (foundIn != -1 && foundIn != null) {
                            dies = d
                            sample = luna.value.verba[d]!!.replace("\n", " ")
                            if ((sample.length - foundIn) > sampleRadius) sample = sample
                                .substring(0..foundIn + sampleRadius) + sampleMore
                            if (foundIn > sampleRadius) sample = sampleMore + sample
                                .substring(foundIn - sampleRadius until sample.length)
                        }
                    }
                    if (dies != -1) results.add(Result(luna.key, dies.toByte(), sample))
                }
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    c, emojis.joinToString(", ") + " -- " +
                        words.joinToString(", ")/* + " -- " +
                        numSym.joinToString(", ")*/, Toast.LENGTH_SHORT
                ).show()
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
        Kit.AnyViewHolder<SearchItemBinding> =
        Kit.AnyViewHolder(SearchItemBinding.inflate(c.layoutInflater, parent, false))

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(h: Kit.AnyViewHolder<SearchItemBinding>, i: Int) {
        h.b.date.text = results[i].luna + "." + Kit.z(results[i].dies + 1)
        h.b.sample.text = results[i].sample
        h.b.sep.isVisible = i < results.size - 1

        h.b.root.setOnClickListener { }
    }

    override fun getItemCount(): Int = results.size

    data class Result(val luna: String, val dies: Byte, val sample: String)
}
