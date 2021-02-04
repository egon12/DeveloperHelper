package com.egon12.developerhelper.rest

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.egon12.developerhelper.R

class CollectionAdapter : RecyclerView.Adapter<CollectionAdapter.ViewHolder>() {

    lateinit var callRequest: (Collection.Item.RequestItem) -> Unit


    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_name)
        private val ivCaret: ImageView? = itemView.findViewById(R.id.iv_caret)

        private var node: CollectionTreeNode? = null
        private var pos: Int = 1

        init {
            itemView.setOnClickListener {
                val n = node ?: return@setOnClickListener
                when (n.item) {
                    is Collection.Item.Folder -> clickFolder(n, pos)
                    is Collection.Item.RequestItem -> clickRequestItem(n, pos)
                }
            }
        }

        fun bind(node: CollectionTreeNode?, position: Int) {
            this.node = node
            this.pos = position

            if (node == null) {
                return
            }

            tvName.text = node.item.name

            if (node.item is Collection.Item.Folder) {
                val resId =
                    if (node.isExpand) R.drawable.ic_baseline_expand_more_24
                    else R.drawable.ic_baseline_navigate_next_24
                ivCaret?.setImageResource(resId)
            } else if (node.item is Collection.Item.RequestItem) {
                Log.d("BOOM", "$pos: ${node.selected}")
                if (node.selected) {
                    itemView.setBackgroundColor(Color.LTGRAY)
                } else {
                    itemView.setBackgroundColor(Color.TRANSPARENT)
                }
            }
        }
    }

    lateinit var colTreeNode: CollectionTreeNode

    fun setCollection(col: Collection) {
        colTreeNode = CollectionTreeNode.from(col)
    }

    var requestClicked: CollectionTreeNode? = null
    private fun clickRequestItem(it: CollectionTreeNode, pos: Int) {
        val oldPos = findPosForRequestClicked()
        requestClicked?.selected = false
        requestClicked = it
        requestClicked?.selected = true
        notifyItemChanged(oldPos)
        notifyItemChanged(pos)
        callRequest(it.item as Collection.Item.RequestItem)
    }

    private fun clickFolder(it: CollectionTreeNode, pos: Int) {
        if (it.isExpand) {
            val count = it.count()
            it.collapse()
            notifyItemRangeRemoved(pos + 1, count - 1)
            notifyItemChanged(pos)
        } else {
            it.expand()
            it.count()
            notifyItemRangeInserted(pos + 1, it.count() - 1)
            notifyItemChanged(pos)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val (layoutId, level) = CollectionTreeNode.inflateData(viewType)
        val v = layoutInflater.inflate(layoutId, parent, false)
        v.setPadding(
            v.paddingLeft * level,
            v.paddingTop,
            v.paddingRight,
            v.paddingBottom
        )
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(colTreeNode.get(position + 1), position)
    }

    override fun getItemId(position: Int): Long = colTreeNode.get(position + 1)?.id ?: 0L

    override fun getItemCount(): Int = colTreeNode.count() - 1

    override fun getItemViewType(position: Int): Int =
        colTreeNode.get(position + 1)?.getViewType() ?: 0

    private fun findPosForRequestClicked(): Int {
        if (requestClicked == null) {
            return -1
        }

        (0..itemCount).forEach {
            if (colTreeNode.get(it) == requestClicked) {
                return it - 1
            }
        }

        return -1
    }
}