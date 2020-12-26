package com.egon12.developerhelper

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.egon12.developerhelper.database.persistent.Connection
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditConnectionFragment : Fragment() {

    private val viewModel: DatabaseViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_connection, container, false)
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
        val btnPostgres = view.findViewById<MaterialButton>(R.id.btn_postgresql)

        if (viewModel.connectionInEdit != Connection.EMPTY) {
            val c = viewModel.connectionInEdit
            etId.setText(c.name)
            etHost.setText(c.host)
            etDbName.setText(c.dbName)
            etUsername.setText(c.username)
            etPassword.setText(c.password)
            when (c.type) {
                "mysql" -> btnMysql.performClick()
                "postgresql" -> btnPostgres.performClick()
                else -> btnMysql.performClick()
            }
        }

        view.findViewById<MaterialButton>(R.id.btn_save).setOnClickListener {

            val type = when (etType.checkedButtonId) {
                R.id.btn_mysql -> "mysql"
                R.id.btn_postgresql -> "postgresql"
                else -> "unknown"
            }

            viewModel.storeConnection(
                Connection(
                    etId.text.toString(),
                    type,
                    etHost.text.toString(),
                    etDbName.text.toString(),
                    etUsername.text.toString(),
                    etPassword.text.toString()
                )
            )

            findNavController().popBackStack()
        }
    }
}
