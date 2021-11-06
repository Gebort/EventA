package com.example.eventa.loginFragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.eventa.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegistrationFragment : Fragment() {

    private lateinit var email: String
    private var customRegistration: Boolean = true

    private lateinit var auth: FirebaseAuth

    private lateinit var emailInput: EditText
    private lateinit var nameInput: EditText
    private lateinit var passInput: EditText
    private lateinit var passInput2: EditText
    private lateinit var phoneInput: EditText
    private lateinit var ageInput: EditText
    private lateinit var cityInput: EditText
    private lateinit var descInput: EditText
    private lateinit var emailText: TextView
    private lateinit var nameText: TextView
    private lateinit var passText: TextView
    private lateinit var passText2: TextView
    private lateinit var phoneText: TextView
    private lateinit var ageText: TextView
    private lateinit var cityText: TextView
    private lateinit var descText: TextView
    private lateinit var warningText: TextView
    private lateinit var progressBar: ProgressBar

    private lateinit var pass: String
    private lateinit var name: String
    private lateinit var phone: String
    private var age: Int = -1
    private lateinit var desc: String
    private lateinit var city: String

    private lateinit var regBut: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val inflate = inflater.inflate(R.layout.fragment_registration, container, false)

        emailInput = inflate.findViewById(R.id.emailReg)
        nameInput = inflate.findViewById(R.id.nameReg)
        passInput = inflate.findViewById(R.id.passReg)
        passInput2 = inflate.findViewById(R.id.passReg2)
        phoneInput = inflate.findViewById(R.id.phoneReg)
        ageInput = inflate.findViewById(R.id.ageReg)
        descInput = inflate.findViewById(R.id.descReg)
        cityInput = inflate.findViewById(R.id.cityReg)

        emailText = inflate.findViewById(R.id.emailText)
        nameText = inflate.findViewById(R.id.nameText)
        passText = inflate.findViewById(R.id.passwordText)
        passText2 = inflate.findViewById(R.id.passwordText2)
        phoneText = inflate.findViewById(R.id.phoneText)
        ageText = inflate.findViewById(R.id.ageText)
        descText = inflate.findViewById(R.id.descText)
        cityText = inflate.findViewById(R.id.cityText)
        warningText = inflate.findViewById(R.id.warningText)

        regBut = inflate.findViewById(R.id.registerBut)
        progressBar = inflate.findViewById(R.id.progressBar)

        auth = Firebase.auth

        progressBar.visibility = View.GONE
        //На warningText крепится scrollView, поэтому нельзя его полностью убрать
        warningText.visibility = View.INVISIBLE

        val args: RegistrationFragmentArgs by navArgs()

        customRegistration = !args.customRegistration

        if(customRegistration){
            passInput.visibility = View.GONE
            passInput2.visibility = View.GONE
            passText.visibility = View.GONE
            passText2.visibility = View.GONE
            emailInput.setText(args.email)
            emailInput.isEnabled = false
        }

        regBut.setOnClickListener {
            if(checkInput()) {

                email = emailInput.text.toString()
                name = nameInput.text.toString()
                phone = phoneInput.text.toString()
                age = ageInput.text.toString().toInt()
                desc = descInput.text.toString()
                pass = passInput.text.toString()
                city = cityInput.text.toString()


                loadingBar(true)

                if (args.customRegistration) {
                    auth.createUserWithEmailAndPassword(email, pass)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    DBHelper.fillUserData(name, email, phone, age, desc, city, ::onRegistrationResult)
                                    sendVerificationEmail()
                                } else {
                                    loadingBar(false)
                                    val error = task.exception
                                    val foo = 2
                                    warningText.text = getString(R.string.warning_registration_failed)
                                }
                            }
                } else {
                    DBHelper.fillUserData(name, email, phone, age, desc, city, ::onRegistrationResult)
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
//Callback отправляемый в регистрацию через гугл
    private fun onRegistrationResult(result: Boolean){
        if(result) {
            User.age = age
            User.description = desc
            User.email = email
            User.name = name
            User.phone = phone
            toLoginFragment()
        }
        else{
            loadingBar(false)
            warningText.text = getString(R.string.warning_registration_failed)
        }
    }

    private fun loadingBar(loading: Boolean){
        if(loading){
            progressBar.visibility = View.VISIBLE
            emailInput.isEnabled = false
            nameInput.isEnabled = false
            phoneInput.isEnabled = false
            ageInput.isEnabled = false
            descInput.isEnabled = false
            passInput.isEnabled = false
            passInput2.isEnabled = false
            cityInput.isEnabled = false
            regBut.isEnabled = false
        }
        else{
            progressBar.visibility = View.GONE
            emailInput.isEnabled = customRegistration
            nameInput.isEnabled = true
            phoneInput.isEnabled = true
            ageInput.isEnabled = true
            descInput.isEnabled = true
            cityInput.isEnabled = true

            if(customRegistration){
                passInput.isEnabled = true
                passInput2.isEnabled = true
            }

            regBut.isEnabled = true
        }

    }

    private fun checkInput(): Boolean{
        var result = true

        if(emailInput.text.toString() == ""){
            result = false
            emailText.text = getString(R.string.warning_email)
            emailText.setTextColor(Color.RED)
        }
        if(nameInput.text.toString() == ""){
            result = false
            nameText.text = getString(R.string.warning_name)
            nameText.setTextColor(Color.RED)
        }
        if(phoneInput.text.toString() == ""){
            result = false
            phoneText.text = getString(R.string.warning_phone)
            phoneText.setTextColor(Color.RED)
        }
        if(ageInput.text.toString() == ""){
            result = false
            ageText.text = getString(R.string.warning_age)
            ageText.setTextColor(Color.RED)
        }
        else if(ageInput.text.toString().toInt() < 14){
            result = false
            ageText.text = getString(R.string.warning_age_limit)
            ageText.setTextColor(Color.RED)
        }
        if(descInput.text.toString().length > 200){
            result = false
            descText.text = getString(R.string.warning_desc_size)
            descText.setTextColor(Color.RED)
        }
        if(passInput.visibility == View.VISIBLE) {
            if (passInput.text.toString().length < 8) {
                result = false
                passText.text = getString(R.string.warning_password_size)
                passText.setTextColor(Color.RED)
            }
            if (passInput2.text.toString() != passInput.text.toString()) {
                result = false
                passText2.text = getString(R.string.warning_passwords_match)
                passText2.setTextColor(Color.RED)
            }
        }
        if(cityInput.text.toString() == ""){
            result = false
            cityText.text = getString(R.string.warning_city)
            cityText.setTextColor(Color.RED)
        }
        return result
    }
}