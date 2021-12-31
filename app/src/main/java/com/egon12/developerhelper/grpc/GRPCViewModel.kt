package com.egon12.developerhelper.grpc

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.egon12.developerhelper.rest.Collection

class GRPCViewModel : ViewModel() {

    private val mutableServerList = MutableLiveData<Server>()
    val serverList: LiveData<Server> = mutableServerList

    private val mutableServiceList = MutableLiveData<Service>()
    val serviceList: LiveData<Service> = mutableServiceList

    private val mutableMethodList = MutableLiveData<Collection.Method>()
    val methodList: LiveData<Collection.Method> = mutableMethodList

    private val mutableCallList = MutableLiveData<Call>()
    val callList = mutableCallList


    fun loadCall(call: Call) {

    }

    fun loadServer(server: Server) {

    }

    fun loadService(service: Service) {

    }

    fun loadMethod(method: Collection.Method) {

    }

}
