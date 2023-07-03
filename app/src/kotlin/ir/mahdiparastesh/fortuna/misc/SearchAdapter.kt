package ir.mahdiparastesh.fortuna.misc

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import ir.mahdiparastesh.fortuna.Kit
import ir.mahdiparastesh.fortuna.Main
import ir.mahdiparastesh.fortuna.R
import ir.mahdiparastesh.fortuna.Vita
import ir.mahdiparastesh.fortuna.databinding.SearchItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.min

/** A RecyclerView adapter for the search dialogue which also includes utilities for searching. */
class SearchAdapter(private val c: Main) :
    RecyclerView.Adapter<Kit.AnyViewHolder<SearchItemBinding>>() {

    companion object {
        const val sampleRadius = 50
        const val sampleMore = "..."
        const val maxEmojiChars = 6
    }

    @SuppressLint("NotifyDataSetChanged")
    fun search(q: CharSequence?, inclusivityChanged: Boolean = false) {
        if (q == c.m.lastSearchQuery && !inclusivityChanged) return
        c.m.lastSearchQuery = q.toString()
        c.m.searchResults.clear()
        if (q != null && c.m.vita != null)
            Search(q, c.m.vita!!.clone() as Vita) { notifyDataSetChanged() }
        else notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
        Kit.AnyViewHolder<SearchItemBinding> =
        Kit.AnyViewHolder(SearchItemBinding.inflate(c.layoutInflater, parent, false))

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(h: Kit.AnyViewHolder<SearchItemBinding>, i: Int) {
        h.b.date.text = c.m.searchResults[i].luna + "." + (
            if (c.m.searchResults[i].dies >= 0) Kit.z(c.m.searchResults[i].dies + 1)
            else c.getString(R.string.defValue)
            )
        h.b.sample.text = c.m.searchResults[i].sample
        h.b.sep.isVisible = i < c.m.searchResults.size - 1

        h.b.root.setOnClickListener { }
    }

    override fun getItemCount(): Int = c.m.searchResults.size


    inner class Search(q: CharSequence, clonedVita: Vita, onFinish: CoroutineScope.() -> Unit) {
        private val vita = clonedVita.toSortedMap { a, b -> b.compareTo(a) }
        private val qEmojis = arrayListOf<String>()
        private val qWords = arrayListOf<String>()

        init {
            CoroutineScope(Dispatchers.IO).launch {
                // process the query text
                var end: Int
                var x = 0
                while (x < q.length) {
                    /*if (ec.getEmojiStart(q, x) != -1) {
                        end = ec.getEmojiEnd(q, x)
                        qEmojis.add(q.subSequence(x, end).toString())
                        // together with the next case, they completely support emojis
                    } else */if (q[x].isLetter()) {
                        end = x + 1
                        while (end < q.length && q[end].isLetter()) end++
                        qWords.add(q.subSequence(x, end).toString())
                    } else if (q[x].isDigit()) {
                        end = x + 1
                        while (end < q.length && q[end].isDigit()) end++
                        qWords.add(q.subSequence(x, end).toString())
                    } else if (q[x].isWhitespace()) {
                        x++; continue
                    } else if (getEmojiEnd(q.subSequence(x, q.length)).also { end = x + it } != -1)
                        qEmojis.add(q.subSequence(x, end).toString())
                    else { // symbols
                        end = x + 1
                        while (end < q.length //&& ec.getEmojiStart(q, end) == -1
                            && getEmojiEnd(q.subSequence(end, q.length)) == -1
                            && !q[end].isLetter() && !q[end].isDigit() && !q[end].isWhitespace()
                        ) end++
                        qWords.add(q.subSequence(x, end).toString())
                    }
                    x = end
                }

                // then search the Vita
                for (luna in vita.entries) {
                    onEachDay(luna.key, (-1).toByte(), luna.value.emoji, luna.value.verbum)
                    for (d in luna.value.verba.indices.reversed())
                        onEachDay(luna.key, d.toByte(), luna.value.emojis[d], luna.value.verba[d])
                }

                withContext(Dispatchers.Main, onFinish)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        c, qEmojis.joinToString(", ") + " -- " +
                            qWords.joinToString(", "), Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        private fun onEachDay(luna: String, d: Byte, emoji: String?, verbum: String?) {
            var found = qEmojis.any { it == emoji }
            var sample: CharSequence = ""
            if (found) sample = emoji!! + " " + (verbum?.let {
                if (it.length > sampleRadius) it.substring(0, sampleRadius) + sampleMore else it
            } ?: "")
            else for (q in qWords.plus(qEmojis)) { // TODO FOREACH WTF?
                var foundIn = verbum?.indexOf(q, 0, true)
                if (foundIn != -1 && foundIn != null) {
                    found = true
                    sample = verbum!!.replace("\n", " ")
                    if ((sample.length - (foundIn + q.length)) > sampleRadius) sample =
                        sample.substring(0, (foundIn + q.length) + sampleRadius) + sampleMore
                    if (foundIn > sampleRadius) sample = sampleMore + sample
                        .substring(foundIn - sampleRadius until sample.length)
                    sample = SpannableStringBuilder(sample).apply {
                        foundIn = sample.indexOf(q, 0, true)
                        setSpan(
                            StyleSpan(Typeface.BOLD), foundIn!!, foundIn!! + q.length,
                            Spannable.SPAN_INCLUSIVE_INCLUSIVE
                        )
                    }
                    break
                }
            }
            if (found) c.m.searchResults.add(Result(luna, d, sample))
            // don't convert sample toString!
        }

        /** Helper function for better supporting emojis. */
        private fun getEmojiEnd(text: CharSequence): Int {
            // FIXME this iteration makes many mistakes
            /*var i = 1
            while (i <= min(maxEmojiChars, text.length)) {
                (ec.getEmojiMatch(text.subSequence(0, i), Main.EMOJI_METADATA_VERSION)
                    in 1..2).let { if (it) return@getEmojiEnd i }
                i++
            }*/
            return -1
        }
    }

    data class Result(val luna: String, val dies: Byte, val sample: CharSequence)
}
