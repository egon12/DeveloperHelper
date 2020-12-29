package com.egon12.developerhelper.database.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.egon12.developerhelper.database.Database
import com.egon12.developerhelper.database.Table
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class TableViewModel(private val scope: CoroutineScope) {

    var db: Database? = null

    private val _data = MutableLiveData<List<Table>>()
    val data: LiveData<List<Table>> = _data

    private var allTables: List<Table> = emptyList()

    fun search(s: String) {
        val cleanS = s.trim().toLowerCase()
        _data.postValue(allTables.filter { it.name.toLowerCase().contains(cleanS) })
    }

    fun reload() {
        scope.launch {
            allTables = db?.getTables() ?: emptyList()
            _data.postValue(allTables)
        }
    }
}