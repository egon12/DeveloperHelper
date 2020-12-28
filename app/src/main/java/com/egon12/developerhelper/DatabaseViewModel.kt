package com.egon12.developerhelper

import android.util.Log
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.egon12.developerhelper.database.persistent.Connection
import com.egon12.developerhelper.database.persistent.ConnectionDao
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@ActivityScoped
class DatabaseViewModel @ViewModelInject constructor(
    private val connectionDao: ConnectionDao,
    @Assisted private val savedStateHandle: SavedStateHandle

) : ViewModel(), LifecycleObserver {

    private val ioScope = CoroutineScope(Dispatchers.IO + Job())
    private val mainScope = CoroutineScope(Dispatchers.Main + Job())

    val connections: LiveData<List<Connection>>
        get() = connectionDao.getAll()

    val dbFactory = DatabaseFactory()
    var db: Database? = null

    private val _tables = MutableLiveData<TablesResult>()
    val tables: LiveData<TablesResult> = _tables

    private val _data = MutableLiveData<Data>()
    val data: LiveData<Data> = _data

    private val _row = MutableLiveData<List<Cell>>()
    val row: LiveData<List<Cell>> = _row

    val error = MutableLiveData<java.lang.Exception>()

    var connectionInEdit: Connection = Connection.EMPTY

    fun connectToDatabase(connection: Connection) = liveData(Dispatchers.IO) {
        emit(ConnectionStatus.Connecting)
        try {
            db = dbFactory.build(connection)
            db?.connect()
            emit(ConnectionStatus.Connected)
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "Connect", e)
            emit(ConnectionStatus.Disconnected)
            mainScope.launch { error.value = e }
        }
    }


    fun loadTables() = ioScope.launch {
        setTablesResult(TablesResult.Loading())

        try {
            Log.d("Before GetTabl", db.hashCode().toString())
            db?.getTables()?.let {
                setTablesResult(TablesResult.Success(it))
            }
        } catch (e: java.lang.Exception) {
            setTablesResult(TablesResult.Error(e))
        }
    }

    fun loadData(t: Table) = ioScope.launch {
        val d = db?.getData(t)
        mainScope.launch { _data.value = d }
    }

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
            Log.e(TAG, "updateRow", e)
            mainScope.launch { error.value = e }
        }
    }

    private fun setTablesResult(r: TablesResult) = mainScope.launch { _tables.value = r }

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
                    mainScope.launch { error.value = e }
                }
            }
        }
    }

    sealed class TablesResult() {
        class Loading : TablesResult()
        class Error(val exception: Exception) : TablesResult()
        class Success(val table: List<Table>) : TablesResult()
    }

    companion object {
        const val TAG = "DatabaseViewModel"
    }
}