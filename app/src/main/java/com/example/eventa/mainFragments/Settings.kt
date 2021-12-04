package com.example.eventa.mainFragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.eventa.DBHelper
import com.example.eventa.loginFragments.LoginActivity
import com.example.eventa.R
import com.example.eventa.User
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

class Settings : Fragment() {
    private lateinit var savedBackground: Drawable

    private lateinit var butLogout: Button
    private lateinit var butUpdate: Button
    private lateinit var nameInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var birthInput: EditText
    private lateinit var descInput: EditText
    private lateinit var cityInput: EditText
    private lateinit var warningText: TextView
    private var birth: Long = 0

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val i = inflater.inflate(R.layout.fragment_settings, container, false)

        activity?.title = activity?.resources?.getString(R.string.profile)

        nameInput = i.findViewById(R.id.nameInput)
        phoneInput = i.findViewById(R.id.phoneInput)
        birthInput = i.findViewById(R.id.birthInput)
        descInput = i.findViewById(R.id.descInput)
        butLogout = i.findViewById(R.id.logout_but)
        butUpdate = i.findViewById(R.id.updateBut)
        warningText = i.findViewById(R.id.warningText)
        cityInput = i.findViewById(R.id.cityInput)

        nameInput.setText(User.name)
        phoneInput.setText(User.phone)
        birth = User.birth!!
        val birthStr = Date(User.birth!!)
        val format = SimpleDateFormat("dd.MM.yyyy")
        birthInput.setText(format.format(birthStr))
        descInput.setText(User.description)
        cityInput.setText(User.city)
        warningText.visibility = View.GONE

        savedBackground = nameInput.background

        uiEnabled(false)

        birthInput.setOnFocusChangeListener { _, b ->
            if (b) {
                val constraintsBuilder =
                        CalendarConstraints.Builder()
                                .setValidator(DateValidatorPointBackward.now())
                val picker =
                        MaterialDatePicker.Builder.datePicker()
                                .setTitleText(R.string.birth)
                                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                                .setCalendarConstraints(constraintsBuilder.build())
                                .build()

                picker.addOnPositiveButtonClickListener { birth: Long ->
                    this.birth = birth
                    val birthStr = Date(birth)
                    val format = SimpleDateFormat("dd.MM.yyyy")
                    birthInput.setText(format.format(birthStr))
                }

                fragmentManager?.let { it1 -> picker.show(it1, "birthPicker") }
                birthInput.isActivated = false
                birthInput.clearFocus()
            }
        }

        butUpdate.setOnClickListener {
            changeData()
        }

        butLogout.setOnClickListener {
            signOut()
        }

        return i
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val search = menu.findItem(R.id.action_search)
        search.isVisible = false
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun signOut(){
        User.signout()
        val intent = Intent(activity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    @SuppressLint("NewApi")
    private fun uiEnabled(status: Boolean){
            nameInput.isEnabled = status
            phoneInput.isEnabled = status
            birthInput.isEnabled = status
            descInput.isEnabled = status
            cityInput.isEnabled = status
    }

    private fun changeData(){
        butUpdate.text = "Confirm"
        uiEnabled(true)
        butUpdate.setOnClickListener {
            updateData()
        }
    }

    private fun updateData(){
        butUpdate.isEnabled = false
        uiEnabled(false)
        DBHelper.fillUserData(nameInput.text.toString(), User.email, phoneInput.text.toString(), birth, descInput.text.toString(), cityInput.text.toString(), ::onDataChanged)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onDataChanged(result: Boolean){
        butUpdate.isEnabled = true
        if(result){
            User.name = nameInput.text.toString()
            User.phone = phoneInput.text.toString()
            val instant = Instant.ofEpochMilli(User.birth!!)
            var dateSnap = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())
            var nowSnap = ZonedDateTime.now()
            User.age = nowSnap.year - dateSnap.year
            User.city = cityInput.text.toString()
            User.description = descInput.text.toString()
            butUpdate.text = "Change"
            warningText.visibility = View.GONE
            uiEnabled(false)
            butUpdate.setOnClickListener {
                changeData()
            }
        }
        else{
            warningText.text = getString(R.string.warning_failed_to_change_data)
            warningText.visibility = View.VISIBLE
            uiEnabled(true)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.action_search).isVisible = false
        super.onPrepareOptionsMenu(menu)
    }
}