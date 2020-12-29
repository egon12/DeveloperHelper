package com.egon12.developerhelper

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.egon12.developerhelper.database.persistent.Connection
import com.egon12.developerhelper.database.persistent.ConnectionDao
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.*

@ActivityScoped
class DatabaseViewModel @ViewModelInject constructor(
    private val connectionDao: ConnectionDao,
    private val dbFactory: DatabaseFactory
) : ViewModel(), LifecycleObserver {

    private val ioScope = CoroutineScope(Dispatchers.IO + Job())
    private val mainScope = CoroutineScope(Dispatchers.Main + Job())

    val error = MutableLiveData<java.lang.Exception>()

    var db: Database? = null

    val connections: LiveData<List<Connection>> by lazy { connectionDao.getAll() }

    var connectionInEdit: Connection = Connection.EMPTY

    fun connectToDatabase(connection: Connection) = liveData {
        _loadingStatus.postValue(true)
        emit(ConnectionStatus.Connecting)
        try {
            db = dbFactory.build(connection)
            emit(ConnectionStatus.Connected)
        } catch (e: java.lang.Exception) {
            emit(ConnectionStatus.Disconnected)
            handleError(e, "connect")
        } finally {
            _loadingStatus.postValue(false)
        }
    }

    private val _tables = MutableLiveData<List<Table>>()
    fun tables() = liveData {
        try {
            db?.getTables()?.let {
                Log.d(TAG, "getTables" + it.map { it.name }.joinToString())
                emit(it)
            }
            //emitSource(_tables)
        } catch (e: java.lang.Exception) {
            handleError(e, "loadTables")
        }
    }

    fun refreshTables() {
        viewModelScope.launch {
            try {
                db?.getTables()?.let { _tables.postValue(it) }
            } catch (e: java.lang.Exception) {
                handleError(e, "loadTables")
            }
        }
    }


    private val _data = MutableLiveData<Data>()
    val data: LiveData<Data> = _data


    fun loadData(t: Table) = ioScope.launch {
        val d = db?.getData(t)
        mainScope.launch { _data.value = d }
    }

    private val _row = MutableLiveData<List<Cell>>()
    val row: LiveData<List<Cell>> = _row
    fun loadRow(row: Row) {
        val cellList = _data.value?.columnDefinition?.mapIndexed() { idx, cd ->
            Cell.from(cd, row.cells[idx])
        } ?: emptyList()

        _row.value = cellList
    }

    fun updateRow(cells: List<Cell>) = ioScope.launch {
        try {
            val table = data.value?.table ?: throw Exception("Cannot edit multiple table")
            db?.update(table, cells)
        } catch (e: java.lang.Exception) {
            handleError(e, "updateRow")
        }
    }

    fun newConnection() {
        connectionInEdit = Connection.EMPTY
    }

    fun editConnection(conn: Connection) {
        connectionInEdit = conn
    }

    fun storeConnection(conn: Connection) {
        if (connectionInEdit == Connection.EMPTY) {
            ioScope.launch {
                try {
                    connectionDao.insertAll(conn)
                } catch (e: Exception) {
                    handleError(e, "storeConnection")
                }
            }
        }
    }

    private val _loadingStatus = MutableLiveData<Boolean>()
    val loadingStatus: LiveData<Boolean> = _loadingStatus

    private fun handleError(e: Exception, message: String) {
        Log.e(TAG, message, e)
        error.postValue(e)
    }

    companion object {
        const val TAG = "DatabaseViewModel"
    }
}