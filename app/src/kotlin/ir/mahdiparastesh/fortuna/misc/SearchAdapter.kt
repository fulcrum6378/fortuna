package ir.mahdiparastesh.fortuna.misc

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.widget.Toast
import androidx.emoji2.text.EmojiCompat
import androidx.recyclerview.widget.RecyclerView
import ir.mahdiparastesh.fortuna.Kit
import ir.mahdiparastesh.fortuna.Main
import ir.mahdiparastesh.fortuna.databinding.SearchItemBinding

class SearchAdapter(private val c: Main) :
    RecyclerView.Adapter<Kit.AnyViewHolder<SearchItemBinding>>() {
    private var results = ArrayList<String>()
    private var lastQ: String? = null

    @SuppressLint("NotifyDataSetChanged")
    fun search(q: CharSequence?) {
        if (q == lastQ) return
        lastQ = q.toString()
        results.clear()
        if (q == null) {
            notifyDataSetChanged(); return; }

        // Collect emojis from the text
        val emojis = arrayListOf<CharSequence>()
        val ec = EmojiCompat.get()
        var x = 0
        while (x < q.length) {
            val start = ec.getEmojiStart(q, x)
            if (start != -1) {
                val end = ec.getEmojiEnd(q, x)
                emojis.add(q.subSequence(x, end))
                x = end
                continue; }
            x++
        }

        // Search for emojis

        // Search for other texts

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
        Kit.AnyViewHolder<SearchItemBinding> =
        Kit.AnyViewHolder(SearchItemBinding.inflate(c.layoutInflater, parent, false))

    override fun onBindViewHolder(h: Kit.AnyViewHolder<SearchItemBinding>, i: Int) {
    }

    override fun getItemCount(): Int = results.size

    //data class Item()
}
