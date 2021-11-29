package com.example.eventa.mainFragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.eventa.Event
import com.example.eventa.R
import com.example.eventa.User
import com.example.eventa.recyclerViews.followedEventsAdapter
import com.example.eventa.recyclerViews.orgEventsAdapter
import com.example.eventa.viewModels.followedEventsViewModel
import com.example.eventa.viewModels.orgEventsViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class OrginisedEvents : Fragment() {
    private lateinit var fab: FloatingActionButton
    private lateinit var prBar: ProgressBar
    private lateinit var swipeR: SwipeRefreshLayout
    private lateinit var rView: RecyclerView
    private lateinit var layoutEmpty: ConstraintLayout
    private var adapter: orgEventsAdapter? = null
    private lateinit var model: orgEventsViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val i = inflater.inflate(R.layout.fragment_orginised_events, container, false)

        prBar = i.findViewById(R.id.progressBarOrgEvents)
        prBar.isEnabled = false
        prBar.visibility = View.INVISIBLE

        layoutEmpty = i.findViewById(R.id.layoutEmpty)

        swipeR = i.findViewById(R.id.swiperefreshOrgEvents)
        swipeR.setOnRefreshListener {
            updateData()
            swipeR.isRefreshing = false
        }

        rView = i.findViewById(R.id.rViewOrgEvents)
        rView.layoutManager = LinearLayoutManager(activity?.applicationContext)

        fab = i.findViewById(R.id.fab)

        fab.setOnClickListener {
            val action = MyEventsDirections.actionMyEventsToOrgEvents()
            findNavController().navigate(action)
        }

        val modelN: orgEventsViewModel by activityViewModels()
        model = modelN

        if(adapter == null){
            adapter = orgEventsAdapter(model.getEvents().value!!, rView)
            rView.adapter = adapter
        }
        activity?.let {
            model.getEvents().observe(it, { events ->
                adapter!!.events = events
                onEventsResult(model.change, model.newPos, model.oldPos)
            })
        }

        if (model.email != User.email){
            updateData()
        }

        return i
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateData(){
        prBar.visibility = View.VISIBLE
        prBar.isEnabled = true
        model.email = User.email
        model.loadOrgEvents()
    }

    fun onEventsResult(type: orgEventsViewModel.Types, newPos: Int, oldPos: Int) {
        prBar.visibility = View.INVISIBLE
        prBar.isEnabled = false
        if (adapter!!.events.isEmpty())
            layoutEmpty.visibility = View.VISIBLE
        else {
            layoutEmpty.visibility = View.GONE
        }

        when (type) {
            orgEventsViewModel.Types.ADDED -> {
                adapter!!.notifyItemInserted(newPos)
            }
            orgEventsViewModel.Types.MODIFIED -> {
                if (oldPos == newPos){
                    adapter!!.notifyItemChanged(oldPos)
                }
                else{
                    adapter!!.notifyItemRemoved(oldPos)
                    adapter!!.notifyItemInserted(newPos)
                }
            }
            orgEventsViewModel.Types.REMOVED -> {
                adapter!!.previousExpandedPosition = adapter!!.noSelected
                adapter!!.mExpandedPosition = adapter!!.noSelected
                adapter!!.notifyItemRemoved(oldPos)
            }
            orgEventsViewModel.Types.CLEARED -> {
                adapter!!.notifyDataSetChanged()
            }
        }
    }

}