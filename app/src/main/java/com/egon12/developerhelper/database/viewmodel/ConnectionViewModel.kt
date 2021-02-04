package com.egon12.developerhelper.database.viewmodel

import androidx.lifecycle.MutableLiveData
import com.egon12.developerhelper.ConnInfo
import com.egon12.developerhelper.ConnInfoDao
import com.egon12.developerhelper.ConnType
import com.egon12.developerhelper.database.persistent.DBConnInfo
import com.egon12.developerhelper.database.persistent.DBType
import com.egon12.developerhelper.database.persistent.DatabaseDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ConnectionViewModel(
    private val connectionDao: ConnInfoDao,
    private val databaseDao: DatabaseDao,
    private val scope: CoroutineScope,
    private val handleError: (Exception, String) -> Unit
) {

    val onEdit = MutableLiveData<ConnInfo>()

    val dbOnEdit = MutableLiveData<DBConnInfo>()

    private var isNew = false

    init {
        new()
    }

    val list by lazy {
        try {
            connectionDao.all()
        } catch (e: Exception) {
            handleError(e, "GetConnectionInfo")
            MutableLiveData(emptyList())
        }
    }


    fun new() {
        isNew = true
        val conn = ConnInfo(name = "", type = ConnType.Database)
        onEdit.value = conn
        dbOnEdit.value = DBConnInfo(conn.uuid, DBType.MySQL)
    }

    fun edit(conn: ConnInfo) {
        isNew = false
        scope.launch {
            onEdit.value = conn
            if (conn.type == ConnType.Database) {
                dbOnEdit.postValue(databaseDao.find(conn.uuid))
            }
        }
    }

    fun save(conn: ConnInfo?, dbConn: DBConnInfo?) {
        scope.launch {
            try {
                if (isNew) {
                    conn?.let { connectionDao.insert(it) }
                    dbConn?.let { databaseDao.insert(it) }
                } else {
                    conn?.let { connectionDao.update(it) }
                    dbConn?.let { databaseDao.update(it) }
                }
            } catch (e: Exception) {
                handleError(e, "SaveConnectionInfo")
            }
        }
    }
}