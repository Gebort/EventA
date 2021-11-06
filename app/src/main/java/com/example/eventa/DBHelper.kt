package com.example.eventa

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.reflect.KFunction1

object DBHelper {

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

    fun loadAvalEvents(email: String, city: String, age: Int, callback: (Boolean, List<Event>?) -> Unit) {

        val db = Firebase.firestore
        val events = mutableListOf<Event>()
        //TODO Добавить проверку, что пользователь не подписан на событие
        db.collection("cities").document(city.toLowerCase(Locale.ROOT)).collection("events").whereLessThanOrEqualTo("minAge", age)
                .get()
                .addOnSuccessListener { docs ->
                    for (doc in docs) {
                        val event = doc.toObject(Event::class.java)
                        if(event.orgEmail!! != email) {
                            if (event.users == null || !event.users!!.contains(email)) {
                                if (event.currPartNumber < event.partNumber && event.minAge <= age) {
                                    event.id = doc.id
                                    event.city = city.toLowerCase(Locale.ROOT)
                                    events.add(event)
                                }
                            }
                        }
                    }
                    events.sortBy { it.date }
                    callback(true, events)
                }
                .addOnFailureListener {
                    callback(false, null)
                }

    }

    fun loadOrganisedEvents(email: String, callback: (Boolean, List<Event>?) -> Unit){

//        val db = Firebase.firestore
//        val events = mutableListOf<Event>()
//        db.collection("cities").document(User.city.toLowerCase()).collection("events").whereEqualTo("orgEmail", email)
//                .get()
//                .addOnSuccessListener { docs ->
//                    for (doc in docs){
//                        val event = doc.toObject(Event::class.java)
//                        event.id = doc.id
//                        event.city = User.city.toLowerCase()
//                        events.add(event)
//                    }
//                    events.sortBy { it.date }
//                    callback(true, events)
//                }
//                .addOnFailureListener {
//                    callback(false, null)
//                }

        //TODO поиск не только по городу пользователя но и по none
        val db = Firebase.firestore
        db.collection("cities").document(User.city.toLowerCase(Locale.ROOT)).collection("events").whereEqualTo("orgEmail", email)
                .addSnapshotListener { value, error ->
                    val events = mutableListOf<Event>()
                    if (error != null) {
                        Log.d("DBHelper", "Failed to load organised events: ${error}")
                        callback(false, null)
                    }
                    else if (value != null) {
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
        val events = mutableListOf<Event>()
        db.collection("cities").document(User.city.toLowerCase()).collection("events").whereArrayContains("users", email)
                .get()
                .addOnSuccessListener { docs ->
                    for (doc in docs){
                        val event = doc.toObject(Event::class.java)
                        event.id = doc.id
                        event.city = User.city.toLowerCase()
                        events.add(event)
                    }
                    events.sortBy { it.date }
                    callback(true, events)
                }
                .addOnFailureListener {
                    callback(false, null)
                }

    }

    fun addParticipant(id: String, city: String, email: String, callback: (Boolean) -> Unit){
        val db = Firebase.firestore

        val docData = hashMapOf<String, String>()

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

//        db.collection("cities").document(city.toLowerCase()).collection("events").document(id).collection("participants").document(email).set(docData)
//                .addOnSuccessListener {
//                    db.collection("cities").document(city.toLowerCase()).collection("events").document(id)
//                            .update("currPartNumber", FieldValue.increment(1))
//
//                    callback(true)
//                }
//                .addOnFailureListener {
//                    callback(false)
//                }
    }


}