package com.example.eventa.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.eventa.DBHelper
import com.example.eventa.Event
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class allEventsViewModel : ViewModel() {
    var email = ""
    var city: String? = ""
    var age = -1

    var eventIncrement = 5
    var eventMin = 20
    var eventCount = eventMin
    var updateDelay = 2000L
    var isDelayLoading = false


    private val events = MutableLiveData<MutableList<Event>>()
    enum class Types {ADDED, MODIFIED, REMOVED, CLEARED}
    var change = Types.CLEARED
    var pos: Int = -1

    init{
        events.value = mutableListOf()
    }

    fun getEvents(): LiveData<MutableList<Event>> {
        return events
    }

    @DelicateCoroutinesApi
    fun loadAllEvents(clear: Boolean) {
        if(email != "" && city != "" && age != -1) {
            if (clear) {
                change = Types.CLEARED
                events.value = mutableListOf()
            }
            DBHelper.loadAvalEvents(city, eventCount.toLong()){ event, result ->
                onAllEventsResult(event, result)
            }

            delayUpdateCheck()
        }
        else
            Log.d("allEventsViewModel", "No input data, cant load all events")
    }

    @DelicateCoroutinesApi
    private fun onAllEventsResult(event: Event, type: Types){
        val e = events.value!!.firstOrNull { it.id == event.id }
        change = type

        if(change == Types.MODIFIED){
            if (e == null){
                change = Types.ADDED
            }
            else{
                if ((event.users != null && event.users!!.contains(email)) ||
                        event.currPartNumber >= event.partNumber ||
                        event.minAge > age) {
                    change = Types.REMOVED
                }
                else{
                    change = Types.MODIFIED
                }
            }
        }

        when (change) {
            Types.ADDED -> {
                if(e == null) {
                    if (event.orgEmail!! != email && (event.users == null || !event.users!!.contains(email)) &&
                            event.currPartNumber < event.partNumber &&
                            event.minAge <= age) {
                        events.value!!.add(event)
                        val newEvents = events.value!!.sortedBy { it.date }.toMutableList()
                        pos = newEvents.indexOf(event)
                        events.value = newEvents
                    }
                }
            }
            Types.MODIFIED -> {
                pos = events.value!!.indexOf(e)
                events.value!![pos] = event
                events.value = events.value
            }
            Types.REMOVED -> {
                pos = events.value!!.indexOf(e)
                if(pos != -1) {
                    events.value!!.removeAt(pos)
                    events.value = events.value
                }
            }
        }

        delayUpdateCheck()

    }

    @DelicateCoroutinesApi
    private fun delayUpdateCheck(){
        if (!isDelayLoading && events.value!!.size < eventMin) {
            GlobalScope.launch {
                isDelayLoading = true
                delay(updateDelay)
                if (events.value != null)
                    if (events.value!!.size < eventMin) {
                        eventCount += eventIncrement
                        loadAllEvents(false)
                    }
                isDelayLoading = false
            }
        }
        }

}