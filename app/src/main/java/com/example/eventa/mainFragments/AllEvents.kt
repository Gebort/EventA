package com.example.eventa.mainFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.eventa.Event
import com.example.eventa.R
import com.example.eventa.User
import com.example.eventa.recyclerViews.allEventsAdapter
import com.example.eventa.viewModels.allEventsViewModel


class AllEvents : Fragment() {
    //TODO добавить поиск по названию/ID события
    private lateinit var spinner: Spinner
    private lateinit var prBar: ProgressBar
    private lateinit var swipeR: SwipeRefreshLayout
    private lateinit var rView: RecyclerView
    private lateinit var layoutEmpty: ConstraintLayout
    private var adapter: allEventsAdapter? = null
    private var events = mutableListOf<Event>()
    private lateinit var v: View

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val i = inflater.inflate(R.layout.fragment_all_events, container, false)

        activity?.title = ""

        val data = arrayOf(User.city, "None")

        prBar = i.findViewById(R.id.progressBar)
        prBar.isEnabled = false
        prBar.visibility = View.INVISIBLE

        layoutEmpty = i.findViewById(R.id.layoutEmpty)

        v = inflater.inflate(R.layout.spinner_city, null)

        spinner = v as Spinner
        val adapterSpinner = activity?.applicationContext?.let { ArrayAdapter(it, R.layout.spinner_city_item, data) }
        adapterSpinner?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapterSpinner
        spinner.setSelection(0, false)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateData(data[position], User.age)
            }

        }

        swipeR = i.findViewById(R.id.swiperefresh)
        swipeR.setOnRefreshListener {
            updateData(data[spinner.selectedItemPosition], User.age)
            swipeR.isRefreshing = false
        }

        rView = i.findViewById(R.id.rView)
        rView.layoutManager = LinearLayoutManager(activity?.applicationContext)

        val model: allEventsViewModel by activityViewModels()
        if (model.city != User.city){
            updateData(data[0], User.age)
        }
        activity?.let {
            model.getEvents().observe(it, { events ->
                onEventsResult(true, events)
            })
        }

        return i
    }


    override fun onStop() {
        super.onStop()
        val act: AppCompatActivity = activity as AppCompatActivity
        val actionBar = act.supportActionBar
        actionBar?.customView = null
    }

    override fun onResume(){
        super.onResume()
        val act: AppCompatActivity = activity as AppCompatActivity
        val actionBar = act.supportActionBar
        actionBar?.customView = v
        actionBar?.setDisplayShowCustomEnabled(true)
    }

    fun updateData(city: String, age: Int){
        prBar.visibility = View.VISIBLE
        prBar.isEnabled = true
        val model: allEventsViewModel by activityViewModels()
        model.city = city
        model.age = age
        model.email = User.email
        model.loadAllEvents()
    }

    fun onEventsResult(result: Boolean, eventsNew: List<Event>?) {
        prBar.visibility = View.INVISIBLE
        prBar.isEnabled = false
        if (result) {
            if(eventsNew != null)
                events.clear()
                events.addAll(eventsNew!!)
                if(events.size == 0){
                    layoutEmpty.visibility = View.VISIBLE
                }
                else{
                    layoutEmpty.visibility = View.GONE
                }
            if(adapter == null){
                adapter = allEventsAdapter(events!!)
                rView.adapter = adapter
            }
            adapter!!.notifyDataSetChanged()
        }
    }
}