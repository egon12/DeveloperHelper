package com.egon12.developerhelper.database.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.egon12.developerhelper.database.persistent.Connection
import com.egon12.developerhelper.database.persistent.ConnectionDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ConnectionViewModel(
    private val connectionDao: ConnectionDao,
    private val scope: CoroutineScope,
    private val handleError: (Exception, String) -> Unit
) {

    val list: LiveData<List<Connection>> by lazy {
        try {
            connectionDao.getAll()
        } catch (e: Exception) {
            handleError(e, "GetConnectionInfo")
            MutableLiveData<List<Connection>>(emptyList())
        }
    }

    var onEdit: Connection = Connection.EMPTY

    fun new() {
        onEdit = Connection.EMPTY
    }

    fun edit(conn: Connection) {
        onEdit = conn
    }

    fun save(conn: Connection) {
        scope.launch {
            try {
                if (onEdit == Connection.EMPTY) connectionDao.insertAll(conn)
                else connectionDao.update(conn)
            } catch (e: Exception) {
                handleError(e, "SaveConnectionInfo")
            }
        }
    }
}