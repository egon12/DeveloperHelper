package com.egon12.developerhelper.rest

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.os.Bundle
import android.util.JsonReader
import android.util.JsonToken
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.egon12.developerhelper.ConnInfo
import com.egon12.developerhelper.ConnInfoDao
import com.egon12.developerhelper.ConnType
import com.egon12.developerhelper.R
import com.egon12.developerhelper.rest.persistent.RequestGroup
import com.egon12.developerhelper.rest.persistent.RequestGroupDao
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.scopes.FragmentScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.*

class EditHttpFragment : Fragment() {
    private val model: EditHttpViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_http_conn_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadJsonButton = view.findViewById(R.id.btn_load_json)
        loadJsonButton.setOnClickListener {
            val intent = Intent().apply {
                action = ACTION_GET_CONTENT
                type = "*/*"
            }

            startActivityForResult(intent, COLLECTION_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != COLLECTION_REQUEST_CODE) {
            return
        }

        if (resultCode != RESULT_OK) {
            return
        }

        val uri = data?.data ?: return
        val env =  mutableMapOf<String, String>()

        val inputStream = activity?.contentResolver?.openInputStream(uri) ?: return
        val reader = JsonReader(inputStream.reader())
        reader.beginObject()
        while(reader.peek() == JsonToken.NAME) {
            env[reader.nextName()] = reader.nextString()
        }
        reader.close()
    }

    lateinit var loadJsonButton: MaterialButton

    companion object {
        private const val TAG = "EditHttpFragment"
        private const val COLLECTION_REQUEST_CODE = 0x71
    }

}

@FragmentScoped
class EditHttpViewModel @ViewModelInject constructor(
    private val connInfoDao: ConnInfoDao,
    private val requestGroupDao: RequestGroupDao,
    private val restClient: RestClient,
) : ViewModel() {

    private var mConnInfo = MutableLiveData<ConnInfo>()
    val connInfo: LiveData<ConnInfo> = mConnInfo

    private var mRequestGroup = MutableLiveData<RequestGroup>()
    val requestGroup: LiveData<RequestGroup> = mRequestGroup

    private var ioScope = CoroutineScope(Dispatchers.IO)

    private var isNew = false
    fun start(uuid: UUID?) {
        if (uuid == null) {
            isNew = true
            createData()
            return
        }

        isNew = false
        loadData(uuid)
    }

    fun setTitle(title: String) {
        val c = mConnInfo.value ?: throw Exception("edit before exists")
        val r = mRequestGroup.value ?: throw Exception("edit before exists")
        c.name = title
        pub(c, r)
    }

    fun setCollection(col: Collection) {
        val c = mConnInfo.value ?: throw Exception("edit before exists")
        val r = mRequestGroup.value ?: throw Exception("edit before exists")
        r.setCollection(col)
        pub(c, r)
    }

    var env = mutableMapOf<String, String>()
    fun addEnv(key: String, value: String) {
        env[key] = value
    }

    fun saveEnv() {
        val jsonValue = buildJsonObject {
            env.forEach { t, u -> put(t, u) }
        }.toString()
        val c = mConnInfo.value ?: throw Exception("edit before exists")
        val r = mRequestGroup.value ?: throw Exception("edit before exists")
        r.environmentsRaw = jsonValue
        pub(c, r)


    }

    fun save(c: ConnInfo, r: RequestGroup) {
        ioScope.launch {
            if (isNew) {
                connInfoDao.insert(c)
                requestGroupDao.insert(r)
            } else {
                connInfoDao.update(c)
                requestGroupDao.update(r)

            }
        }

    }

    private fun createData() {
        val uuid = UUID.randomUUID()
        pub(
            ConnInfo(uuid = uuid, type = ConnType.Http, name = "New Request Group"),
            RequestGroup(uuid = uuid),
        )
    }

    private fun loadData(uuid: UUID) {
        ioScope.launch {
            pub(
                connInfoDao.find(uuid),
                requestGroupDao.find(uuid),
            )
        }
    }


    private fun pub(c: ConnInfo, r: RequestGroup) {
        mConnInfo.postValue(c)
        mRequestGroup.postValue(r)
    }

    private var requestJob: Job? = null
    fun call(requestItem: Collection.Item.RequestItem) {
        val req = RestClientRequest.from(requestItem.request, env)

        requestJob?.cancel()
        requestJob = ioScope.launch { restClient.request(req) }
    }


}
