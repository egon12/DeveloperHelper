package com.egon12.developerhelper.root

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
import com.egon12.developerhelper.R
import com.egon12.developerhelper.UuidDiffUtil
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class ConnectionFragment : Fragment() {

    private val model: ConnectionViewModel by activityViewModels()

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

        model.list.observe(viewLifecycleOwner) { listAdapter.submitList(it) }
        model.navigate.observe(viewLifecycleOwner, this::navigate)

        val addDbButton = view.findViewById<ExtendedFloatingActionButton>(R.id.fab_db)?.apply {
            shrink(fabChangeCallback)
            setOnClickListener { model.newDatabase() }
        }

        val addHttpButton =
            view.findViewById<ExtendedFloatingActionButton>(R.id.fab_http)?.apply {
                shrink(fabChangeCallback)
                setOnClickListener { model.newHttp() }
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


    private fun navigate(arg: Pair<Int, UUID?>?) {
        if (arg == null) {
            return
        }

        val (action, uuid) = arg
        val bundle = bundleOf("uuid" to uuid)
        findNavController().navigate(action, bundle)
        model.navigateDone()
    }

    inner class Adapter : ListAdapter<ConnInfo, Adapter.ViewHolder>(UuidDiffUtil(ConnInfo::class)) {
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

            private val tvName: TextView? = itemView.findViewById(R.id.tv_name)
            private val tvHostDBName: TextView? = itemView.findViewById(R.id.tv_host_dbname)
            lateinit var connInfo: ConnInfo

            init {
                itemView.setOnLongClickListener { model.edit(connInfo); true }
                itemView.setOnClickListener { model.open(connInfo) }
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
