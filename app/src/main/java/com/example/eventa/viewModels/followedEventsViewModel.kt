package com.example.eventa.viewModels

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.eventa.DBHelper
import com.example.eventa.Event
import com.example.eventa.User
import java.time.*

class followedEventsViewModel : ViewModel() {
    var email: String = ""

    private val events = MutableLiveData<MutableList<Event>>()
    private val _notifications = MutableLiveData<Int>()
    val notifications get() = _notifications
    var sharedPreferences: SharedPreferences? = null

    enum class Types {ADDED, MODIFIED, REMOVED, CLEARED}
    var change = Types.CLEARED
    var newPos = -1
    var oldPos = -1

    init{
        events.value = mutableListOf()
        _notifications.value = 0
    }

    fun getEvents(): LiveData<MutableList<Event>> {
        return events
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadFollowedEvents() {
        if (email != "") {
            change = Types.CLEARED
            events.value = mutableListOf()
            _notifications.value = 0
            DBHelper.loadFollowedEvents(email){ event, newPos, oldPos, type ->
                onOrgEventsResult(event, newPos, oldPos, type)
            }
        }
        else {
            Log.d("followedEventsViewModel", "No input data, cant load all events")
        }
    }

    fun addNotification(index: Int){
        if (!events.value!![index].notification) {
            _notifications.value = _notifications.value?.plus(1)
            events.value!![index].notification = true
        }
    }

    private fun removeNotification(index: Int){
        if (events.value!![index].notification) {
            _notifications.value = _notifications.value?.minus(1)
            events.value!![index].notification = false
            sharedPreferences?.let {
                it.edit().putLong(events.value!![index].id, System.currentTimeMillis()).apply()

            }
        }
    }

    private fun deleteNotificationTimeRecord(index: Int){
        sharedPreferences?.edit()?.remove(events.value!![index].id)?.apply()
    }

    fun rewriteNotificationRecords(){
        sharedPreferences?.edit()?.clear()?.commit()
        val editor = sharedPreferences?.edit()
        events.value!!.forEach {
            editor?.putLong(it.id!!, it.lastUpdate)
        }
        editor?.commit()
    }

    fun itemExpanded(index: Int){
        if (events.value!![index].notification){
            removeNotification(index)
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
            val lastUpdateDay = ZonedDateTime.now(ZoneOffset.UTC).dayOfMonth
            if (lastUpdateDay != dateSnap.dayOfMonth) {
                addNotification(index)
            }
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
                val lastUpdate = sharedPreferences?.getLong(event.id, -1) ?: -1
                if (lastUpdate < event.lastUpdate){
                    addNotification(newPos)
                }
                checkToday(event, newPos)
                events.value = events.value
            }
            Types.MODIFIED -> {
                if (!event.notification){
                    val lastUpdate = sharedPreferences?.getLong(event.id, -1) ?: -1
                    if (lastUpdate < event.lastUpdate){
                        addNotification(newPos)
                    }
                }
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
                if (event.notification){
                    removeNotification(oldPos)
                }
                deleteNotificationTimeRecord(oldPos)
                events.value!!.removeAt(oldPos)
                events.value = events.value
            }
        }
    }
}
