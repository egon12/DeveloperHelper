package com.egon12.developerhelper.rest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.egon12.developerhelper.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText

class RestFragment : Fragment() {

    private val model: RestViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_rest, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val collectionAdapter = CollectionAdapter()
        model.collection.observe(viewLifecycleOwner, { collectionAdapter.setCollection(it) })

        view.findViewById<RecyclerView>(R.id.rv_request).apply {
            layoutManager = LinearLayoutManager(view.context)
            adapter = collectionAdapter
        }

        val sMethod = view.findViewById<MaterialAutoCompleteTextView>(R.id.s_method).apply {
            setAdapter(
                ArrayAdapter(
                    view.context,
                    android.R.layout.simple_list_item_1,
                    Collection.Method.values().map { it.name }
                )
            )
        }

        val btnSend = view.findViewById<ExtendedFloatingActionButton>(R.id.btn_send).apply {
            shrink()
        }

        val bottomSheet: ConstraintLayout = view.findViewById(R.id.bottom_sheet_request)

        val collapseState = ConstraintSet().apply { clone(bottomSheet) }
        val expandState = ConstraintSet().apply {
            clone(view.context, R.layout.bottom_sheet_request_alt)
        }

        val bottomSheetBehaviorCallback = object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(v: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        expandState.applyTo(bottomSheet)
                        btnSend.extend()
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        collapseState.applyTo(bottomSheet)
                        btnSend.shrink()
                    }
                    else -> {
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        }

        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet).apply {
            addBottomSheetCallback(bottomSheetBehaviorCallback)
        }

        val etUrl = view.findViewById<TextInputEditText>(R.id.et_url)

        collectionAdapter.callRequest = {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }


            etUrl.setText(it.request.url.raw)
            sMethod.setText(it.request.method.name)
        }
    }
}