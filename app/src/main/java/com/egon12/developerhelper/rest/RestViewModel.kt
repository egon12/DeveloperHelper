package com.egon12.developerhelper.rest

import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.egon12.developerhelper.rest.persistent.HttpRequest
import com.egon12.developerhelper.rest.persistent.HttpRequestDao
import com.egon12.developerhelper.rest.persistent.VariablesDao
import kotlinx.coroutines.launch
import java.util.Collections.emptyMap

class RestViewModel @ViewModelInject constructor(
    private val client: RestClient,
    private val httpRequestDao: HttpRequestDao,
    private val variablesDao: VariablesDao
) : ViewModel() {

    val Load: Int = -1
    val Done: Int = 0

    val requests by lazy { httpRequestDao.all() }

    fun saveRequest(req: HttpRequest) = liveData {
        emit(Load)
        try {
            if (req.id > 0) {
                httpRequestDao.update(req)
            } else {
                httpRequestDao.insert(req)
            }
        } catch (e: Exception) {
            handleError(e, "saveRequest")
        } finally {
            emit(Done)
        }
    }

    fun editRequest(req: HttpRequest?) = liveData {
        if (req == null) emit(HttpRequest.EMPTY)
        else emit(req)
    }


    val variables by lazy { variablesDao.all() }

    private val _response = MutableLiveData<Response>()
    val response: LiveData<Response> = _response

    private val _error = MutableLiveData<Exception>()
    val error: LiveData<Exception> = _error

    fun request(method: String, url: String) = viewModelScope.launch {
        try {
            val res = client.request(method, url, emptyMap(), null)
            _response.postValue(res)
        } catch (e: Exception) {
            handleError(e, "request")
        }
    }

    private fun handleError(e: Exception, message: String) {
        Log.e(TAG, message, e)
        _error.postValue(e)
    }

    companion object {
        const val TAG = "RestViewModel"
    }

}
