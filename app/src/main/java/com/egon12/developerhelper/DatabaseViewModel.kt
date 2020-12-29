package com.egon12.developerhelper

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.egon12.developerhelper.database.persistent.Connection
import com.egon12.developerhelper.database.persistent.ConnectionDao
import com.egon12.developerhelper.viewModel.TableViewModel
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.launch

@ActivityScoped
class DatabaseViewModel @ViewModelInject constructor(
    private val connectionDao: ConnectionDao,
    private val dbFactory: DatabaseFactory
) : ViewModel(), LifecycleObserver {

    val error = MutableLiveData<java.lang.Exception>()

    lateinit var db: Database

    val connections: LiveData<List<Connection>> by lazy { connectionDao.getAll() }

    var connectionInEdit: Connection = Connection.EMPTY

    val table = TableViewModel(viewModelScope)

    fun connectToDatabase(connection: Connection) = liveData {
        _loadingStatus.postValue(true)
        emit(ConnectionStatus.Connecting)
        try {
            db = dbFactory.build(connection)
            table.db = db
            emit(ConnectionStatus.Connected)
        } catch (e: java.lang.Exception) {
            emit(ConnectionStatus.Disconnected)
            handleError(e, "connect")
        } finally {
            _loadingStatus.postValue(false)
        }
    }

    private val _data = MutableLiveData<Data>()
    val data: LiveData<Data> = _data


    fun loadData(t: Table) = viewModelScope.launch {
        try {
            db?.getData(t)?.let { _data.postValue(it) }
        } catch (e: java.lang.Exception) {
            handleError(e, "loadData")
        }
    }

    private val _row = MutableLiveData<List<Cell>>()
    val row: LiveData<List<Cell>> = _row
    fun loadRow(row: Row) {
        val cellList = _data.value?.columnDefinition?.mapIndexed() { idx, cd ->
            Cell.from(cd, row.cells[idx])
        } ?: emptyList()

        _row.value = cellList
    }

    fun updateRow(cells: List<Cell>) = viewModelScope.launch {
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
        viewModelScope.launch {
            try {
                if (connectionInEdit == Connection.EMPTY) {
                    connectionDao.insertAll(conn)
                } else {
                    connectionDao.update(conn)
                }
            } catch (e: Exception) {
                handleError(e, "storeConnection")
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