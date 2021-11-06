package com.example.eventa.mainFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.eventa.Event
import com.example.eventa.R
import com.example.eventa.User
import com.example.eventa.recyclerViews.followedEventsAdapter
import com.example.eventa.recyclerViews.orgEventsAdapter
import com.example.eventa.viewModels.allEventsViewModel
import com.example.eventa.viewModels.followedEventsViewModel

class FollowedEvents : Fragment() {

    private lateinit var prBar: ProgressBar
    private lateinit var swipeR: SwipeRefreshLayout
    private lateinit var rView: RecyclerView
    private lateinit var layoutEmpty: ConstraintLayout
    private var adapter: followedEventsAdapter? = null
    private var events = mutableListOf<Event>()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val i = inflater.inflate(R.layout.fragment_followed_events, container, false)

        prBar = i.findViewById(R.id.progressBarFollowedEvents)
        prBar.isEnabled = false
        prBar.visibility = View.INVISIBLE

        layoutEmpty = i.findViewById(R.id.layoutEmpty)

        swipeR = i.findViewById(R.id.swiperefreshFollowedEvents)
        swipeR.setOnRefreshListener {
            updateData()
            swipeR.isRefreshing = false
        }

        rView = i.findViewById(R.id.rViewFollowedEvents)
        rView.layoutManager = LinearLayoutManager(activity?.applicationContext)

        val model: followedEventsViewModel by activityViewModels()
        if (model.email != User.email){
            updateData()
        }
        activity?.let {
            model.getEvents().observe(it , { events ->
                onEventsResult(true, events)
            })
        }

        return i
    }

    private fun updateData(){
        prBar.visibility = View.VISIBLE
        prBar.isEnabled = true
        val model: followedEventsViewModel by activityViewModels()
        model.email = User.email
        model.loadFollowedEvents()
    }

    fun onEventsResult(result: Boolean, eventsNew: List<Event>?) {
        prBar.visibility = View.INVISIBLE
        prBar.isEnabled = false
        if (result) {
            if(eventsNew != null)
                events.clear()
                events.addAll(eventsNew!!)
                if(events.size == 0)
                    layoutEmpty.visibility = View.VISIBLE
                else
                    layoutEmpty.visibility = View.GONE
            if(adapter == null){
                adapter = followedEventsAdapter(events!!)
                rView.adapter = adapter
            }
            adapter!!.notifyDataSetChanged()
        }
    }

}