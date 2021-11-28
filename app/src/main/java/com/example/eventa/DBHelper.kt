package com.example.eventa

import android.util.Log
import com.example.eventa.viewModels.allEventsViewModel
import com.example.eventa.viewModels.followedEventsViewModel
import com.example.eventa.viewModels.orgEventsViewModel
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
    val events = "events"

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

    fun fillEventData(event: Event, callback: (Boolean) -> Unit)
    {
        val db = Firebase.firestore

        val docData = hashMapOf(
                "title" to event.title,
                "partNumber" to event.partNumber,
                "currPartNumber" to event.currPartNumber,
                "minAge" to event.minAge,
                "desc" to event.desc,
                "date" to event.date,
                "city" to event.city,
                "loc" to event.loc,
                "public" to event.public,
                "showEmail" to event.showEmail,
                "showNumber" to event.showNumber,
                "orgName" to event.orgName,
                "orgPhone" to event.orgPhone,
                "orgEmail" to event.orgEmail
        )

        db.collection(events).add(docData)
                .addOnSuccessListener {
                    callback(true)
                }
                .addOnFailureListener {
                    callback(false)
                }

    }

    fun updateEventData(event: Event, callback: (Boolean) -> Unit )   {
        val db = Firebase.firestore

        val docData = hashMapOf(
                "title" to event.title,
                "partNumber" to event.partNumber,
                "currPartNumber" to event.currPartNumber,
                "minAge" to event.minAge,
                "desc" to event.desc,
                "date" to event.date,
                "city" to event.city,
                "loc" to event.loc,
                "public" to event.public,
                "showEmail" to event.showEmail,
                "showNumber" to event.showNumber,
                "orgName" to event.orgName,
                "orgPhone" to event.orgPhone,
                "orgEmail" to event.orgEmail
        )

        event.id?.let {
            db.collection(events).document(it).set(docData)
                .addOnSuccessListener {
                    callback(true)
                }
                .addOnSuccessListener {
                    callback(false)
                }
        }

    }

    fun deleteEvent(id: String, callback: (Boolean) -> Unit){
        val db = Firebase.firestore

        db.collection(events).document(id).delete()
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener { e ->
                Log.d("DBHelper", "Failed to delete $id, error: $e")
                callback(false)
            }

    }

    fun loadAvalEvents(city: String?, count: Long, callback: (Event, allEventsViewModel.Types) -> Unit) {
        val db = Firebase.firestore

        avalEventsListener?.remove()

        avalEventsListener = db.collection(events).whereEqualTo("city", city).orderBy("date").limit(count)
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

    fun loadOrganisedEvents(email: String, callback: (Event, Int, orgEventsViewModel.Types) -> Unit){
        val db = Firebase.firestore

        orgEventsListener?.remove()

        orgEventsListener = db.collection(events).whereEqualTo("orgEmail", email).orderBy("date")
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        Log.d("DBHelper", "Failed to load organised events: $error")
                    }
                    else{
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
                                        callback(event, doc.newIndex , orgEventsViewModel.Types.ADDED)
                                    }
                                    DocumentChange.Type.MODIFIED -> {
                                        callback(event, doc.newIndex, orgEventsViewModel.Types.MODIFIED)
                                    }
                                    DocumentChange.Type.REMOVED -> {
                                        callback(event, doc.oldIndex, orgEventsViewModel.Types.REMOVED)
                                    }
                                }
                            }
                        }
                    }
                }

    }

    fun loadFollowedEvents(email: String, callback: (Event, Int, followedEventsViewModel.Types) -> Unit){
        val db = Firebase.firestore

        followedEventsListener?.remove()

        followedEventsListener = db.collection(events).whereArrayContains("users", email).orderBy("date")
                .addSnapshotListener { value, error ->
                    if (error != null){
                        Log.d("DBHelper", "Failed to load followed events: $error")
                    }
                    else{
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
                                        callback(event, doc.newIndex , followedEventsViewModel.Types.ADDED)
                                    }
                                    DocumentChange.Type.MODIFIED -> {
                                        callback(event, doc.newIndex, followedEventsViewModel.Types.MODIFIED)
                                    }
                                    DocumentChange.Type.REMOVED -> {
                                        callback(event, doc.oldIndex, followedEventsViewModel.Types.REMOVED)
                                    }
                                }
                            }
                        }
                    }
                }
    }

    fun addParticipant(id: String, email: String, callback: (Boolean) -> Unit){
        val db = Firebase.firestore

        db.runBatch { batch ->
            batch.update(db.collection(events).document(id), "users", FieldValue.arrayUnion(email))
            batch.update(db.collection(events).document(id), "currPartNumber", FieldValue.increment(1))
        }
                .addOnSuccessListener {
                    callback(true)
        }
                .addOnFailureListener{
                    callback(false)
        }
    }

    fun removeParticipant(id: String, email: String, callback: (Boolean) -> Unit){
        val db = Firebase.firestore

        db.runBatch { batch ->
            batch.update(db.collection(events).document(id), "users", FieldValue.arrayRemove(email))
            batch.update(db.collection(events).document(id), "currPartNumber", FieldValue.increment(-1))
        }
                .addOnSuccessListener {
                    callback(true)
                }
                .addOnFailureListener{
                    callback(false)
                }
    }


}