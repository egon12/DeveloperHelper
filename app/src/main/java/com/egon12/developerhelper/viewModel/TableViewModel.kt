package com.egon12.developerhelper.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.egon12.developerhelper.Database
import com.egon12.developerhelper.Table
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class TableViewModel(private val scope: CoroutineScope) {

    var db: Database? = null

    private val _data = MutableLiveData<List<Table>>()
    val data: LiveData<List<Table>> = _data

    private var allTables: List<Table> = emptyList()

    fun search(s: String) {
        _data.postValue(allTables.filter { it.name.contains(s) })
    }

    fun reload() {
        scope.launch {
            allTables = db?.getTables() ?: emptyList()
            _data.postValue(allTables)
        }
    }
}