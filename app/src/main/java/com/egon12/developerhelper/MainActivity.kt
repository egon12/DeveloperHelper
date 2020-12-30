package com.egon12.developerhelper

import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.egon12.developerhelper.database.viewmodel.DatabaseViewModel
import com.egon12.developerhelper.rest.RestViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    private val databaseViewModel by viewModels<DatabaseViewModel>()

    private val restViewModel by viewModels<RestViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        val navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        val view = findViewById<View>(android.R.id.content)
        databaseViewModel.error.observe(this, Observer {
            Snackbar.make(view, it.localizedMessage, Snackbar.LENGTH_LONG).show()
        })

        restViewModel.error.observe(this, Observer {
            Snackbar.make(view, it.localizedMessage, Snackbar.LENGTH_LONG).show()
        })

        val progressBar = findViewById<View>(R.id.SHOW_PROGRESS)
        databaseViewModel.loadingStatus.observe(this, Observer {
            progressBar.visibility = if (it) View.VISIBLE else View.GONE
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val item = menu?.add("Kucing")
        item?.setOnMenuItemClickListener {
            findNavController(R.id.nav_host_fragment).navigate(R.id.action_ConnectionFragment_to_RestFragment)
            true
        }
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}