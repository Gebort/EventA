package com.example.eventa.mainFragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.eventa.DBHelper
import com.example.eventa.loginFragments.LoginActivity
import com.example.eventa.R
import com.example.eventa.User

class Settings : Fragment() {
    private lateinit var savedBackground: Drawable

    private lateinit var butLogout: Button
    private lateinit var butUpdate: Button
    private lateinit var nameInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var ageInput: EditText
    private lateinit var descInput: EditText
    private lateinit var cityInput: EditText
    private lateinit var warningText: TextView

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val i = inflater.inflate(R.layout.fragment_settings, container, false)

        activity?.title = "Profile"

        nameInput = i.findViewById(R.id.nameInput)
        phoneInput = i.findViewById(R.id.phoneInput)
        ageInput = i.findViewById(R.id.ageInput)
        descInput = i.findViewById(R.id.descInput)
        butLogout = i.findViewById(R.id.logout_but)
        butUpdate = i.findViewById(R.id.updateBut)
        warningText = i.findViewById(R.id.warningText)
        cityInput = i.findViewById(R.id.cityInput)

        nameInput.setText(User.name)
        phoneInput.setText(User.phone)
        ageInput.setText(User.age.toString())
        descInput.setText(User.description)
        cityInput.setText(User.city)
        warningText.visibility = View.GONE

        savedBackground = nameInput.background

        uiEnabled(false)

        butUpdate.setOnClickListener {
            changeData()
        }

        butLogout.setOnClickListener {
            signOut()
        }

        return i
    }

    private fun signOut(){
        User.signout()
        val intent = Intent(activity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
//        val action = SettingsDirections.actionSettingsToLoginActivity()
//        action.
//        findNavController().navigate(action)
    }

    @SuppressLint("NewApi")
    private fun uiEnabled(status: Boolean){
        if(status) {
            nameInput.isFocusableInTouchMode = true
            phoneInput.isFocusableInTouchMode = true
            ageInput.isFocusableInTouchMode = true
            descInput.isFocusableInTouchMode = true
            cityInput.isFocusableInTouchMode = true
            nameInput.background = savedBackground
            phoneInput.background = savedBackground
            ageInput.background = savedBackground
            descInput.background = savedBackground
            cityInput.background = savedBackground

        }
        else{
            nameInput.focusable = View.NOT_FOCUSABLE
            phoneInput.focusable = View.NOT_FOCUSABLE
            ageInput.focusable = View.NOT_FOCUSABLE
            descInput.focusable = View.NOT_FOCUSABLE
            cityInput.focusable = View.NOT_FOCUSABLE
            nameInput.setBackgroundColor(Color.TRANSPARENT)
            phoneInput.setBackgroundColor(Color.TRANSPARENT)
            descInput.setBackgroundColor(Color.TRANSPARENT)
            ageInput.setBackgroundColor(Color.TRANSPARENT)
            cityInput.setBackgroundColor(Color.TRANSPARENT)
        }
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
        DBHelper.fillUserData(nameInput.text.toString(), User.email, phoneInput.text.toString(), ageInput.text.toString().toInt(), descInput.text.toString(), cityInput.text.toString(), ::onDataChanged)
    }

    private fun onDataChanged(result: Boolean){
        butUpdate.isEnabled = true
        if(result){
            User.name = nameInput.text.toString()
            User.phone = phoneInput.text.toString()
            User.age = ageInput.text.toString().toInt()
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
}