package com.egon12.developerhelper.rest

import com.egon12.developerhelper.R

class CollectionTreeNode(
    val item: Collection.Item,
    val level: Int,
    private val children: List<CollectionTreeNode> = emptyList(),
    var isExpand: Boolean = false,
    var selected: Boolean = false,
) {

    // Ok, please don't change this
    var id: Long = 0

    fun setIds(nextId: Long): Long {
        id = nextId
        var nId = nextId + 1
        children.forEach { nId = it.setIds(nId) }
        return nId
    }

    private var parent: CollectionTreeNode? = null

    init {
        children.forEach { it.parent = this }
    }

    private fun clearRootMemo() {
        if (parent == null) {
            memo.clear()
            return
        }
        parent!!.clearRootMemo()
    }

    private val memo = mutableMapOf<Int, CollectionTreeNode>()

    fun get(pos: Int): CollectionTreeNode? {
        if (memo[pos] == null) {
            val (_, result) = internalGet(pos)
            result?.let { memo.put(pos, it) }
        }
        return memo[pos]
    }

    fun getViewType(): Int {
        return when (item) {
            is Collection.Item.Folder -> level * 10 + 1
            is Collection.Item.RequestItem -> level * 10 + 2
        }
    }

    private fun internalGet(pos: Int): Pair<Int, CollectionTreeNode?> {
        if (pos == 0) {
            return Pair(0, this)
        }

        var nPos: Int = pos - 1
        if (isExpand) {
            children.forEach {
                val (n, result) = it.internalGet(nPos)
                if (n == 0 && result != null) return Pair(n, result)
                nPos = n
            }
        }

        return Pair(nPos, null)
    }

    fun count(): Int {
        var sum = 1
        if (isExpand) {
            children.forEach { sum += it.count() }
        }
        return sum
    }

    fun collapse() {
        isExpand = false
        clearRootMemo()
    }

    fun expand() {
        isExpand = true
        clearRootMemo()
    }

    fun expandToLevel(level: Int) {
        if (level == 0) {
            isExpand = false
            return
        }

        isExpand = true
        children.forEach { it.expandToLevel(level - 1) }
    }

    companion object {

        fun from(col: Collection): CollectionTreeNode {
            // hack to get one root
            val rootItem = Collection.Item.Folder("root", col.item)
            val root = CollectionTreeNode(rootItem, -1, from(col.item, 1))
            root.expand()
            root.setIds(1)
            return root
        }

        fun from(items: List<Collection.Item>, level: Int): List<CollectionTreeNode> {
            return items.map {
                when (it) {
                    is Collection.Item.Folder -> CollectionTreeNode(
                        it,
                        level,
                        from(it.item, level + 1)
                    )
                    is Collection.Item.RequestItem -> CollectionTreeNode(it, level)
                }
            }
        }

        private fun getLayoutId(viewType: Int): Int = when (viewType % 10) {
            1 -> R.layout.item_rest_folder
            2 -> R.layout.item_rest_request
            else -> throw RuntimeException("Cannot find layoutId from viewType $viewType")
        }

        fun getLevel(viewType: Int): Int = viewType / 10

        fun inflateData(viewType: Int) = Pair(getLayoutId(viewType), getLevel(viewType))
    }
}
