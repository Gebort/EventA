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

    private val events = MutableLiveData<List<Event>>()

    fun getEvents(): LiveData<List<Event>> {
        return events
    }

    fun loadOrgEvents() {
        if (email != "")
            DBHelper.loadOrganisedEvents(User.email, ::onOrgEventsResult)
        else
            Log.d("orgEventsViewModel", "No input data, cant load all events")
    }

    private fun onOrgEventsResult(result: Boolean, newEvents: List<Event>?) {
        if (result)
            if (newEvents != null)
                events.value = newEvents
            else
                events.value = listOf()
    }
}
