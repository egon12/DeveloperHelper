package com.egon12.developerhelper

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import java.util.*

class MainViewModel @ViewModelInject constructor(
    private val connInfoDao: ConnInfoDao
) : ViewModel() {

    val list: LiveData<List<ConnInfo>> by lazy { connInfoDao.all() }

    fun edit(uuid: UUID?): LiveData<ConnType> {
        if (uuid == null) {
            // TODO show error
            return MutableLiveData()
        }
        return internalEdit(uuid)
    }

    private fun internalEdit(uuid: UUID) = liveData {
        try {
            val conn = connInfoDao.find(uuid)
            emit(conn.type)
        } catch (e: Exception) {
            // TODO show error
        }


    }
}