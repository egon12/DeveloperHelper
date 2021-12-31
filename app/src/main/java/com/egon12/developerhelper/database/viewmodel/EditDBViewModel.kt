package com.egon12.developerhelper.database.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.egon12.developerhelper.ConnInfo
import com.egon12.developerhelper.ConnInfoDao
import com.egon12.developerhelper.ConnType
import com.egon12.developerhelper.database.persistent.DBConnInfo
import com.egon12.developerhelper.database.persistent.DBType
import com.egon12.developerhelper.database.persistent.DatabaseDao
import dagger.hilt.android.scopes.ActivityScoped
import dagger.hilt.android.scopes.FragmentScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

@FragmentScoped
class EditDBViewModel @ViewModelInject constructor(
    private val connInfoDao: ConnInfoDao,
    private val databaseDao: DatabaseDao,
) : ViewModel() {

    private val ioScope = CoroutineScope(Dispatchers.IO)
    private var isNew = false

    private val objectEdit = MutableLiveData<Pair<ConnInfo, DBConnInfo>>()
    val edit: LiveData<Pair<ConnInfo, DBConnInfo>> = objectEdit

    fun start(uuid: UUID?) {
        if (uuid == null) {
            isNew = true
            objectEdit.postValue(createData())
            return
        }
        isNew = false
        loadData(uuid)
    }

    fun save(c: ConnInfo, d: DBConnInfo) {
        ioScope.launch {
            if (isNew) {
                connInfoDao.insert(c)
                databaseDao.insert(d)
            } else {
                connInfoDao.update(c)
                databaseDao.update(d)
            }
        }
    }

    private fun loadData(uuid: UUID) {
        ioScope.launch {
            val data = loadDataReal(uuid)
            objectEdit.postValue(data)

        }
    }

    private suspend fun loadDataReal(uuid: UUID): Pair<ConnInfo, DBConnInfo> {
        val c = connInfoDao.find(uuid)
        val d = databaseDao.find(uuid)
        return c to d
    }

    private fun createData(): Pair<ConnInfo, DBConnInfo> {
        val c = ConnInfo(name = "", type = ConnType.Database)
        val d = DBConnInfo(uuid = c.uuid, type = DBType.Postgre)
        return c to d
    }
}