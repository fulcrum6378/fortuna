package ir.mahdiparastesh.fortuna.misc

import android.annotation.SuppressLint
import android.view.ViewGroup
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

        notifyDataSetChanged()
        return true
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
        Kit.AnyViewHolder<SearchItemBinding> =
        Kit.AnyViewHolder(SearchItemBinding.inflate(c.layoutInflater, parent, false))

    override fun onBindViewHolder(h: Kit.AnyViewHolder<SearchItemBinding>, i: Int) {

    }

    override fun getItemCount(): Int = results.size

    data class Item()
}
