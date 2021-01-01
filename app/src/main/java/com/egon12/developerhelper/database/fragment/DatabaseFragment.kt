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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.egon12.developerhelper.R
import com.egon12.developerhelper.database.ConnectionStatus
import com.egon12.developerhelper.database.Table
import com.egon12.developerhelper.database.TableDiffCallback
import com.egon12.developerhelper.database.viewmodel.DatabaseViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
@AndroidEntryPoint
class DatabaseFragment : Fragment() {

    private val model: DatabaseViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_database, container, false)
    }

    val uuid: UUID? by lazy { arguments?.get("uuid") as? UUID }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uuid = uuid ?: return close()

        model.connect(uuid).observe(viewLifecycleOwner, {
            when (it) {
                ConnectionStatus.Connected -> model.table.reload()
                ConnectionStatus.Disconnected -> close()
                ConnectionStatus.Connecting, null -> {
                    // Do Nothing
                }
            }
        })

        val listAdapter = TableListAdapter()
        model.table.apply {
            data.observe(viewLifecycleOwner, {
                listAdapter.submitList(it)
            })
            reload()
        }

        view.findViewById<RecyclerView?>(R.id.rv_table)?.apply {
            layoutManager = LinearLayoutManager(view.context)
            adapter = listAdapter
        }

        val eQuery = view.findViewById<EditText>(R.id.edit_query)?.apply {
            addTextChangedListener { editable ->
                editable?.toString()?.let {
                    model.table.search(it)
                }
            }
        }

        view.findViewById<FloatingActionButton>(R.id.btn_execute)?.apply {
            setOnClickListener {
                val query = eQuery?.text.toString()
                model.data.query(query)
                findNavController().navigate(R.id.action_DatabaseFragment_to_TableFragment)
            }
        }
    }

    private fun navigateToTable(table: Table?) {
        table?.let {
            model.data.getData(it)
            findNavController().navigate(R.id.action_DatabaseFragment_to_TableFragment)
        }
    }

    private fun close() {
        findNavController().popBackStack()
    }


    inner class TableListAdapter :
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