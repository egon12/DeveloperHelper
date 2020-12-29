package com.egon12.developerhelper.database.viewmodel

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.egon12.developerhelper.database.ConnectionStatus
import com.egon12.developerhelper.database.Database
import com.egon12.developerhelper.database.DatabaseFactory
import com.egon12.developerhelper.database.persistent.Connection
import com.egon12.developerhelper.database.persistent.ConnectionDao
import dagger.hilt.android.scopes.ActivityScoped

@ActivityScoped
class DatabaseViewModel @ViewModelInject constructor(
    connectionDao: ConnectionDao,
    private val dbFactory: DatabaseFactory
) : ViewModel(), LifecycleObserver {

    val error = MutableLiveData<java.lang.Exception>()

    private lateinit var db: Database

    val connection = ConnectionViewModel(connectionDao, viewModelScope, this::handleError)

    val table = TableViewModel(viewModelScope)

    val data = DataViewModel(viewModelScope, this::handleError)

    fun connectToDatabase(connection: Connection) = liveData {
        _loadingStatus.postValue(true)
        emit(ConnectionStatus.Connecting)
        try {
            db = dbFactory.build(connection)
            table.db = db
            data.db = db
            emit(ConnectionStatus.Connected)
        } catch (e: java.lang.Exception) {
            emit(ConnectionStatus.Disconnected)
            handleError(e, "connect")
        } finally {
            _loadingStatus.postValue(false)
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