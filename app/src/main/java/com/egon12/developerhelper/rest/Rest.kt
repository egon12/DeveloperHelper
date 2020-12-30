package com.egon12.developerhelper.rest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.egon12.developerhelper.R
import com.egon12.developerhelper.rest.persistent.HttpRequest
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class Rest : Fragment() {

    private val model: RestViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_rest, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewPager = view.findViewById<ViewPager2>(R.id.pager)?.apply {
            adapter = RestFragmentsAdapter(this@Rest)
        }

        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout)
        TabLayoutMediator(tabLayout, viewPager!!) { tab, position ->
            tab.text = when (position) {
                0 -> "History"
                1 -> "Request"
                2 -> "Response"
                else -> ""
            }
        }.attach()


        val spinnerMethod = view.findViewById<AppCompatSpinner>(R.id.spinner_method)
        ArrayAdapter<String>(
            requireActivity(),
            android.R.layout.simple_spinner_item,
            arrayOf("GET", "POST", "PUT", "PATCH", "DELETE")
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerMethod.adapter = this
        }

        val editURL = view.findViewById<EditText>(R.id.edit_url).apply {
            setText("http://localhost:8080")
        }

        view.findViewById<FloatingActionButton>(R.id.btn_execute).apply {
            setOnClickListener {
                val url = editURL.text.toString()
                model.saveRequest(HttpRequest(0, url, "GET", url))
                    .observe(viewLifecycleOwner, Observer { })
            }

            /*
            setOnClickListener {
                val method = spinnerMethod.selectedItem as String
                val url = editURL.text.toString()
                model.request(method, url)
            }
             */
        }
    }

    inner class RestFragmentsAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

        //override fun getItemCount() = 5
        override fun getItemCount() = 3

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> HistoryFragment()
                1 -> RequestBodyFragment()
                2 -> ResponseFragment()
                else -> HistoryFragment()
            }
        }
    }
}

abstract class AutoInflateLayoutFragment(val layoutId: Int) : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(layoutId, container, false)
    }

}

class HistoryFragment : AutoInflateLayoutFragment(R.layout.fragment_rest_history)

class RequestBodyFragment : AutoInflateLayoutFragment(R.layout.fragment_rest_request) {

    val model by activityViewModels<RestViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listAdapter = RequestAdapter()
        model.requests.observe(viewLifecycleOwner, Observer { listAdapter.submitList(it) })

        view.findViewById<RecyclerView>(R.id.rv_request).apply {
            layoutManager = LinearLayoutManager(view.context)
            adapter = listAdapter
        }
    }

    inner class RequestAdapter :
        ListAdapter<HttpRequest, RequestAdapter.ViewHolder>(RequestDiffUtil()) {

        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            private val tName = itemView.findViewById<TextView>(R.id.text_name)
            private val tURL = itemView.findViewById<TextView>(R.id.text_url)

            init {
                itemView.setOnClickListener { req?.let { model.editRequest(it) } }
                itemView.findViewById<ImageView>(R.id.btn_execute).apply {
                    setOnClickListener { req?.let { model.request(it.method, it.url) } }
                }
            }

            private var req: HttpRequest? = null

            fun bind(item: HttpRequest?) {
                req = item
                if (item == null) return
                tName.text = item.getTitle()
                tURL.text = item.getSubtitle()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
            layoutInflater.inflate(R.layout.item_http_request, parent, false)
        )

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(getItem(position))
        }
    }

}

class ResponseFragment : AutoInflateLayoutFragment(R.layout.fragment_rest_response) {

    val model by activityViewModels<RestViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tv = view.findViewById<TextView>(R.id.textView)
        model.response.observe(viewLifecycleOwner, Observer {
            tv.text = it.body
        })
    }
}

