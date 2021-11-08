package com.example.eventa.viewModels

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.eventa.DBHelper
import com.example.eventa.Event
import com.example.eventa.User
import com.google.firestore.v1.DocumentChange

//TODO загружать только N событий, подгружать еще n когда пользователь дойдет до конца листа
class allEventsViewModel : ViewModel() {
    var email = ""
    var city = ""
    var age = -1

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

    fun loadAllEvents() {
        if(email != "" && city != "" && age != -1) {
            change = Types.CLEARED
            events.value = mutableListOf()
            DBHelper.loadAvalEvents(email, city, age, ::onAllEventsResult)
        }
        else
            Log.d("allEventsViewModel", "No input data, cant load all events")
    }

    private fun onAllEventsResult(event: Event, type: Types){
        val e = events.value!!.firstOrNull { it.id == event.id }
        change = type

        if(change == Types.MODIFIED){
            if (e == null){
                change = Types.ADDED
            }
            else{
                if ((event.users != null && event.users!!.contains(email)) ||
                        event.currPartNumber >= event.partNumber) {
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
                            event.currPartNumber < event.partNumber) {
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
    }

}