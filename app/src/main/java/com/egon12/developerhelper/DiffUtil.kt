package com.egon12.developerhelper

import androidx.recyclerview.widget.DiffUtil
import com.egon12.developerhelper.database.persistent.Connection

class TableDiffCallback : DiffUtil.ItemCallback<Table>() {
    override fun areItemsTheSame(oldItem: Table, newItem: Table): Boolean =
        oldItem.name == newItem.name

    override fun areContentsTheSame(oldItem: Table, newItem: Table): Boolean = oldItem == newItem
}

class CellDiffUtil : DiffUtil.ItemCallback<Cell>() {
    override fun areItemsTheSame(oldItem: Cell, newItem: Cell): Boolean = oldItem == newItem

    override fun areContentsTheSame(oldItem: Cell, newItem: Cell): Boolean = oldItem == newItem
}


class ConnectionDiffUtil : DiffUtil.ItemCallback<Connection>() {
    override fun areItemsTheSame(oldItem: Connection, newItem: Connection): Boolean =
        oldItem.name == newItem.name

    override fun areContentsTheSame(oldItem: Connection, newItem: Connection): Boolean =
        oldItem == newItem
}
