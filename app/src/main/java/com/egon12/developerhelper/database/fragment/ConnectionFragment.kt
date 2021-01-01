package com.egon12.developerhelper.database.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.egon12.developerhelper.ConnInfo
import com.egon12.developerhelper.ConnType
import com.egon12.developerhelper.R
import com.egon12.developerhelper.UuidDiffUtil
import com.egon12.developerhelper.database.viewmodel.DatabaseViewModel
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ConnectionFragment : Fragment() {

    private val model: DatabaseViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_connection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listAdapter = Adapter()

        view.findViewById<RecyclerView>(R.id.rv_connections)?.apply {
            layoutManager = LinearLayoutManager(view.context)
            adapter = listAdapter
        }

        model.connection.list.observe(viewLifecycleOwner, { listAdapter.submitList(it) })

        val addDbButton = view.findViewById<ExtendedFloatingActionButton>(R.id.fab_db)?.apply {
            shrink(fabChangeCallback)
            setOnClickListener {
                model.connection.new()
                findNavController().navigate(R.id.editDatabase)
            }
        }

        val addHttpButton =
            view.findViewById<ExtendedFloatingActionButton>(R.id.fab_client)?.apply {
                shrink(fabChangeCallback)
                setOnClickListener {
                    model.connection.new()
                    findNavController().navigate(R.id.editDatabase)
                }
            }


        var shown = false
        view.findViewById<FloatingActionButton>(R.id.fab)?.apply {
            setOnClickListener {
                if (!shown) {
                    setImageResource(R.drawable.ic_baseline_clear_24)
                    addDbButton?.show(fabChangeCallback)
                    addHttpButton?.show(fabChangeCallback)
                } else {
                    setImageResource(R.drawable.ic_baseline_add_24)
                    addDbButton?.shrink(fabChangeCallback)
                    addHttpButton?.shrink(fabChangeCallback)
                }
                shown = !shown
            }
        }
    }

    private fun click(conn: ConnInfo) {
        val action = when (conn.type) {
            ConnType.Database -> R.id.openDatabase
            //ConnType.Http -> R.id.action_ConnectionFragment_to_RestFragment
            else -> throw Exception("Not implemented click on " + conn.type.name)
        }

        val bundle = bundleOf("uuid" to conn.uuid)

        findNavController().navigate(action, bundle)
    }

    private fun longClick(conn: ConnInfo) {
        model.connection.edit(conn)
        findNavController().navigate(R.id.editDatabase)
    }

    inner class Adapter : ListAdapter<ConnInfo, Adapter.ViewHolder>(UuidDiffUtil(ConnInfo::class)) {
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

            private val tvName: TextView? = itemView.findViewById(R.id.tv_name)
            private val tvHostDBName: TextView? = itemView.findViewById(R.id.tv_host_dbname)
            lateinit var connInfo: ConnInfo

            init {
                itemView.setOnLongClickListener { longClick(connInfo); true }
                itemView.setOnClickListener { click(connInfo) }
            }

            @SuppressLint("SetTextI18n")
            fun bind(conn: ConnInfo) {
                connInfo = conn
                tvName?.text = conn.name
                tvHostDBName?.text = connInfo.type.name
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = layoutInflater.inflate(R.layout.item_connection, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(getItem(position))
        }
    }

    private val fabChangeCallback = object : ExtendedFloatingActionButton.OnChangedCallback() {
        override fun onShrunken(extendedFab: ExtendedFloatingActionButton?) {
            extendedFab?.hide()
        }

        override fun onShown(extendedFab: ExtendedFloatingActionButton?) {
            extendedFab?.extend()
        }
    }
}