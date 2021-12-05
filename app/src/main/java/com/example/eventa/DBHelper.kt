package com.example.eventa

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.eventa.viewModels.allEventsViewModel
import com.example.eventa.viewModels.followedEventsViewModel
import com.example.eventa.viewModels.orgEventsViewModel
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.w3c.dom.Document
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*
import kotlin.reflect.KFunction1

object DBHelper {

    private var avalEventsListener: ListenerRegistration? = null
    private var followedEventsListener: ListenerRegistration? = null
    private var orgEventsListener: ListenerRegistration? = null
    private val events = "events"

     @RequiresApi(Build.VERSION_CODES.O)
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
                         User.birth = result.get("birth").toString().toLong()
                         User.description = result.get("desc").toString()
                         User.city = result.get("city").toString()
                         val instant = Instant.ofEpochMilli(User.birth!!)
                         var dateSnap = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())
                         var nowSnap = ZonedDateTime.now()
                         User.age = nowSnap.year - dateSnap.year
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
            birth: Long,
            desc: String,
            city: String,
            callback: (Boolean) -> Unit)
    {
        val db = Firebase.firestore
        val docData = hashMapOf(
                "name" to name,
                "phone" to phone,
                "birth" to birth,
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

        val keywords = event.title?.split(" ")?.toTypedArray()?.toMutableList()
        keywords?.forEachIndexed { index, s ->
            keywords[index] = s.toLowerCase()
        }
        keywords?.add(" ")

        val docData = hashMapOf(
                "title" to event.title,
                "partNumber" to event.partNumber,
                "currPartNumber" to event.currPartNumber,
                "minAge" to event.minAge,
                "desc" to event.desc,
                "date" to event.date,
                "city" to event.city,
                "loc" to event.loc,
                "users" to event.users,
                "requests" to event.requests,
                "public" to event.public,
                "showEmail" to event.showEmail,
                "showNumber" to event.showNumber,
                "orgName" to event.orgName,
                "orgPhone" to event.orgPhone,
                "orgEmail" to event.orgEmail,
                "lastUpdate" to event.lastUpdate,
                "keywords" to keywords
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

        val keywords = event.title?.split(" ")?.toTypedArray()?.toMutableList()
        keywords?.forEachIndexed { index, s ->
            keywords[index] = s.toLowerCase()
        }
        keywords?.add(" ")

        val docData = hashMapOf(
                "title" to event.title,
                "partNumber" to event.partNumber,
                "currPartNumber" to event.currPartNumber,
                "minAge" to event.minAge,
                "desc" to event.desc,
                "date" to event.date,
                "city" to event.city,
                "loc" to event.loc,
                "users" to event.users,
                "requests" to event.requests,
                "public" to event.public,
                "showEmail" to event.showEmail,
                "showNumber" to event.showNumber,
                "orgName" to event.orgName,
                "orgPhone" to event.orgPhone,
                "orgEmail" to event.orgEmail,
                "lastUpdate" to event.lastUpdate,
                "keywords" to keywords
        )

        event.id?.let {
            db.collection(events).document(it).set(docData)
                .addOnSuccessListener {
                    callback(true)
                }
                .addOnFailureListener {
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadAvalEvents(city: String?, count: Long, keywords: MutableList<String>, callback: (Event, allEventsViewModel.Types) -> Unit) {
        val db = Firebase.firestore

        val now = ZonedDateTime.now(ZoneOffset.UTC)

        avalEventsListener?.remove()
        avalEventsListener = db.collection(events).whereEqualTo("city", city).whereArrayContainsAny("keywords", keywords).whereGreaterThan("date", now.toEpochSecond()*1000).orderBy("date").limit(count)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        Log.d("DBHelper", "Failed to load aval events: $error")
                    } else {
                        if (value != null) {
                                for (doc in value.documentChanges) {
                                    val event = doc.document.toObject(Event::class.java)
                                    event.id = doc.document.id
                                    if (event.users == null) {
                                        event.users = listOf()
                                    }
                                    if (event.requests == null) {
                                        event.requests = listOf()
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadOrganisedEvents(email: String, callback: (Event, Int, Int, orgEventsViewModel.Types) -> Unit){
        val db = Firebase.firestore

        orgEventsListener?.remove()
        val now = ZonedDateTime.now(ZoneOffset.UTC)

        orgEventsListener = db.collection(events).whereEqualTo("orgEmail", email).whereGreaterThan("date", now.toEpochSecond()*1000).orderBy("date")
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        Log.d("DBHelper", "Failed to load organised events: $error")
                    }
                    else{
                        if (value != null) {
                            for (doc in value.documentChanges) {
                                val event = doc.document.toObject(Event::class.java)
                                event.id = doc.document.id
                                if (event.users == null) {
                                    event.users = listOf()
                                }
                                when (doc.type) {
                                    DocumentChange.Type.ADDED -> {
                                        callback(event, doc.newIndex , doc.oldIndex, orgEventsViewModel.Types.ADDED)
                                    }
                                    DocumentChange.Type.MODIFIED -> {
                                        callback(event, doc.newIndex, doc.oldIndex, orgEventsViewModel.Types.MODIFIED)
                                    }
                                    DocumentChange.Type.REMOVED -> {
                                        callback(event, doc.oldIndex, doc.oldIndex, orgEventsViewModel.Types.REMOVED)
                                    }
                                }
                            }
                        }
                    }
                }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadFollowedEvents(email: String, callback: (Event, Int, Int, followedEventsViewModel.Types) -> Unit){
        val db = Firebase.firestore

        followedEventsListener?.remove()
        val now = ZonedDateTime.now(ZoneOffset.UTC)

        followedEventsListener = db.collection(events).whereArrayContains("users", email).whereGreaterThan("date", now.toEpochSecond()*1000).orderBy("date")
                .addSnapshotListener { value, error ->
                    if (error != null){
                        Log.d("DBHelper", "Failed to load followed events: $error")
                    }
                    else{
                        if (value != null) {
                            for (doc in value.documentChanges) {
                                val event = doc.document.toObject(Event::class.java)
                                event.id = doc.document.id
                                if (event.users == null) {
                                    event.users = listOf()
                                }
                                when (doc.type) {
                                    DocumentChange.Type.ADDED -> {
                                        callback(event, doc.newIndex, doc.oldIndex, followedEventsViewModel.Types.ADDED)
                                    }
                                    DocumentChange.Type.MODIFIED -> {
                                        callback(event, doc.newIndex, doc.oldIndex, followedEventsViewModel.Types.MODIFIED)
                                    }
                                    DocumentChange.Type.REMOVED -> {
                                        callback(event, doc.oldIndex, doc.oldIndex, followedEventsViewModel.Types.REMOVED)
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

    fun addRequest(id: String, email: String, callback: (Boolean) -> Unit){
        val db = Firebase.firestore

        db.runBatch { batch ->
            batch.update(db.collection(events).document(id), "requests", FieldValue.arrayUnion(email))
        }
                .addOnSuccessListener {
                    callback(true)
                }
                .addOnFailureListener{
                    callback(false)
                }
    }

    fun removeRequestAddParticipant(id: String, email: String, callback: (Boolean) -> Unit){
        val db = Firebase.firestore

        db.runBatch { batch ->
            batch.update(db.collection(events).document(id), "requests", FieldValue.arrayRemove(email))
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


}