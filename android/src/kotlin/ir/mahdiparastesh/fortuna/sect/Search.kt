package ir.mahdiparastesh.fortuna.sect

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.mahdiparastesh.fortuna.Fortuna
import ir.mahdiparastesh.fortuna.Main
import ir.mahdiparastesh.fortuna.R
import ir.mahdiparastesh.fortuna.Vita
import ir.mahdiparastesh.fortuna.databinding.SearchBinding
import ir.mahdiparastesh.fortuna.databinding.SearchItemBinding
import ir.mahdiparastesh.fortuna.util.AnyViewHolder
import ir.mahdiparastesh.fortuna.util.BaseDialogue
import ir.mahdiparastesh.fortuna.util.NumberUtils.z
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min

/** A dialog box for searching in [Vita] verbum descriptions */
class SearchDialog : BaseDialogue() {

    private lateinit var dialogue: AlertDialog
    private val b: SearchBinding by lazy { SearchBinding.inflate(layoutInflater) }

    companion object {
        const val TAG = "search"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        b.field.addTextChangedListener { isCancelable = it.isNullOrEmpty() }
        b.field.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO)
                (b.list.adapter as SearchAdapter).search(v.text)
            return@setOnEditorActionListener true
        }
        b.inclusivity.isChecked = c.c.sp.getBoolean(Fortuna.SP_SEARCH_INCLUSIVE, false)
        b.inclusivity.setOnCheckedChangeListener { _, bb ->
            c.c.sp.edit { putBoolean(Fortuna.SP_SEARCH_INCLUSIVE, bb) }
            (b.list.adapter as SearchAdapter).search(b.field.text, true)
        }

        b.list.adapter = SearchAdapter(c, this)

        isCancelable = true
        dialogue = MaterialAlertDialogBuilder(c).apply {
            setIcon(R.drawable.search)
            setTitle(R.string.navSearch)
            setView(b.root)
        }.create()
        return dialogue
    }

    override fun onResume() {
        super.onResume()
        dialogue.setOnKeyListener { _, keyCode, event ->
            if (keyCode == android.view.KeyEvent.KEYCODE_BACK &&
                event.action == android.view.KeyEvent.ACTION_UP &&
                !isCancelable
            ) {
                dismiss(); true
            } else false
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        c.m.searchResults.clear()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        c.m.searchResults.clear()
    }
}

/** A RecyclerView adapter for the search dialogue which also includes utilities for searching. */
class SearchAdapter(
    private val c: Main, private val f: SearchDialog
) : RecyclerView.Adapter<AnyViewHolder<SearchItemBinding>>() {

    private val sampleRadius = 50
    private val sampleMore = "..."

    @SuppressLint("NotifyDataSetChanged")
    fun search(q: CharSequence?, inclusivityChanged: Boolean = false) {
        if (q == c.m.lastSearchQuery && !inclusivityChanged) return
        c.m.lastSearchQuery = q.toString()
        c.m.searchResults.clear()
        if (!q.isNullOrBlank())
            Search(q.trim(), c.c.vita.clone() as Vita) {
                notifyDataSetChanged()
                if (c.m.searchResults.isEmpty())
                    Toast.makeText(c, R.string.foundNothing, Toast.LENGTH_SHORT).show()
                f.requireDialog()
                    .setTitle(f.getString(R.string.navSearch) + " {${c.m.searchResults.size}}")
            }
        else {
            notifyDataSetChanged()
            f.requireDialog().setTitle(R.string.navSearch)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            AnyViewHolder<SearchItemBinding> =
        AnyViewHolder(SearchItemBinding.inflate(c.layoutInflater, parent, false))

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(h: AnyViewHolder<SearchItemBinding>, i: Int) {
        h.b.date.text = c.m.searchResults[i].luna + "." + (
                if (c.m.searchResults[i].dies >= 0) z(c.m.searchResults[i].dies + 1)
                else c.getString(R.string.defValue)
                )
        h.b.sample.text = c.m.searchResults[i].sample
        h.b.sep.isVisible = i < c.m.searchResults.size - 1

        h.b.root.setOnClickListener {
            c.c.date = c.c.lunaToDate(c.m.searchResults[h.layoutPosition].luna)
            c.onDateChanged()
            c.variabilis(c.m.searchResults[h.layoutPosition].dies.toInt())
        }
    }

    override fun getItemCount(): Int = c.m.searchResults.size


    inner class Search(q: CharSequence, clonedVita: Vita, onFinish: CoroutineScope.() -> Unit) {
        private val vita = clonedVita.toSortedMap { a, b -> b.compareTo(a) }
        private val qEmojis = mutableSetOf<String>()
        private val qWords = mutableSetOf<String>()
        private val exclusive = !c.c.sp.getBoolean(Fortuna.SP_SEARCH_INCLUSIVE, false)

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
