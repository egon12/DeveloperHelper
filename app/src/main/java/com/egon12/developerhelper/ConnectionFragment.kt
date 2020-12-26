package com.egon12.developerhelper

import android.annotation.SuppressLint
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
import com.egon12.developerhelper.database.persistent.Connection
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ConnectionFragment : Fragment() {

    private val viewModel: DatabaseViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_connection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rv = view.findViewById<RecyclerView>(R.id.rv_connections)
        val adapter = ConnectionListAdapter(this::click, this::longClick)
        rv.layoutManager = LinearLayoutManager(view.context)
        rv.adapter = adapter
        viewModel.connections.observe(requireActivity(), Observer {
            adapter.submitList(it)
        })

        view.findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            viewModel.newConnection()
            findNavController().navigate(R.id.action_ConnectionFragment_to_EditConnectionFragment)
        }
    }

    private fun click(conn: Connection) {
        viewModel.connectToDatabase(conn).observe(this, Observer {
            if (it == ConnectionStatus.Connected) {
                findNavController().navigate(R.id.action_ConnectionFragment_to_DatabaseFragment)
            }
        })
    }

    private fun longClick(conn: Connection) {
        viewModel.editConnection(conn)
        findNavController().navigate(R.id.action_ConnectionFragment_to_EditConnectionFragment)
    }

    class ConnectionListAdapter(
        private val onClick: (Connection) -> Unit,
        private val onLongClick: (Connection) -> Unit
    ) : ListAdapter<Connection, ConnectionListAdapter.ViewHolder>(ConnectionDiffUtil()) {
        class ViewHolder(
            view: View,
            onClick: (Connection) -> Unit,
            onLongClick: (Connection) -> Unit
        ) : RecyclerView.ViewHolder(view) {

            private val tvName: TextView? = itemView.findViewById(R.id.tv_name)
            private val tvHostDBName: TextView? = itemView.findViewById(R.id.tv_host_dbname)
            lateinit var connection: Connection

            init {
                itemView.setOnLongClickListener { onLongClick(connection); true }
                itemView.setOnClickListener { onClick(connection) }
            }

            @SuppressLint("SetTextI18n")
            fun bind(conn: Connection) {
                connection = conn
                tvName?.text = conn.name
                tvHostDBName?.text = "${conn.host}/${conn.dbName}"
            }
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.item_connection, parent, false)
            return ViewHolder(view, onClick, onLongClick)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(getItem(position))
        }
    }
}