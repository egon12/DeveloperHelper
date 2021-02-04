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

    val collection = liveData {
        emit(Collection.parse(str))
    }
}


val str = """
            {
                "info": {
                    "_postman_id": "12345",
                    "name": "Somekind of something V2",
                    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
                },
                "item": [
                    {
                        "name": "Group1",
                        "item": [
                            {
                                "name": "Group2",
                                "item": [{
                                    "name":"Request1",
                                    "request": {
										"method": "GET",
										"header": [
											{
												"key": "Accept",
												"value": "*"
											},
											{
												"key": "Authorization",
												"value": "Bearer {{intools-token}}"
											},
											{
												"key": "Referer",
												"value": "https://www.tokopedia.com",
												"type": "text"
											}
										],
										"url": {
											"raw": "{{host}}/v1/something?query1=1&query2=2",
											"host": [
												"{{host}}"
											],
											"path": [
												"v1",
												"something",
												"section"
											],
											"query": [
												{
													"key": "query1",
													"value": "1"
												},
												{
													"key": "query2",
													"value": "2"
												}
											]
										},
                                        "body": {
                                            "mode": "raw",
                                            "raw": "{\"hello\":\"world\"}"
                                        }
                                    }
                                }]
                            },
                            {
                                "name": "Group2 (Copy)",
                                "item": [{
                                    "name":"Request1",
                                    "request": {
										"method": "GET",
										"header": [
											{
												"key": "Accept",
												"value": "*"
											},
											{
												"key": "Authorization",
												"value": "Bearer {{intools-token}}"
											},
											{
												"key": "Referer",
												"value": "https://www.tokopedia.com",
												"type": "text"
											}
										],
										"url": {
											"raw": "{{host}}/v1/something?query1=1&query2=2",
											"host": [
												"{{host}}"
											],
											"path": [
												"v1",
												"something",
												"section"
											],
											"query": [
												{
													"key": "query1",
													"value": "1"
												},
												{
													"key": "query2",
													"value": "2"
												}
											]
										},
                                        "body": {
                                            "mode": "raw",
                                            "raw": "{\"hello\":\"world\"}"
                                        }
                                    }
                                }]
                            }
                        ]
                    }]
                }
        """.trimIndent()

