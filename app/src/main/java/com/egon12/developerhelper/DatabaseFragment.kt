package com.egon12.developerhelper

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
@AndroidEntryPoint
class DatabaseFragment : Fragment() {

    private var rvTable: RecyclerView? = null

    private val viewModel: DatabaseViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_database, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvTable = view.findViewById(R.id.rv_table)
        rvTable?.layoutManager = LinearLayoutManager(view.context)
        val adapter = TableListAdapter(this::navigateToTable)

        viewModel.tables.observe(viewLifecycleOwner, Observer {
            if (it is DatabaseViewModel.TablesResult.Success) {
                adapter.submitList(it.table)
            } else if (it is DatabaseViewModel.TablesResult.Error) {
                Log.e("getTables", "Cannot show tables", it.exception)
                Snackbar.make(view, it.exception.localizedMessage, Snackbar.LENGTH_LONG).show()
            }
        })

        viewModel.loadTables()

        rvTable?.adapter = adapter
    }

    private fun navigateToTable(table: Table?) {
        table?.let {
            viewModel.loadData(it)
            findNavController().navigate(R.id.action_DatabaseFragment_to_TableFragment)
        }
    }


}

class TableDiffCallback : DiffUtil.ItemCallback<Table>() {
    override fun areItemsTheSame(oldItem: Table, newItem: Table): Boolean = oldItem == newItem

    override fun areContentsTheSame(oldItem: Table, newItem: Table): Boolean = oldItem == newItem
}

class TableListAdapter(private val onItemClick: (Table?) -> Unit) :
    ListAdapter<Table, TableListAdapter.ViewHolder>(TableDiffCallback()) {

    class ViewHolder(itemView: View, val onItemClick: (Table?) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val tvTableName: TextView? = itemView.findViewById(R.id.table_name)
        private var table: Table? = null

        init {
            itemView.setOnClickListener {
                onItemClick(table)
            }
        }


        fun bind(table: Table) {
            this.table = table
            tvTableName?.text = table.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.item_table, parent, false)
        return ViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

