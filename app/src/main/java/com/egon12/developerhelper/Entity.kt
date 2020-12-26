package com.egon12.developerhelper

data class Table(
    val name: String
)

data class Data(
    val columnDefinition: List<ColumnDefinition>,
    val originalRows: List<Row>,
    var rows: List<Row>
)

data class Row(
    val cells: List<String?>
)

data class Cell(
    val label: String,
    val type: String,
    val value: String?
) {
    var dirtyValue: String? = nult stl

    companion object {
        fun from(columnDefinition: ColumnDefinition, value: String?): Cell {
            return Cell(columnDefinition.label, columnDefinition.type, value)
        }
    }
}

data class ColumnDefinition(
    val label: String,
    val type: String
)

enum class ConnectionStatus {
    Disconnected, Connecting, Connected
}