package com.example.eventa.loginFragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.eventa.DBHelper
import com.example.eventa.R
import com.example.eventa.User
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

class RegistrationFragment : Fragment() {

    private lateinit var email: String
    private var googleRegistration: Boolean = true

    private lateinit var auth: FirebaseAuth

    private lateinit var emailInput: EditText
    private lateinit var nameInput: EditText
    private lateinit var passInput: EditText
    private lateinit var passInput2: EditText
    private lateinit var phoneInput: EditText
    private lateinit var birthInput: EditText
    private lateinit var cityInput: EditText
    private lateinit var descInput: EditText

    private lateinit var emailLayout: TextInputLayout
    private lateinit var nameLayout: TextInputLayout
    private lateinit var passLayout: TextInputLayout
    private lateinit var pass2Layout: TextInputLayout
    private lateinit var phoneLayout: TextInputLayout
    private lateinit var birthLayout: TextInputLayout
    private lateinit var cityLayout: TextInputLayout
    private lateinit var descLayout: TextInputLayout

    private lateinit var warningText: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var pass: String
    private lateinit var name: String
    private lateinit var phone: String
    private var birth: Long = -1
    private lateinit var desc: String
    private lateinit var city: String

    private lateinit var regBut: Button

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val inflate = inflater.inflate(R.layout.fragment_registration, container, false)

        emailInput = inflate.findViewById(R.id.emailReg)
        nameInput = inflate.findViewById(R.id.nameReg)
        passInput = inflate.findViewById(R.id.passReg)
        passInput2 = inflate.findViewById(R.id.pass2Reg)
        phoneInput = inflate.findViewById(R.id.phoneReg)
        birthInput = inflate.findViewById(R.id.birthReg)
        descInput = inflate.findViewById(R.id.descReg)
        cityInput = inflate.findViewById(R.id.cityReg)

        emailLayout = inflate.findViewById(R.id.emailLayout)
        nameLayout = inflate.findViewById(R.id.nameLayout)
        passLayout = inflate.findViewById(R.id.passLayout)
        pass2Layout = inflate.findViewById(R.id.pass2Layout)
        phoneLayout = inflate.findViewById(R.id.phoneLayout)
        birthLayout = inflate.findViewById(R.id.birthLayout)
        descLayout = inflate.findViewById(R.id.descLayout)
        cityLayout = inflate.findViewById(R.id.cityLayout)

        regBut = inflate.findViewById(R.id.registerBut)
        progressBar = inflate.findViewById(R.id.progressBar)
        warningText = inflate.findViewById(R.id.warningText)

        auth = Firebase.auth

        progressBar.visibility = View.GONE
        //На warningText крепится scrollView, поэтому нельзя его полностью убрать
        warningText.visibility = View.INVISIBLE

        val args: RegistrationFragmentArgs by navArgs()

        googleRegistration = !args.customRegistration

        if(googleRegistration){
            passLayout.visibility = View.GONE
            pass2Layout.visibility = View.GONE
            emailInput.setText(args.email)
            emailInput.isEnabled = false
        }

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

        regBut.setOnClickListener {
            if(checkInput()) {

                email = emailInput.text.toString()
                name = nameInput.text.toString()
                phone = phoneInput.text.toString()
                desc = descInput.text.toString()
                pass = passInput.text.toString()
                city = cityInput.text.toString()


                loadingBar(true)

                if (!googleRegistration) {
                    auth.createUserWithEmailAndPassword(email, pass)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    DBHelper.fillUserData(name, email, phone, birth, desc, city){ result ->
                                        onRegistrationResult(result)
                                    }
                                    sendVerificationEmail()
                                } else {
                                    loadingBar(false)
                                    warningText.text = getString(R.string.warning_registration_failed)
                                    warningText.isVisible = true
                                }
                            }
                } else {
                    DBHelper.fillUserData(name, email, phone, birth, desc, city){ result ->
                        onRegistrationResult(result)
                    }
                }
            }
        }

        return inflate
    }

    private fun sendVerificationEmail() {
        val user = FirebaseAuth.getInstance().currentUser

        user!!.sendEmailVerification()
    }

    private fun toLoginFragment(){
        val action = RegistrationFragmentDirections.actionRegistrationFragmentToLoginFragment()
        findNavController().navigate(action)
    }
//Callback отправляемый при результате внесения данных в firestore
    private fun onRegistrationResult(result: Boolean){
        if(result) {
            User.birth = birth
            User.description = desc
            User.email = email
            User.name = name
            User.phone = phone
            warningText.isVisible = false
            toLoginFragment()
        }
        else{
            loadingBar(false)
            warningText.isVisible = true
            warningText.text = getString(R.string.warning_registration_failed)
        }
    }

    private fun loadingBar(loading: Boolean){
        if(loading){
            progressBar.visibility = View.VISIBLE
            emailInput.isEnabled = false
            nameInput.isEnabled = false
            phoneInput.isEnabled = false
            birthInput.isEnabled = false
            descInput.isEnabled = false
            passInput.isEnabled = false
            passInput2.isEnabled = false
            cityInput.isEnabled = false
            regBut.isEnabled = false
        }
        else{
            progressBar.visibility = View.GONE
            emailInput.isEnabled = !googleRegistration
            nameInput.isEnabled = true
            phoneInput.isEnabled = true
            birthInput.isEnabled = true
            descInput.isEnabled = true
            cityInput.isEnabled = true

            if(!googleRegistration){
                passInput.isEnabled = true
                passInput2.isEnabled = true
            }

            regBut.isEnabled = true
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkInput(): Boolean{
        var result = true

        if(emailInput.text.toString() == ""){
            result = false
            emailLayout.error = getString(R.string.warning_email)
        }
        else{
            emailLayout.error = null
        }
        if(nameInput.text.toString() == ""){
            result = false
            nameLayout.error = getString(R.string.warning_name)
        }
        else{
            nameLayout.error = null
        }
        if(phoneInput.text.toString() == ""){
            result = false
            phoneLayout.error = getString(R.string.warning_phone)
        }
        else if(phoneInput.text.length < 11){
            result = false
            phoneLayout.error = getString(R.string.warning_phone_size)
        }
        else{
            phoneLayout.error = null
        }
        if(birthInput.text.toString() == ""){
            result = false
            birthInput.error = getString(R.string.warning_birth)
        }
        else{
            val instant = Instant.ofEpochMilli(birth)
            val birthSnap = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())
            val nowSnap = ZonedDateTime.now()
            if (nowSnap.year - birthSnap.year < 14) {
                result = false
                birthLayout.error = getString(R.string.warning_age_limit)
            }
            else{
                birthLayout.error = null
            }
        }
        if(passLayout.visibility == View.VISIBLE) {
            if (passInput.text.toString().length < 8) {
                result = false
                passLayout.error = getString(R.string.warning_password_size)
            }
            else{
                passLayout.error = null
            }
            if (passInput2.text.toString() != passInput.text.toString()) {
                result = false
                passLayout.error = getString(R.string.warning_passwords_match)
            }
            else{
                pass2Layout.error = null
            }
        }
        if(cityInput.text.toString() == ""){
            result = false
            cityLayout.error = getString(R.string.warning_city)
        }
        else{
            cityLayout.error = null
        }
        return result
    }
}