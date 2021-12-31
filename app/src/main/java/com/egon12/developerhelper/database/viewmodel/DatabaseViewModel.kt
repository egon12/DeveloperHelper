package com.egon12.developerhelper.database.viewmodel

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.egon12.developerhelper.ConnInfoDao
import com.egon12.developerhelper.database.ConnectionStatus
import com.egon12.developerhelper.database.Database
import com.egon12.developerhelper.database.DatabaseFactory
import com.egon12.developerhelper.database.persistent.DBConnInfo
import com.egon12.developerhelper.database.persistent.DatabaseDao
import dagger.hilt.android.scopes.ActivityScoped
import java.util.*

@ActivityScoped
class DatabaseViewModel @ViewModelInject constructor(
    private val connectionDao: ConnInfoDao,
    private val databaseDao: DatabaseDao,
    private val dbFactory: DatabaseFactory,
) : ViewModel(), LifecycleObserver {

    val error = MutableLiveData<Exception>()

    private lateinit var db: Database

    val connection =
        ConnectionViewModel(connectionDao, databaseDao, viewModelScope, this::handleError)

    val table = TableViewModel(viewModelScope)

    val data = DataViewModel(viewModelScope, this::handleError)

    fun connect(uuid: UUID) = liveData {
        _loadingStatus.postValue(true)
        emit(ConnectionStatus.Connecting)

        val connection = databaseDao.find(uuid)
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


    fun connectToDatabase(connection: DBConnInfo) = liveData {
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

    fun start(uuid: UUID?) {
        TODO("Not yet implemented")
    }

    companion object {
        const val TAG = "DatabaseViewModel"
    }
}