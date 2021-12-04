package com.example.eventa.mainFragments

import android.app.ActionBar
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.compose.ui.layout.Layout
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.eventa.R
import com.example.eventa.User
import com.example.eventa.viewModels.followedEventsViewModel
import com.example.eventa.viewModels.orgEventsViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {

    val modelFollowedEvents: followedEventsViewModel by viewModels()
    val modelOrgEvents: orgEventsViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav_menu)
        val navController = findNavController(R.id.navigation_fragment)
        bottomNavigationView.setupWithNavController(navController)
        val badge = bottomNavigationView.getOrCreateBadge(R.id.myEvents)
        badge.backgroundColor = resources.getColor(R.color.design_default_color_secondary_variant)

        modelFollowedEvents.notifications.observe(this){ amount ->
            if (amount == 0){
                badge.isVisible = false
            }
            else{
                badge.isVisible = true
                badge.number = amount
            }
        }
        modelFollowedEvents.sharedPreferences = getSharedPreferences("FOLLOWED_EVENTS_TIMESTAMPS", Context.MODE_PRIVATE)
        modelFollowedEvents.email = User.email
        modelFollowedEvents.loadFollowedEvents()

        modelOrgEvents.email = User.email
        modelOrgEvents.loadOrgEvents()


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