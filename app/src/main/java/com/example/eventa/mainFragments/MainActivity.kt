package com.example.eventa.mainFragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.eventa.R
import com.example.eventa.User
import com.example.eventa.viewModels.allEventsViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav_menu)
        val navController = findNavController(R.id.navigation_fragment)
        bottomNavigationView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.all_events_icon, menu)
        val search = menu.findItem(R.id.action_search)
        val searchView = SearchView(this)
        search.actionView = searchView
        search.expandActionView()
        return super.onCreateOptionsMenu(menu)
    }
}