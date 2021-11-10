package com.example.eventa.mainFragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Selection.setSelection
import android.text.format.DateFormat.is24HourFormat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.example.eventa.DBHelper
import com.example.eventa.R
import com.example.eventa.User
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import java.text.SimpleDateFormat
import java.util.*

class OrgEvents : Fragment() {

    private lateinit var eventInput: EditText
    private lateinit var partNumbInput: EditText
    private lateinit var ageInput: EditText
    private lateinit var descInput: EditText
    private lateinit var locInput: EditText
    private lateinit var dateText: TextView
    private lateinit var timeText: TextView
    private lateinit var locSwitch: Switch
    private lateinit var publicSwitch: Switch
    private lateinit var emailSwitch: Switch
    private lateinit var phoneSwitch: Switch
    private lateinit var cityText: TextView
    private lateinit var createBut: Button
    private lateinit var dateBut: Button
    private lateinit var timeBut: Button
    private var loc = true
    private var public = false
    private var email = false
    private var phone = false
    private var date: Long = 0
    private var hour: Int = 0
    private var min: Int = 0

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val i = inflater.inflate(R.layout.fragment_org_events, container, false)

        activity?.title = "New event"

        eventInput = i.findViewById(R.id.titleInput)
        partNumbInput = i.findViewById(R.id.numberInput)
        ageInput = i.findViewById(R.id.ageInput)
        descInput = i.findViewById(R.id.descInput)
        locInput = i.findViewById(R.id.locInput)
        dateText = i.findViewById(R.id.dateText)
        timeText = i.findViewById(R.id.timeText)
        locSwitch = i.findViewById(R.id.locationSwitch)
        publicSwitch = i.findViewById(R.id.publicSwitch)
        emailSwitch = i.findViewById(R.id.emailSwitch)
        phoneSwitch = i.findViewById(R.id.numberSwitch)
        cityText = i.findViewById(R.id.cityText)
        createBut = i.findViewById(R.id.createEventBut)
        dateBut = i.findViewById(R.id.butDatePicker)
        timeBut = i.findViewById(R.id.butTimePicker)

        cityText.text = User.city
        locSwitch.isChecked = loc
        publicSwitch.isChecked = public
        emailSwitch.isChecked = email
        phoneSwitch.isChecked = phone

        locInput.isEnabled = loc
        cityText.isEnabled = loc
        createBut.isEnabled = true

        locSwitch.setOnClickListener{
            loc = !loc
            locInput.isEnabled = loc
            if(!loc) {
                locInput.setText(getText(R.string.no_place))
                cityText.setText(getText(R.string.no_city))
            }
            else {
                locInput.setText("")
                cityText.setText(User.city)
            }
            cityText.isEnabled = loc
        }

        dateBut.setOnClickListener {
            //TODO ограничение по времени
            val picker =
                    MaterialDatePicker.Builder.datePicker()
                            .setTitleText("Select your event date")
                            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                            .build()

            picker.addOnPositiveButtonClickListener { date: Long ->
                this.date = date
                val dateStr = Date(date)
                val format = SimpleDateFormat("dd.MM.yyyy")
                dateText.text =  format.format(dateStr)
            }

            fragmentManager?.let { it1 -> picker.show(it1, "datePicker") };



        }

        timeBut.setOnClickListener {

            val isSystem24Hour = is24HourFormat(activity?.applicationContext)
            val clockFormat = if (isSystem24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H

            val picker =
                MaterialTimePicker.Builder()
                    .setTimeFormat(clockFormat)
                    .setTitleText("Event starting time")
                    .build()

            picker.addOnPositiveButtonClickListener {
                val hour = picker.hour
                val min = picker.minute
                this.hour = hour
                this.min = min

                timeText.text = "$hour:$min"
            }

            fragmentManager?.let { it1 -> picker.show(it1, "timePicker") };
        }

        createBut.setOnClickListener {
            //TODO добавить обработку ввода
            createBut.isEnabled = false

            var city: String? = null
            if(loc)
                city = User.city

            DBHelper.fillEventData(
                    eventInput.text.toString(),
                    partNumbInput.text.toString().toInt(),
                    ageInput.text.toString().toInt(),
                    date,
                    hour,
                    min,
                    descInput.text.toString(),
                    city,
                    locInput.text.toString(),
                    publicSwitch.isChecked,
                    emailSwitch.isChecked,
                    phoneSwitch.isChecked,
                    User.name,
                    User.phone,
                    User.email,
                    ::onCreateResult
            )
        }

        return i
    }

    private fun onCreateResult(result: Boolean) {
        if (result) {
            try {
                findNavController().popBackStack()
            }
            catch (e: Exception){
                e.toString()
            }
        }
        else{

        }
    }

}