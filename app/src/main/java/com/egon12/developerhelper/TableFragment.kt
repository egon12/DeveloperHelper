package com.egon12.developerhelper

import android.graphics.Paint
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.egon12.developerhelper.TableFragment.Companion.PADDING_CELL

class TableFragment : Fragment() {

    private var rvTable: RecyclerView? = null

    private val model: DatabaseViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_table, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvTable = view.findViewById<RecyclerView>(R.id.rv_table)?.apply {
            layoutManager = LinearLayoutManager(view.context)
        }

        val lTableHeader = view.findViewById<LinearLayout>(R.id.table_header)

        model.data.observe(viewLifecycleOwner, Observer {
            lTableHeader.removeAllViews()

            val columnWidths = it.columnDefinition.mapIndexed { idx, _ ->
                this.measureColumnWidth(view, it, idx)
            }

            Log.d("columnWidth", columnWidths.toString())

            it.columnDefinition.forEachIndexed { idx, col ->
                val tv = TextView(view.context)
                tv.width = columnWidths[idx]
                tv.text = col.label
                lTableHeader.addView(tv)
            }

            val adapter = DataAdapter(it, this::interactRow, columnWidths)
            rvTable?.adapter = adapter
        })


    }

    private fun interactRow(row: Row?) {
        row?.let {
            model.loadRow(it)
            findNavController().navigate(R.id.action_TableFragment_to_RowFragment)
        }
    }

    private fun measureColumnWidth(
        view: View,
        data: Data,
        index: Int
    ): Int {
        val paint = Paint()
        paint.textSize = view.resources.displayMetrics.density * 25
        var mr = paint.measureText(data.columnDefinition[index].label)

        data.rows.forEach {
            val newmr = paint.measureText(it.cells[index] ?: "")
            if (mr < newmr) mr = newmr
        }

        if (mr > MAX_CELL_WIDTH) mr = MAX_CELL_WIDTH

        return mr.toInt()
    }

    companion object {
        const val MAX_CELL_WIDTH = 500.0F
        const val PADDING_CELL = 4
    }

}

class DataAdapter(
    private val data: Data,
    private val onItemClick: (Row?) -> Unit,
    private val columnWidths: List<Int>
) :
    RecyclerView.Adapter<DataAdapter.ViewHolder>() {

    class ViewHolder(itemView: View, val texts: List<TextView>, val onItemClick: (Row?) -> Unit) :
        RecyclerView.ViewHolder(itemView) {

        private var row: Row? = null

        init {
            itemView.setOnClickListener {
                onItemClick(row)
            }
        }

        fun bind(row: Row) {
            this.row = row
            row.cells.forEachIndexed { idx, value -> texts[idx].text = value ?: "NULL" }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val ctx = parent.context

        val padding: Int = (parent.resources.displayMetrics.density * PADDING_CELL).toInt()

        val tvList = columnWidths.map { columnWidth ->
            TextView(ctx).also {
                it.setPadding(padding, padding, padding, padding)
                it.width = columnWidth
                it.maxLines = 2
                it.ellipsize = TextUtils.TruncateAt.MARQUEE
                it.minHeight = 25
            }
        }

        val group = LinearLayout(ctx)
        group.orientation = LinearLayout.HORIZONTAL
        tvList.forEach { group.addView(it) }

        return ViewHolder(group, tvList, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data.rows[position])
    }

    override fun getItemCount() = data.rows.size
}

