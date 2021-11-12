package com.example.eventa.mainFragments

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
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
    private lateinit var v: View
    private lateinit var model: allEventsViewModel

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val i = inflater.inflate(R.layout.fragment_all_events, container, false)

        activity?.title = ""

        val data = arrayOf(User.city, "None")

        prBar = i.findViewById(R.id.progressBar)
        prBar.isEnabled = false
        prBar.visibility = View.INVISIBLE

        layoutEmpty = i.findViewById(R.id.layoutEmpty)
        layoutEmpty.visibility = View.GONE

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
                updateData(data[position])
            }

        }

        swipeR = i.findViewById(R.id.swiperefresh)
        swipeR.setOnRefreshListener {
            updateData(data[spinner.selectedItemPosition])
            swipeR.isRefreshing = false
        }

        rView = i.findViewById(R.id.rView)
        rView.layoutManager = LinearLayoutManager(activity?.applicationContext)

        val modelN: allEventsViewModel by activityViewModels()
        model = modelN
        if(adapter == null){
            adapter = allEventsAdapter(rView, model.eventMin, model.getEvents().value!!.toMutableList(), ::onScrollEnd)
            rView.adapter = adapter
        }
        activity?.let {
            model.getEvents().observe(it, { events ->
                adapter!!.events = events.toMutableList()
                if (adapter!!.isLoading){
                    adapter!!.events.add(null)
                }
                onEventsResult(model.change, model.pos, model.getEvents().value!!.size)
            })
        }
        if (model.city != data[spinner.selectedItemPosition] || model.email != User.email || model.age != User.age){
            updateData(data[0])
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val search = menu.findItem(R.id.action_search)
        val searchView = search.actionView as SearchView?
        searchView?.queryHint = "Search by title or id..."
        searchView?.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                return true
            }
        })
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.action_search -> {

            }
            R.id.action_more -> {

            }
        }

        return super.onOptionsItemSelected(item)
    }

    fun updateData(city: String){
        prBar.visibility = View.VISIBLE
        prBar.isEnabled = true
        adapter!!.setLoaded()
        model.city = city
        model.age = User.age
        model.email = User.email
        model.eventCount = model.eventMin
        model.loadAllEvents(true)
    }

    fun onScrollEnd(){
        if (!adapter!!.isLoading) {
            adapter!!.isLoading = true
            adapter!!.events.add(null)
            adapter!!.notifyItemInserted(adapter!!.events.size - 1)
            model.eventCount += model.eventIncrement
            model.loadAllEvents(false)
        }
    }

    fun onEventsResult(type: allEventsViewModel.Types, pos: Int, size: Int) {
        prBar.visibility = View.INVISIBLE
        prBar.isEnabled = false

        if (adapter!!.events.isEmpty()){
            layoutEmpty.visibility = View.VISIBLE
        }
        else {
            layoutEmpty.visibility = View.GONE
        }

        when (type) {
            allEventsViewModel.Types.ADDED -> {
                if (adapter!!.isLoading){
                    adapter!!.setLoaded()
                    adapter!!.events.remove(null)
                    adapter!!.notifyItemRemoved(adapter!!.events.size)
                    adapter!!.notifyItemInserted(pos)
                }
                else {
                    adapter!!.notifyItemInserted(pos)
                }
            }
            allEventsViewModel.Types.MODIFIED -> {
                adapter!!.notifyItemChanged(pos)
            }
            allEventsViewModel.Types.REMOVED -> {
                adapter!!.notifyItemRemoved(pos)
                if (adapter!!.events.size < model.eventMin){
                    onScrollEnd()
                }
            }
            allEventsViewModel.Types.CLEARED -> {
                adapter!!.notifyDataSetChanged()
            }
        }
    }
}