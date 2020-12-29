package com.egon12.developerhelper.database.fragment

import android.graphics.Paint
import android.os.Bundle
import android.text.TextUtils
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
import com.egon12.developerhelper.R
import com.egon12.developerhelper.database.Data
import com.egon12.developerhelper.database.Row
import com.egon12.developerhelper.database.viewmodel.DatabaseViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class TableFragment : Fragment() {

    private val model: DatabaseViewModel by activityViewModels()

    var paddingCell: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_table, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        paddingCell = (view.resources.displayMetrics.density * PADDING_CELL).toInt()

        val rvTable = view.findViewById<RecyclerView>(R.id.rv_table)?.apply {
            layoutManager = LinearLayoutManager(view.context)
        }

        val lTableHeader = view.findViewById<LinearLayout>(R.id.table_header)

        model.data.data.observe(viewLifecycleOwner, Observer { data ->
            lTableHeader.removeAllViews()

            val columnWidths = data.columnDefinition.mapIndexed { idx, _ ->
                this.measureColumnWidth(view, data, idx)
            }

            data.columnDefinition.forEachIndexed { idx, col ->
                createTextView(view, columnWidths[idx])
                    .apply { text = col.label }
                    .let { lTableHeader.addView(it) }
            }

            val adapter = DataAdapter(data, columnWidths)
            rvTable?.adapter = adapter
        })

        view.findViewById<FloatingActionButton>(R.id.btn_insert)?.apply {
            setOnClickListener {
                model.data.new()
                findNavController().navigate(R.id.action_TableFragment_to_RowFragment)
            }
        }
    }

    inner class DataAdapter(private val data: Data, private val columnWidths: List<Int>) :
        RecyclerView.Adapter<DataAdapter.ViewHolder>() {

        inner class ViewHolder(v: View, val texts: List<TextView>) : RecyclerView.ViewHolder(v) {

            private var row: Row? = null

            init {
                itemView.setOnClickListener { this@TableFragment.interactRow(row) }
            }

            fun bind(row: Row) {
                this.row = row
                row.cells.forEachIndexed { idx, value -> texts[idx].text = value ?: "NULL" }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val tvList = columnWidths.map { columnWidth -> createTextView(parent, columnWidth) }
            val group = LinearLayout(parent.context).apply { orientation = LinearLayout.HORIZONTAL }
            tvList.forEach { group.addView(it) }
            return ViewHolder(group, tvList)
        }

        override fun onBindViewHolder(holder: ViewHolder, pos: Int) = holder.bind(data.rows[pos])

        override fun getItemCount() = data.rows.size
    }

    private fun interactRow(row: Row?) {
        row?.let {
            model.data.edit(it)
            findNavController().navigate(R.id.action_TableFragment_to_RowFragment)
        }
    }

    private fun measureColumnWidth(view: View, data: Data, index: Int): Int {
        val paint = Paint()
        paint.textSize = view.resources.displayMetrics.scaledDensity * 25
        var width = paint.measureText(data.columnDefinition[index].label)

        data.rows.forEach {
            val newWidth = paint.measureText(it.cells[index] ?: "")
            if (width < newWidth) width = newWidth
        }

        if (width > MAX_CELL_WIDTH) width = MAX_CELL_WIDTH

        return width.toInt()
    }

    private fun createTextView(view: View, columnWidth: Int) = TextView(view.context).apply {
        setPadding(paddingCell, paddingCell, paddingCell, paddingCell)
        width = columnWidth
        maxLines = 2
        ellipsize = TextUtils.TruncateAt.MARQUEE
        minHeight = 25
    }

    companion object {
        const val MAX_CELL_WIDTH = 500.0F
        const val PADDING_CELL = 4
    }
}
