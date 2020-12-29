package com.egon12.developerhelper

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
@AndroidEntryPoint
class DatabaseFragment : Fragment() {

    private val viewModel: DatabaseViewModel by activityViewModels()

    private val adapter = TableListAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_database, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.tables().observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })

        val rvTable = view.findViewById<RecyclerView?>(R.id.rv_table)
        rvTable?.layoutManager = LinearLayoutManager(view.context)
        rvTable?.adapter = adapter
    }

    private fun navigateToTable(table: Table?) {
        table?.let {
            viewModel.loadData(it)
            findNavController().navigate(R.id.action_DatabaseFragment_to_TableFragment)
        }
    }

    inner class TableListAdapter() :
        ListAdapter<Table, TableListAdapter.ViewHolder>(TableDiffCallback()) {

        inner class ViewHolder(v: View) :
            RecyclerView.ViewHolder(v) {

            private val tvTableName: TextView? = itemView.findViewById(R.id.table_name)
            private var table: Table? = null

            init {
                itemView.setOnClickListener { this@DatabaseFragment.navigateToTable(table) }
            }

            fun bind(table: Table) {
                this.table = table
                tvTableName?.text = table.name
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val view = layoutInflater.inflate(R.layout.item_table, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(getItem(position))
        }
    }
}