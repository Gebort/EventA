package com.example.eventa.viewModels

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.key
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.eventa.DBHelper
import com.example.eventa.Event
import com.example.eventa.User
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

class allEventsViewModel : ViewModel() {
    var email = ""
    var city: String? = ""
    var age: Int = -1

    var eventIncrement = 30
    var eventMin = 30
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

    @RequiresApi(Build.VERSION_CODES.O)
    @DelicateCoroutinesApi
    fun loadAllEvents(clear: Boolean, keywords: MutableList<String> = mutableListOf(" ")) {
        if(email != "" && city != "" && age != -1) {
            if (clear) {
                change = Types.CLEARED
                events.value = mutableListOf()
            }
            DBHelper.loadAvalEvents(city, eventCount.toLong(), keywords){ event, result ->
                onAllEventsResult(event, result)
            }
        }
        else
            Log.d("allEventsViewModel", "No input data, cant load all events")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @DelicateCoroutinesApi
    private fun onAllEventsResult(event: Event, type: Types){
        val e = events.value!!.firstOrNull { it.id == event.id }
        change = type

        if(change == Types.MODIFIED){
            if (e == null){
                change = Types.ADDED
            }
            else{
                change = if (
                        (event.users != null && event.users!!.contains(email)) ||
                        (event.requests != null && event.requests!!.contains(email)) ||
                        event.currPartNumber >= event.partNumber ||
                        event.minAge > age) {
                    Types.REMOVED
                } else{
                    Types.MODIFIED
                }
            }
        }

        when (change) {
            Types.ADDED -> {
                if(e == null) {
                    if (
                            event.orgEmail!! != email &&
                            (event.users == null || !event.users!!.contains(email)) &&
                            (event.requests == null || !event.requests!!.contains(email)) &&
                            event.currPartNumber < event.partNumber &&
                            event.minAge <= age) {

                        events.value!!.add(event)
                        val newEvents = events.value!!.sortedBy { it.date }.toMutableList()
                        pos = newEvents.indexOf(event)
                        val dateInstant = Instant.ofEpochMilli(event.date)
                        var dateSnap = ZonedDateTime.ofInstant(dateInstant, ZoneOffset.UTC)
                        val nowInstant = Instant.ofEpochMilli(System.currentTimeMillis())
                        val nowSnap = ZonedDateTime.ofInstant(nowInstant, ZoneOffset.UTC)
                        if (dateSnap.year == nowSnap.year && dateSnap.month == nowSnap.month && dateSnap.dayOfMonth == nowSnap.dayOfMonth) {
                            event.today = true
                        }
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

//        delayUpdateCheck()

    }

    //TODO отключена проверка
    @RequiresApi(Build.VERSION_CODES.O)
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