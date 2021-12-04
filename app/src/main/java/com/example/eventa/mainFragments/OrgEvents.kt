package com.example.eventa.mainFragments

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navArgument
import com.example.eventa.DBHelper
import com.example.eventa.Event
import com.example.eventa.R
import com.example.eventa.User
import com.example.eventa.viewModels.orgEventsViewModel
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

class OrgEvents : Fragment() {

    private var edit = false
    private var eventToChange: Event? = null

    private lateinit var eventInput: EditText
    private lateinit var partNumbInput: EditText
    private lateinit var ageInput: EditText
    private lateinit var descInput: EditText
    private lateinit var locInput: EditText
    private lateinit var dateInput: EditText
    private lateinit var timeInput: EditText
    private lateinit var eventLayout: TextInputLayout
    private lateinit var partNumbLayout: TextInputLayout
    private lateinit var ageLayout: TextInputLayout
    private lateinit var descLayout: TextInputLayout
    private lateinit var locLayout: TextInputLayout
    private lateinit var dateLayout: TextInputLayout
    private lateinit var timeLayout: TextInputLayout
    private lateinit var locSwitch: Switch
    private lateinit var publicSwitch: Switch
    private lateinit var emailSwitch: Switch
    private lateinit var phoneSwitch: Switch

    private lateinit var timeBut: Button
    private lateinit var cityText: TextView
    private lateinit var createBut: Button

    private lateinit var progressBar: ProgressBar

    private var loc = true
    private var public = false
    private var email = false
    private var phone = false
    private var date: Long = 0
    private var hour: Int = 0
    private var min: Int = 0

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val i = inflater.inflate(R.layout.fragment_org_events, container, false)

        val args: OrgEventsArgs by navArgs()
        edit = args.edit
        val index = args.eventIndex
        if (index != -1){
            val model: orgEventsViewModel by activityViewModels()
            eventToChange = model.getEvents().value?.get(index)
        }

        if (!edit) {
            activity?.title = activity?.resources?.getString(R.string.add_event)
        }
        else{
            activity?.title = activity?.resources?.getString(R.string.edit_event)
        }

        eventInput = i.findViewById(R.id.titleInput)
        partNumbInput = i.findViewById(R.id.numberInput)
        ageInput = i.findViewById(R.id.ageInput)
        descInput = i.findViewById(R.id.descInput)
        locInput = i.findViewById(R.id.locInput)
        dateInput = i.findViewById(R.id.datePickerInput)
        timeInput = i.findViewById(R.id.timePickerInput)

        eventLayout = i.findViewById(R.id.titleLayout)
        partNumbLayout = i.findViewById(R.id.numberLayout)
        ageLayout = i.findViewById(R.id.ageLayout)
        descLayout = i.findViewById(R.id.descLayout)
        locLayout = i.findViewById(R.id.locLayout)
        dateLayout = i.findViewById(R.id.datePickerLayout)
        timeLayout = i.findViewById(R.id.timePickerLayout)

        locSwitch = i.findViewById(R.id.locationSwitch)
        publicSwitch = i.findViewById(R.id.publicSwitch)
        emailSwitch = i.findViewById(R.id.emailSwitch)
        phoneSwitch = i.findViewById(R.id.numberSwitch)
        cityText = i.findViewById(R.id.cityText)
        createBut = i.findViewById(R.id.createEventBut)
        progressBar = i.findViewById(R.id.progressBar)

        cityText.text = User.city
        locSwitch.isChecked = loc
        publicSwitch.isChecked = public
        emailSwitch.isChecked = email
        phoneSwitch.isChecked = phone

        locInput.isEnabled = loc
        cityText.isEnabled = loc
        createBut.isEnabled = true

        progressBar.isVisible = false

        locSwitch.setOnClickListener{
            loc = !loc
            locEnabled(loc)
        }

        dateInput.setOnFocusChangeListener { _, b ->
            if (b) {
                val constraintsBuilder =
                        CalendarConstraints.Builder()
                                .setValidator(DateValidatorPointForward.now())
                val selection = if (!edit) MaterialDatePicker.todayInUtcMilliseconds() else eventToChange?.date
                val picker =
                        MaterialDatePicker.Builder.datePicker()
                                .setTitleText(R.string.pick_date)
                                .setSelection(selection)
                                .setCalendarConstraints(constraintsBuilder.build())
                                .build()

                picker.addOnPositiveButtonClickListener { date: Long ->
                    this.date = date
                    val dateStr = Date(date)
                    val format = SimpleDateFormat("dd.MM.yyyy")
                    dateInput.setText(format.format(dateStr))
                }

                fragmentManager?.let { it1 -> picker.show(it1, "datePicker") }
                dateInput.isActivated = false
                dateInput.clearFocus()
            }
        }

        timeInput.setOnFocusChangeListener { _, b ->
            if (b) {
                val isSystem24Hour = is24HourFormat(activity?.applicationContext)
                val clockFormat = if (isSystem24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H

                val picker =
                        MaterialTimePicker.Builder()
                                .setTimeFormat(clockFormat)
                                .setTitleText(R.string.pick_time)
                                .build()

                picker.addOnPositiveButtonClickListener {
                    this.hour = picker.hour
                    this.min = picker.minute
                    val time = DateTimeFormatter.ofPattern("HH:mm").format(LocalTime.of(hour, min))
                    timeInput.setText(time)
                }

                fragmentManager?.let { it1 -> picker.show(it1, "timePicker") }
                timeInput.isActivated = false
                timeInput.clearFocus()
            }
        }


        if (!edit){
            createBut.text = activity?.resources?.getString(R.string.add_event)
        }
        else{
            createBut.text = activity?.resources?.getString(R.string.edit_event)
            eventInput.setText(eventToChange?.title)
            partNumbInput.setText(eventToChange?.partNumber.toString())
            ageInput.setText(eventToChange?.minAge.toString())
            descInput.setText(eventToChange?.desc)
            val instant = eventToChange?.date?.let { Instant.ofEpochMilli(it) }
            var dateSnap = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())
            hour = dateSnap.hour
            min = dateSnap.minute
            date = dateSnap.minusHours(hour.toLong()).minusMinutes(min.toLong()).toEpochSecond()*1000  + TimeZone.getDefault().rawOffset - TimeZone.getDefault().dstSavings
            dateInput.setText(DateTimeFormatter.ofPattern("dd.MM.yyyy").format(dateSnap))
            timeInput.setText(DateTimeFormatter.ofPattern("HH.mm").format(dateSnap))
            if (eventToChange?.city == null){
                loc = false
                locEnabled(loc)
            }
            else{
                loc = true
                locEnabled(loc)
                cityText.setText(eventToChange?.city)
                locInput.setText(eventToChange?.loc)
            }
            locSwitch.isChecked = loc
            public = eventToChange?.public == true
            email = eventToChange?.showEmail == true
            phone = eventToChange?.showNumber == true
            publicSwitch.isChecked = public
            emailSwitch.isChecked = email
            phoneSwitch.isChecked = phone

        }


        createBut.setOnClickListener {
            if (checkInput()) {
                createBut.isEnabled = false
                progressBar.isVisible = true

                var city: String? = null
                if (loc)
                    city = User.city

                val dateSnapLocal = date + LocalTime.of(hour, min).toSecondOfDay()*1000
                val dateSnapUTC = dateSnapLocal - TimeZone.getDefault().rawOffset - TimeZone.getDefault().dstSavings
                val dateUpdateUTC = System.currentTimeMillis()

                val users = if (edit) eventToChange!!.users ?: listOf() else listOf()
                val requests = if (edit) eventToChange!!.requests ?: listOf() else listOf()

                val event = Event(
                        eventToChange?.id,
                        eventInput.text.toString(),
                        partNumbInput.text.toString().toInt(),
                        users.size,
                        ageInput.text.toString().toInt(),
                        dateSnapUTC,
                        descInput.text.toString(),
                        city,
                        locInput.text.toString(),
                        publicSwitch.isChecked,
                        emailSwitch.isChecked,
                        phoneSwitch.isChecked,
                        User.name,
                        User.phone,
                        User.email,
                        users,
                        requests,
                        false,
                        dateUpdateUTC
                )

                if (edit){
                    DBHelper.updateEventData(event){ result ->
                        onEditResult(result)
                    }
                }
                else{
                    DBHelper.fillEventData(event){ result ->
                        onCreateResult(result)
                }
                }
            }
        }

        return i
    }

    private fun onCreateResult(result: Boolean) {
        progressBar.isVisible = false
        if (result) {
            Snackbar.make(createBut, R.string.event_created, Snackbar.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
        else{
            Snackbar.make(createBut, R.string.event_created_error, Snackbar.LENGTH_SHORT).show()
            createBut.isEnabled = true
        }
    }

    private fun onEditResult(result: Boolean) {
        progressBar.isVisible = false
        if (result) {
            Snackbar.make(createBut, R.string.event_edited, Snackbar.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
        else{
            Snackbar.make(createBut, R.string.event_edited_error, Snackbar.LENGTH_SHORT).show()
            createBut.isEnabled = true
        }
    }

    private fun locEnabled(enabled: Boolean){
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

    private fun checkInput(): Boolean {
        var result = true

        if (eventInput.text.toString() == "") {
            result = false
            eventLayout.error = resources.getString(R.string.warning_required)
        } else {
            eventLayout.error = null
        }

        if (ageInput.text.toString() == "") {
            result = false
            ageLayout.error = resources.getString(R.string.warning_required)

        }
        else if (ageInput.text.toString().toInt() < 14){
            result = false
            ageLayout.error = resources.getString(R.string.warning_min_age)
        }
        else{
            ageLayout.error = null
        }

        if (partNumbInput.text.toString() == "" ) {
            result = false
            partNumbLayout.error = resources.getString(R.string.warning_required)

        }
        else if (partNumbInput.text.toString().toInt() < 1){
            result = false
            partNumbLayout.error = resources.getString(R.string.warning_min_participants)
        }
        else{
            partNumbLayout.error = null
        }

        if (dateInput.text.toString() == ""){
            result = false
            dateLayout.error = resources.getString(R.string.warning_required)
        }
        else{
            dateLayout.error = null
        }

        if (timeInput.text.toString() == ""){
            result = false
            timeLayout.error = resources.getString(R.string.warning_required)
        }
        else{
            timeLayout.error = null
        }

        if (descInput.text.toString() == ""){
            result = false
            descLayout.error = resources.getString(R.string.warning_required)
        }
        else{
            descLayout.error = null
        }

        if (loc){
            if (locInput.text.toString() == ""){
                result = false
                locLayout.error = resources.getString(R.string.warning_required)
            }
            else{
                locLayout.error = null
            }
        }
        else{
            locLayout.error = null
        }

        return result

    }

}