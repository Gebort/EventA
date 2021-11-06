package com.example.eventa

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

object User {
    lateinit var email: String
    lateinit var name: String
    var age: Int = 0
    lateinit var phone: String
    lateinit var description: String
    lateinit var city: String

    fun signout(){
        Firebase.auth.signOut()
    }
}