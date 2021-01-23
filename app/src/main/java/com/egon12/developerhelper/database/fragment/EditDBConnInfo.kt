package com.egon12.developerhelper.database.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.egon12.developerhelper.ConnInfo
import com.egon12.developerhelper.ConnType
import com.egon12.developerhelper.R
import com.egon12.developerhelper.database.persistent.DBConnInfo
import com.egon12.developerhelper.database.persistent.DBType
import com.egon12.developerhelper.database.viewmodel.DatabaseViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditDBConnInfo : Fragment() {

    private val viewModel: DatabaseViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_db_connection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        val etId = view.findViewById<TextInputEditText>(R.id.et_id)
        val etType = view.findViewById<MaterialButtonToggleGroup>(R.id.opt_type)
        val etHost = view.findViewById<TextInputEditText>(R.id.et_host)
        val etDbName = view.findViewById<TextInputEditText>(R.id.et_dbname)
        val etUsername = view.findViewById<TextInputEditText>(R.id.et_username)
        val etPassword = view.findViewById<TextInputEditText>(R.id.et_password)

        val btnMysql = view.findViewById<MaterialButton>(R.id.btn_mysql)
        val btnPostgres = view.findViewById<MaterialButton>(R.id.btn_postgre)
        val btnHttp = view.findViewById<MaterialButton>(R.id.btn_http)
        val btnGRPC = view.findViewById<MaterialButton>(R.id.btn_grpc)

        val btnSave = view.findViewById<ExtendedFloatingActionButton>(R.id.btn_save)

        var conn: ConnInfo? = null
        viewModel.connection.onEdit.observe(viewLifecycleOwner) {
            conn = it
            etId.setText(it.name)
        }

        var dbConn: DBConnInfo? = null
        viewModel.connection.dbOnEdit.observe(viewLifecycleOwner) {
            dbConn = it
            etHost.setText(it.host)
            etDbName.setText(it.dbName)
            etUsername.setText(it.username)
            etPassword.setText(it.password)
            when (it.type) {
                DBType.MySQL -> btnMysql.performClick()
                DBType.Postgre -> btnPostgres.performClick()
            }
        }

        btnSave.shrink()

        val scrollView = view.findViewById<ScrollView>(R.id.scroll_view)
        scrollView.viewTreeObserver.addOnScrollChangedListener {
            if (scrollView.getChildAt(0).bottom <= scrollView.height + scrollView.scrollY) {
                btnSave.extend()
            } else {
                btnSave.shrink()
            }
        }

        btnSave.setOnClickListener {

            conn?.apply {
                name = etId.text.toString()
                type = when(etType.checkedButtonId) {
                    R.id.btn_db -> ConnType.Database
                    R.id.btn_http -> ConnType.Http
                    R.id.btn_grpc -> ConnType.GRPC
                    else -> throw IllegalStateException("Not known type for id ${etType.checkedButtonId}")
                }
            }

            dbConn?.apply {
                host = etHost.text.toString()
                dbName = etDbName.text.toString()
                username = etUsername.text.toString()
                password = etPassword.text.toString()
            }

            viewModel.connection.save(conn, dbConn)

            findNavController().popBackStack()
        }
    }
}
