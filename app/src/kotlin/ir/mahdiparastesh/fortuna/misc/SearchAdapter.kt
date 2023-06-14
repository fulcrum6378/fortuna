package ir.mahdiparastesh.fortuna.misc

import android.annotation.SuppressLint
import android.text.Spanned
import android.text.style.MetricAffectingSpan
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.text.getSpans
import androidx.core.text.toSpanned
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
    fun search(q: String?): Boolean {
        if (q == lastQ) return false
        lastQ = q
        results.clear()
        if (q == null) {
            notifyDataSetChanged(); return true; }


        /*val spanned = EmojiCompat.get().process(q.toSpanned()) as? Spanned
        val emojis = spanned?.getSpans<MetricAffectingSpan>(0, spanned.length)
        Toast.makeText(c, emojis?.size.toString(), Toast.LENGTH_SHORT).show()*/
        Toast.makeText(c, EmojiCompat.get().getEmojiStart(q, 0).toString(), Toast.LENGTH_SHORT).show()

        notifyDataSetChanged()
        return true
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
        Kit.AnyViewHolder<SearchItemBinding> =
        Kit.AnyViewHolder(SearchItemBinding.inflate(c.layoutInflater, parent, false))

    override fun onBindViewHolder(h: Kit.AnyViewHolder<SearchItemBinding>, i: Int) {
    }

    override fun getItemCount(): Int = results.size

    //data class Item()
}
