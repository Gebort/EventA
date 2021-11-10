package com.example.eventa

data class Event(
        var id: String? = "",
        var title: String? = "",
        var partNumber: Int = -1,
        var currPartNumber: Int = -1,
        var minAge: Int = -1,
        var date: Long = -1,
        var hour: Int = -1,
        var min: Int = -1,
        var desc: String? = "",
        var city: String? = "",
        var loc: String? = null,
        var public: Boolean = false,
        var showEmail: Boolean = false,
        var showNumber: Boolean = false,
        var orgName: String? = "",
        var orgPhone: String? = "",
        var orgEmail: String? = "",
        var users: List<String>? = null
)