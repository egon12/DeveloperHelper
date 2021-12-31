package com.egon12.developerhelper.database.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.egon12.developerhelper.ConnInfo
import com.egon12.developerhelper.R
import com.egon12.developerhelper.R.id.*
import com.egon12.developerhelper.database.persistent.DBConnInfo
import com.egon12.developerhelper.database.persistent.DBType
import com.egon12.developerhelper.database.viewmodel.EditDBViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class EditDBConnInfo : Fragment() {

    private val model: EditDBViewModel by activityViewModels()
    private var c: ConnInfo? = null
    private var d: DBConnInfo? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_db_connection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindView(view)
        bindSaveButton(view)
        model.edit.observe(viewLifecycleOwner, this::bindData)

        model.start(arguments?.get("uuid") as? UUID?)
    }

    private fun bindData(it: Pair<ConnInfo, DBConnInfo>) {
        val (connInfo, dbConnInfo) = it
        c = connInfo.apply {
            idEditText.setText(name)
        }

        d = dbConnInfo.apply {
            hostEditText.setText(host)
            dbNameEditText.setText(dbName)
            usernameEditText.setText(username)
            passwordEditText.setText(password)
            when (type) {
                DBType.MySQL -> btnMySQL.performClick()
                DBType.Postgre -> btnPostgre.performClick()
            }
        }
    }

    private fun bindView(view: View?) {
        if (view == null) {
            Log.e(TAG, "Cannot bind view")
            findNavController().popBackStack()
            return
        }

        view.apply {
            idEditText = findViewById(et_id)
            hostEditText = findViewById(et_host)
            dbNameEditText = findViewById(et_dbname)
            usernameEditText = findViewById(et_username)
            passwordEditText = findViewById(et_password)

            dbTypeButtonGroup = findViewById(opt_db_type)
            btnMySQL = findViewById(btn_mysql)
            btnPostgre = findViewById(btn_postgre)
        }
    }

    private fun bindSaveButton(view: View) {
        view.findViewById<ExtendedFloatingActionButton>(btn_save)?.apply {
            setOnClickListener {
                save()
                findNavController().popBackStack()
            }

            shrink()

            view.findViewById<ScrollView>(scroll_view).let { sv ->
                sv.viewTreeObserver.addOnScrollChangedListener {
                    val bottom = sv.getChildAt(0).bottom
                    when {
                        bottom <= sv.height + sv.scrollY -> extend()
                        else -> shrink()
                    }
                }
            }

        }
    }

    private fun save() {
        val fc = c?.apply {
            name = idEditText.text.toString()
        } ?: return

        val fd = d?.apply {
            host = hostEditText.text.toString()
            dbName = dbNameEditText.text.toString()
            username = usernameEditText.text.toString()
            password = passwordEditText.text.toString()
            type = when (dbTypeButtonGroup.checkedButtonId) {
                btn_mysql -> DBType.MySQL
                btn_postgre -> DBType.Postgre
                else -> DBType.MySQL
            }
        } ?: return

        model.save(fc, fd)
    }

    private lateinit var idEditText: TextInputEditText
    private lateinit var hostEditText: TextInputEditText
    private lateinit var dbNameEditText: TextInputEditText
    private lateinit var usernameEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText

    private lateinit var dbTypeButtonGroup: MaterialButtonToggleGroup
    private lateinit var btnMySQL: MaterialButton
    private lateinit var btnPostgre: MaterialButton

    companion object {
        const val TAG = "EditDB"
    }
}
