package com.example.eventa.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.eventa.DBHelper
import com.example.eventa.Event
import com.example.eventa.User

class orgEventsViewModel : ViewModel() {
    var email: String = ""

    private val events = MutableLiveData<MutableList<Event>>()
    enum class Types { ADDED, MODIFIED, REMOVED, CLEARED }
    var change = Types.CLEARED
    var pos: Int = -1

    init{
        events.value = mutableListOf()
    }

    fun getEvents(): LiveData<MutableList<Event>> {
        return events
    }

    fun loadOrgEvents() {
        if (email != "") {
            change = Types.CLEARED
            events.value = mutableListOf()
            DBHelper.loadOrganisedEvents(User.email, ::onOrgEventsResult)
        } else {
            Log.d("orgEventsViewModel", "No input data, cant load all events")
        }
    }

    private fun onOrgEventsResult(event: Event, pos: Int, type: Types) {
        change = type
        this.pos = pos

        when (change) {
            Types.ADDED -> {
                events.value!!.add(event)
                events.value = events.value!!.sortedBy { it.date }.toMutableList()
            }
            Types.MODIFIED -> {
                events.value!![pos] = event
                events.value = events.value
            }
            Types.REMOVED -> {
                events.value!!.removeAt(pos)
                events.value = events.value
            }
        }
    }
}
