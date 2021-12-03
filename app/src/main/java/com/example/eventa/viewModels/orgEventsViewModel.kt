package com.example.eventa.viewModels

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.eventa.DBHelper
import com.example.eventa.Event
import com.example.eventa.User
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime

class orgEventsViewModel : ViewModel() {
    var email: String = ""

    private val events = MutableLiveData<MutableList<Event>>()
    enum class Types { ADDED, MODIFIED, REMOVED, CLEARED }
    var change = Types.CLEARED
    var newPos: Int = -1
    var oldPos: Int = -1

    init{
        events.value = mutableListOf()
    }

    fun getEvents(): LiveData<MutableList<Event>> {
        return events
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadOrgEvents() {
        if (email != "") {
            change = Types.CLEARED
            events.value = mutableListOf()
            DBHelper.loadOrganisedEvents(User.email){ event, newPos, oldPos, type ->
                onOrgEventsResult(event, newPos, oldPos, type)
            }
        } else {
            Log.d("orgEventsViewModel", "No input data, cant load all events")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkToday(event: Event, index: Int) {
        val dateInstant = Instant.ofEpochMilli(event.date)
        var dateSnap = ZonedDateTime.ofInstant(dateInstant, ZoneOffset.UTC)
        val nowInstant = Instant.ofEpochMilli(System.currentTimeMillis())
        val nowSnap = ZonedDateTime.ofInstant(nowInstant, ZoneOffset.UTC)
        if (dateSnap.year == nowSnap.year && dateSnap.month == nowSnap.month && dateSnap.dayOfMonth == nowSnap.dayOfMonth) {
            event.today = true
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onOrgEventsResult(event: Event, newPos: Int, oldPos: Int, type: Types) {
        change = type
        this.newPos = newPos
        this.oldPos = oldPos

        when (change) {
            Types.ADDED -> {
                if (newPos >= events.value!!.size) {
                    events.value!!.add(event)
                }
                else{
                    events.value!!.add(newPos, event)
                }
                checkToday(event, newPos)
                events.value = events.value
            }
            Types.MODIFIED -> {
                if (newPos == oldPos){
                    events.value!![newPos] = event
                }
                else{
                    events.value!!.removeAt(oldPos)
                    events.value!!.add(newPos, event)
                }
                checkToday(event, newPos)
                events.value = events.value
            }
            Types.REMOVED -> {
                events.value!!.removeAt(oldPos)
                events.value = events.value
            }
        }
    }
}
