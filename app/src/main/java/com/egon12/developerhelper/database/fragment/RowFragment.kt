package com.egon12.developerhelper.database.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.egon12.developerhelper.R
import com.egon12.developerhelper.database.Cell
import com.egon12.developerhelper.database.CellDiffUtil
import com.egon12.developerhelper.database.viewmodel.DatabaseViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton


class RowFragment : Fragment() {

    private val model by activityViewModels<DatabaseViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_row, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val rowAdapter = RowAdapter()

        view.findViewById<RecyclerView>(R.id.rv_row)?.apply {
            layoutManager = LinearLayoutManager(view.context)
            adapter = rowAdapter
        }

        model.data.cells.observe(requireActivity(), Observer {
            rowAdapter.submitList(it)
        })

        view.findViewById<FloatingActionButton>(R.id.btn_save).setOnClickListener { _ ->
            model.data.save(rowAdapter.currentList).observe(viewLifecycleOwner, Observer {
                if (it) findNavController().popBackStack()
            })
        }

        super.onViewCreated(view, savedInstanceState)
    }

    class RowAdapter : ListAdapter<Cell, RowAdapter.ViewHolder>(CellDiffUtil()) {
        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvLabel = itemView.findViewById<TextView>(R.id.tv_label)
            private val tvLabelType = itemView.findViewById<TextView>(R.id.tv_label_type)
            private lateinit var cell: Cell

            private val tvValue = itemView.findViewById<EditText>(R.id.tv_value).apply {
                this.addTextChangedListener {
                    if (this.tag == null) {
                        cell.dirtyValue = it.toString()
                    }
                }
            }

            fun bind(cell: Cell) {
                this.cell = cell
                tvLabel.text = cell.label
                tvValue.tag = "Edited"
                tvValue.setText(cell.value ?: "NULL")
                tvValue.tag = null
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