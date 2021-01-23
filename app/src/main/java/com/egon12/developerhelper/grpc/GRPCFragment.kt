package com.egon12.developerhelper.grpc

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.egon12.developerhelper.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch

class GRPCFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("GRPC", "kucing1")
        return inflater.inflate(R.layout.fragment_rest, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d("GRPC", "kucing")
        doSomething(view)

    }

    val uiScope = CoroutineScope(Main)

    fun doSomething(view: View) = CoroutineScope(IO).launch {

        val r = ReflectionService("192.168.100.7", 50051)
        val services = r.listService()
        Log.d("GRPC", services.joinToString())

        uiScope.launch {
            Snackbar.make(view, services.joinToString(), Snackbar.LENGTH_SHORT).show()
        }

        val s = r.getService(services[1])
        val m = s.methods[0]

        Log.d("GRPC", m)
        val f = s.requestFieldsFor(m)
        val i: Long = 1
        val msg = s.messageBuilderFor(m)
            .setField(f[0], i)
            .build()

        val res = s.call(m, msg)
        uiScope.launch {
            Snackbar.make(view, res.toString(), Snackbar.LENGTH_SHORT).show()
        }

        Log.d("GRPC", res.toString())
    }


}