package ir.mahdiparastesh.fortuna.misc

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import ir.mahdiparastesh.fortuna.Kit
import ir.mahdiparastesh.fortuna.Main
import ir.mahdiparastesh.fortuna.R
import ir.mahdiparastesh.fortuna.Vita
import ir.mahdiparastesh.fortuna.Vita.Companion.toCalendar
import ir.mahdiparastesh.fortuna.databinding.SearchItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.ArrayList
import kotlin.collections.any
import kotlin.collections.arrayListOf
import kotlin.collections.forEach
import kotlin.collections.hashMapOf
import kotlin.collections.indices
import kotlin.collections.mutableSetOf
import kotlin.collections.plus
import kotlin.collections.set
import kotlin.collections.toSortedMap
import kotlin.math.max
import kotlin.math.min

/** A RecyclerView adapter for the search dialogue which also includes utilities for searching. */
class SearchAdapter(private val c: Main) :
    RecyclerView.Adapter<Kit.AnyViewHolder<SearchItemBinding>>() {
    lateinit var dialogue: AlertDialog

    companion object {
        const val sampleRadius = 50
        const val sampleMore = "..."
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

        h.b.root.setOnClickListener {
            c.m.calendar = c.m.searchResults[i].luna.toCalendar(Kit.calType)
            c.onCalendarChanged()
            dialogue.cancel()
            c.closeDrawer()
        }
    }

    override fun getItemCount(): Int = c.m.searchResults.size


    inner class Search(q: CharSequence, clonedVita: Vita, onFinish: CoroutineScope.() -> Unit) {
        private val vita = clonedVita.toSortedMap { a, b -> b.compareTo(a) }
        private val qEmojis = mutableSetOf<String>()
        private val qWords = mutableSetOf<String>()
        private val exclusive = !c.sp.getBoolean(Kit.SP_SEARCH_INCLUSIVE, false)

        init {
            CoroutineScope(Dispatchers.IO).launch {
                // process the query text
                var end: Int
                var x = 0
                while (x < q.length) {
                    if (q[x].isLetter()) {
                        end = x + 1
                        while (end < q.length && q[end].isLetter()) end++
                        qWords.add(q.subSequence(x, end).toString())
                    } else if (q[x].isDigit()) {
                        end = x + 1
                        while (end < q.length && q[end].isDigit()) end++
                        qWords.add(q.subSequence(x, end).toString())
                    } else if (q[x].isWhitespace()) {
                        x++; continue
                    } else if (getEmojiEndIfStartsWithIt(q.subSequence(x, q.length)).also {
                            end = x + it
                        } != -1) {
                        qEmojis.add(q.subSequence(x, end).toString())
                    } else { // then it should be a symbol...
                        end = x + 1
                        while (end < q.length //&& ec.getEmojiStart(q, end) == -1
                            && getEmojiEndIfStartsWithIt(q.subSequence(end, q.length)) == -1
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
            }
        }

        private fun onEachDay(luna: String, d: Byte, emoji: String?, verbum: String?) {
            val matches = hashMapOf<String, ArrayList<Pair<Int, Int>>>()
            for (q in qWords.plus(qEmojis)) matches[q] = arrayListOf()

            // check if emoji of luna is queried
            if (emoji != null) for (e in qEmojis)
                if (e == emoji) matches[e]!!.add(Pair(-1, -1))

            // check the daily notes (verbum)
            if (verbum != null) for (q in matches.keys) {
                val index = verbum.indexOf(q, 0, true)
                if (index != -1) matches[q]!!.add(Pair(index, index + q.length))
            }

            // determine if the search was successful
            var allGood = true
            var anyGood = false
            for (subMatch in matches.values)
                if (subMatch.isEmpty()) allGood = false
                else anyGood = true
            if (!anyGood) return
            if (exclusive && !allGood) return

            // now render the sample
            val sample: CharSequence
            val includeTheEmoji =
                matches.values.any { pairs -> pairs.any { pair -> pair.first == -1 } }
            if (verbum != null) {
                var preSample = SpannableStringBuilder(verbum)
                var leastMin = verbum.length
                var greatestMax = 0
                if (includeTheEmoji) leastMin = 0

                for (pairs in matches.values)
                    pairs.forEach { pair ->
                        if (pair.first == -1) return@forEach
                        preSample.setSpan(
                            StyleSpan(Typeface.BOLD), pair.first, pair.second,
                            Spannable.SPAN_INCLUSIVE_INCLUSIVE
                        )
                        leastMin = min(leastMin, pair.first)
                        greatestMax = max(greatestMax, pair.second)
                    }
                leastMin -= sampleRadius
                greatestMax += sampleRadius
                if (leastMin < 0) leastMin = 0
                if (greatestMax > verbum.length) greatestMax = verbum.length
                preSample = preSample.subSequence(leastMin, greatestMax) as SpannableStringBuilder
                if (leastMin != 0) preSample.insert(0, sampleMore)
                if (greatestMax != verbum.length) preSample.append(sampleMore)

                for (ch in preSample.indices)
                    if (preSample[ch] == '\n') preSample.replace(ch, ch + 1, " ")
                if (includeTheEmoji) preSample.insert(0, "${emoji!!} ")
                sample = preSample
            } else sample = emoji!!

            c.m.searchResults.add(Result(luna, d, sample))
            // never convert a SpannableStringBuilder to String!
        }

        private fun getEmojiEndIfStartsWithIt(text: CharSequence): Int {
            for (e in c.m.emojis) if (text.indexOf(e) == 0) return e.length
            return -1
        }
    }

    data class Result(val luna: String, val dies: Byte, val sample: CharSequence)
}
