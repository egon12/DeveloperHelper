package com.egon12.developerhelper.root

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.egon12.developerhelper.ConnInfo
import com.egon12.developerhelper.ConnInfoDao
import com.egon12.developerhelper.ConnType.*
import com.egon12.developerhelper.R.id.*
import dagger.hilt.android.scopes.ActivityScoped
import java.util.*

@ActivityScoped
class ConnectionViewModel @ViewModelInject constructor(
    private val connectionDao: ConnInfoDao,
) : ViewModel() {

    val list by lazy {
        try {
            connectionDao.all()
        } catch (e: Exception) {
            handleError(e, "GetConnectionInfo")
            MutableLiveData(emptyList())
        }
    }

    val navigate = MutableLiveData<Pair<Int, UUID?>?>()

    val error = MutableLiveData<Exception>()

    fun newDatabase() {
        navigate.postValue(editDatabase to null)
    }

    fun newHttp() {
        navigate.postValue(editHttp to null)
    }

    fun newGraphQL() {
        navigate.postValue(editGraphQL to null)
    }

    fun newGRPC() {
        navigate.postValue(editGRPC to null)
    }

    fun edit(conn: ConnInfo) {
        val action = when (conn.type) {
            Database -> editDatabase
            Http -> editHttp
            GraphQL -> editGraphQL
            GRPC -> editGRPC
        }
        navigate.postValue(action to conn.uuid)
    }

    fun open(conn: ConnInfo) {
        val action = when (conn.type) {
            Database -> openDatabase
            Http -> openHttp
            GraphQL -> openGraphQL
            GRPC -> openGRPC
        }
        navigate.postValue(action to conn.uuid)
    }

    private fun handleError(e: Exception, @Suppress("SameParameterValue") message: String) {
        Log.e(TAG, message, e)
        error.postValue(e)
    }

    fun navigateDone() {
        navigate.value = null
    }

    companion object {
        const val TAG = "ConnectionViewModel"
    }
}
