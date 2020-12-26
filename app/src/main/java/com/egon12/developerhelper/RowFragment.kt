package com.egon12.developerhelper

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView


class RowFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_row, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val viewModel = ViewModelProvider(requireActivity()).get(DatabaseViewModel::class.java)
        val rvRow = view.findViewById<RecyclerView>(R.id.rv_row)
        val adapter = RowAdapter()
        rvRow.layoutManager = LinearLayoutManager(view.context)
        rvRow.adapter = adapter

        viewModel.row.observe(requireActivity(), Observer{
            adapter.submitList(it)
        })

        super.onViewCreated(view, savedInstanceState)
    }

    class RowAdapter: ListAdapter<Cell, RowAdapter.ViewHolder>(CellDiffUtil()) {
        class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
            private val tvLabel = itemView.findViewById<TextView>(R.id.tv_label)
            private val tvLabelType = itemView.findViewById<TextView>(R.id.tv_label_type)
            private lateinit var cell: Cell

            private val tvValue = itemView.findViewById<EditText>(R.id.tv_value).apply {
                this.addTextChangedListener { cell.dirtyValue = it.toString() }
            }

            fun bind(cell: Cell) {
                this.cell = cell
                tvLabel.text = cell.label
                tvValue.setText(cell.value ?: "NULL")
                tvLabelType.text = cell.type
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.item_cell, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(getItem(position))
        }
    }
}