package com.example.eventa.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.eventa.DBHelper
import com.example.eventa.Event
import com.example.eventa.User
//TODO загружать только N событий, подгружать еще n когда пользователь дойдет до конца листа
class allEventsViewModel : ViewModel() {
    var email = ""
    var city = ""
    var age = -1

    private val events = MutableLiveData<List<Event>>()

    fun getEvents(): LiveData<List<Event>> {
        return events
    }

    fun loadAllEvents() {
        if(email != "" && city != "" && age != -1)
            DBHelper.loadAvalEvents(User.email, city, age, ::onAllEventsResult)
        else
            Log.d("allEventsViewModel", "No input data, cant load all events")
    }

    private fun onAllEventsResult(result: Boolean, newEvents: List<Event>?){
        if(result)
            if(newEvents != null)
                events.value = newEvents
            else
                events.value = listOf()
    }
}