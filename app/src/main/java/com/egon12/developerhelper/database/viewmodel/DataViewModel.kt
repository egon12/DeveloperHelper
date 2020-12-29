package com.egon12.developerhelper.database.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.egon12.developerhelper.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class DataViewModel(
    private val scope: CoroutineScope,
    private val handleError: (Exception, String) -> Unit
) {

    lateinit var db: Database

    private val _data = MutableLiveData<Data>()
    val data: LiveData<Data> = _data

    private var isNew = false
    private val _cells = MutableLiveData<List<Cell>>()
    val cells: LiveData<List<Cell>> = _cells

    fun getData(table: Table) {
        scope.launch {
            try {
                _data.postValue(db.getData(table))
            } catch (e: Exception) {
                handleError(e, "getData")
            }
        }
    }

    fun query(query: String) {
        scope.launch {
            try {
                when {
                    query.contains("INSERT", true) -> db.execute(query)
                    query.contains("UPDATE", true) -> db.execute(query)
                    else -> db.query(query).let { _data.postValue(it) }
                }
            } catch (e: java.lang.Exception) {
                handleError(e, "loadData")
            }
        }
    }

    fun edit(row: Row) {
        data.value
            ?.columnDefinition
            ?.mapIndexed { idx, cd -> Cell.from(cd, row.cells[idx]) }
            ?.let { _cells.postValue(it) }

        isNew = false
    }

    fun new() {
        data.value
            ?.columnDefinition
            ?.map { Cell.from(it, "") }
            ?.let { _cells.postValue(it) }

        isNew = false
    }

    fun save(input: List<Cell>) = liveData {
        try {
            data.value
                ?.table
                ?.let {
                    if (isNew) db.insert(it, input)
                    else db.update(it, input)
                    getData(it)
                    emit(true)
                }
        } catch (e: java.lang.Exception) {
            handleError(e, "updateRow")
            emit(false)
        }
    }
}