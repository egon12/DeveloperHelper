package com.egon12.developerhelper.database

import androidx.recyclerview.widget.DiffUtil
import com.egon12.developerhelper.ConnInfo

class TableDiffCallback : DiffUtil.ItemCallback<Table>() {
    override fun areItemsTheSame(oldItem: Table, newItem: Table): Boolean =
        oldItem.name == newItem.name

    override fun areContentsTheSame(oldItem: Table, newItem: Table): Boolean = oldItem == newItem
}

class CellDiffUtil : DiffUtil.ItemCallback<Cell>() {
    override fun areItemsTheSame(oldItem: Cell, newItem: Cell): Boolean = oldItem == newItem

    override fun areContentsTheSame(oldItem: Cell, newItem: Cell): Boolean = oldItem == newItem
}


class ConnInfoDiffUtil : DiffUtil.ItemCallback<ConnInfo>() {
    override fun areItemsTheSame(o: ConnInfo, n: ConnInfo): Boolean = o.uuid == n.uuid

    override fun areContentsTheSame(o: ConnInfo, n: ConnInfo): Boolean = o == n
}
