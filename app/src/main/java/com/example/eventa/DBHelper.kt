package com.example.eventa

import android.util.Log
import com.example.eventa.viewModels.allEventsViewModel
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.w3c.dom.Document
import java.util.*
import kotlin.reflect.KFunction1

object DBHelper {

    var avalEventsListener: ListenerRegistration? = null
    var followedEventsListener: ListenerRegistration? = null
    var orgEventsListener: ListenerRegistration? = null

     fun emailCheck(
             email: String,
             callback: (Boolean) -> Unit)
     {
         val db = Firebase.firestore
         db.collection("users").document(email)
                 .get()
                 .addOnSuccessListener { result ->
                     if(result.exists()){
                         User.name = result.get("name").toString()
                         User.email = email
                         User.phone = result.get("phone").toString()
                         User.age = result.get("age").toString().toInt()
                         User.description = result.get("desc").toString()
                         User.city = result.get("city").toString()
                         callback(true)
                     }
                     else{
                         callback(false)
                     }

                 }
                 .addOnFailureListener {
                     callback(false)
                 }
        }

    fun fillUserData(
            name: String,
            email: String,
            phone: String,
            age: Int,
            desc: String,
            city: String,
            callback: (Boolean) -> Unit)
    {
        val db = Firebase.firestore
        val docData = hashMapOf(
                "name" to name,
                "phone" to phone,
                "age" to age,
                "desc" to desc,
                "city" to city
        )

        db.collection("users").document(email)
            .set(docData)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    fun fillEventData(title: String,
                      partNumber: Int,
                      minAge: Int,
                      date: Long,
                      hour: Int,
                      min: Int,
                      desc: String,
                      city: String?,
                      loc: String?,
                      public: Boolean,
                      showEmail: Boolean,
                      showNumber: Boolean,
                      orgName: String,
                      orgPhone: String,
                      orgEmail: String,
                      callback: KFunction1<Boolean, Unit>)
    {
        val db = Firebase.firestore

        val docData = hashMapOf(
                "title" to title,
                "partNumber" to partNumber,
                "currPartNumber" to 0,
                "minAge" to minAge,
                "desc" to desc,
                "date" to date,
                "hour" to hour,
                "min" to min,
                "loc" to loc,
                "public" to public,
                "showEmail" to showEmail,
                "showNumber" to showNumber,
                "orgName" to orgName,
                "orgPhone" to orgPhone,
                "orgEmail" to orgEmail
        )

        var city2 = ""
        if (city == null)
            city2 = "none"
        else
            city2 = city

            db.collection("cities").document(city2.toLowerCase(Locale.ROOT)).collection("events").add(docData)
                    .addOnSuccessListener {
                        callback(true)
                    }
                    .addOnFailureListener {
                        callback(false)
                    }


    }

    fun loadAvalEvents(email: String, city: String, age: Int, callback: (Event, allEventsViewModel.Types) -> Unit) {
        val db = Firebase.firestore

        avalEventsListener?.remove()

        avalEventsListener = db.collection("cities").document(city.toLowerCase(Locale.ROOT)).collection("events").whereLessThanOrEqualTo("minAge", age)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        Log.d("DBHelper", "Failed to load aval events: $error")
                    } else {
                        if (value != null) {
                            for (doc in value.documentChanges) {
                                val event = doc.document.toObject(Event::class.java)
                                event.id = doc.document.id
                                event.city = User.city.toLowerCase(Locale.ROOT)
                                if (event.users == null) {
                                    event.users = listOf()
                                }
                                when (doc.type) {
                                    DocumentChange.Type.ADDED -> {
                                        callback(event, allEventsViewModel.Types.ADDED)
                                    }
                                    DocumentChange.Type.MODIFIED -> {
                                        callback(event, allEventsViewModel.Types.MODIFIED)
                                    }
                                    DocumentChange.Type.REMOVED -> {
                                        callback(event, allEventsViewModel.Types.REMOVED)
                                    }
                                }
                            }
                        }
                    }
                }
    }

    fun loadOrganisedEvents(email: String, callback: (Boolean, List<Event>?) -> Unit){
        //TODO поиск не только по городу пользователя но и по none
        val db = Firebase.firestore

        orgEventsListener?.remove()

        orgEventsListener = db.collection("cities").document(User.city.toLowerCase(Locale.ROOT)).collection("events").whereEqualTo("orgEmail", email)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        Log.d("DBHelper", "Failed to load organised events: $error")
                        callback(false, null)
                    }
                    else if (value != null) {
                        val events = mutableListOf<Event>()
                        for (doc in value) {
                            val event = doc.toObject(Event::class.java)
                            event.id = doc.id
                            event.city = User.city.toLowerCase(Locale.ROOT)
                            events.add(event)
                        }
                        events.sortBy { it.date }
                        callback(true, events)
                    }
                    else {
                        callback(true, null)
                    }
                }

    }

    fun loadFollowedEvents(email: String, callback: (Boolean, List<Event>?) -> Unit){
        val db = Firebase.firestore

        followedEventsListener?.remove()

        followedEventsListener = db.collection("cities").document(User.city.toLowerCase()).collection("events").whereArrayContains("users", email)
                .addSnapshotListener { value, error ->
                    if (error != null){
                        Log.d("DBHelper", "Failed to load followed events: $error")
                        callback(false, null)
                    }
                    else if (value != null){
                        val events = mutableListOf<Event>()
                        for (doc in value){
                            val event = doc.toObject(Event::class.java)
                            event.id = doc.id
                            event.city = User.city.toLowerCase()
                            events.add(event)
                        }
                        events.sortBy { it.date }
                        callback(true, events)
                    }
                    else{
                        callback(true, null)
                    }
                }
    }

    fun addParticipant(id: String, city: String, email: String, callback: (Boolean) -> Unit){
        val db = Firebase.firestore

        db.runBatch { batch ->
            batch.update(db.collection("cities").document(city.toLowerCase()).collection("events").document(id), "users", FieldValue.arrayUnion(email))
            batch.update(db.collection("cities").document(city.toLowerCase()).collection("events").document(id), "currPartNumber", FieldValue.increment(1))
        }
                .addOnSuccessListener {
                    callback(true)
        }
                .addOnFailureListener{
                    callback(false)
        }
    }

    fun removeParticipant(id: String, city: String, email: String, callback: (Boolean) -> Unit){
        val db = Firebase.firestore

        db.runBatch { batch ->
            batch.update(db.collection("cities").document(city.toLowerCase()).collection("events").document(id), "users", FieldValue.arrayRemove(email))
            batch.update(db.collection("cities").document(city.toLowerCase()).collection("events").document(id), "currPartNumber", FieldValue.increment(-1))
        }
                .addOnSuccessListener {
                    callback(true)
                }
                .addOnFailureListener{
                    callback(false)
                }
    }


}