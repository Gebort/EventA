package com.example.eventa.loginFragments

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.eventa.DBHelper
import com.example.eventa.R
import com.example.eventa.mainFragments.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginFragment : Fragment() {

    private lateinit var googleBut: ImageButton
    private lateinit var loginBut: Button
    private lateinit var regBut: Button
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var emailLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var warningText: TextView
    private lateinit var sendVerifyBut: Button

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private var email: String? = null

    private val RC_SIGN_IN = 1

    private val dTAG = "LOGIN"

    private lateinit var inflate: View

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        inflate = inflater.inflate(R.layout.fragment_login, container, false)

        loginBut = inflate.findViewById(R.id.loginBut)
        googleBut = inflate.findViewById(R.id.googleBut)
        regBut = inflate.findViewById(R.id.regBut)
        emailInput = inflate.findViewById(R.id.emailInput)
        passwordInput = inflate.findViewById(R.id.passwordInput)
        emailLayout = inflate.findViewById(R.id.loginLayout)
        passwordLayout = inflate.findViewById(R.id.passwordLayout)
        progressBar = inflate.findViewById(R.id.progressBar)
        warningText = inflate.findViewById(R.id.warningText)
        sendVerifyBut = inflate.findViewById(R.id.butSendAgain)

        warningText.visibility = View.GONE
        sendVerifyBut.visibility = View.GONE
        loadingBar(false)

        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(activity, gso)
        auth = Firebase.auth

        googleBut.setOnClickListener {
            signInGoogle()
        }

        loginBut.setOnClickListener {
            signInPassword()
        }

        regBut.setOnClickListener {
            toRegFragment()
        }

        val currentUser = auth.currentUser

        if(currentUser != null){
            emailInput.setText(currentUser.email)
            if(currentUser.isEmailVerified) {
                loadingBar(true)
                updateUI(currentUser)
                email = currentUser.email
                email?.let { DBHelper.emailCheck(it, ::onEmailCheckResultAuto) }
            }
            else {
                loadingBar(false)
                warningText.visibility = View.VISIBLE
                warningText.text = getString(R.string.warning_verificate_account)
                sendVerifyBut.visibility = View.VISIBLE
                sendVerifyBut.setOnClickListener{
                    currentUser.sendEmailVerification()
                    Snackbar.make(loginBut, R.string.verification_letter_resend, Snackbar.LENGTH_SHORT).show()

                }
            }
        }

        return inflate
    }

    private fun signInGoogle(){
        googleSignInClient.signOut()
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
        loadingBar(true)
    }

    private fun signInPassword(){
        val login = emailInput.text.toString()
        val password = passwordInput.text.toString()

        if(login == "" && password == "")
            return

        loadingBar(true)

        auth.signInWithEmailAndPassword(login, password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        emailLayout.error = null
                        passwordLayout.error = null
                        if (currentUser.isEmailVerified) {
                            email = currentUser.email
                            warningText.visibility = View.GONE
                            email?.let { DBHelper.emailCheck(it){ result ->
                                onEmailCheckResult(result)
                            } }
                        }
                        else{
                            warningText.visibility = View.VISIBLE
                            warningText.text = getString(R.string.warning_verificate_account)
                            sendVerifyBut.visibility = View.VISIBLE
                            sendVerifyBut.setOnClickListener{
                                currentUser.sendEmailVerification()
                                Snackbar.make(loginBut, R.string.verification_letter_resend, Snackbar.LENGTH_SHORT).show()

                            }
                        }
                    }
                }
                else{
                    val error = task.exception
                    loadingBar(false)
                    emailLayout.error = getText(R.string.warning_invalid_login)
                    passwordLayout.error = getText(R.string.warning_invalid_login)
                }
            }

    }

    private fun signOut(){
        Firebase.auth.signOut()
    }

    private fun toMainActivity(){
        val intent = Intent(activity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun toRegFragment(email: String? = null, custom: Boolean = true){
        val action = LoginFragmentDirections.actionLoginFragmentToRegistrationFragment(email)
        action.email = email
        action.customRegistration = custom
        findNavController().navigate(action)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d(dTAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                loadingBar(false)
                warningText.text = getString((R.string.warning_google_signin_failed))
                warningText.visibility = View.VISIBLE
                Log.w(dTAG, "Google sign in failed", e)
            }
        }
    }

    private fun updateUI(user: FirebaseUser?){
        if(user != null) {
            emailInput.setText(user.displayName)
            passwordLayout.visibility = View.INVISIBLE
        }
        else{
            emailInput.setText("")
            passwordLayout.visibility = View.VISIBLE
        }
    }

    private fun loadingBar(loading: Boolean){
        if(loading){
            progressBar.visibility = View.VISIBLE
            emailInput.isEnabled = false
            passwordInput.isEnabled = false
            googleBut.isEnabled = false
            loginBut.isEnabled = false
            regBut.isEnabled = false
        }
        else{
            progressBar.visibility = View.GONE
            emailInput.isEnabled = true
            passwordInput.isEnabled = true
            googleBut.isEnabled = true
            loginBut.isEnabled = true
            regBut.isEnabled = true
        }

    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        activity?.let {
            auth.signInWithCredential(credential)
                .addOnCompleteListener(it) { task ->
                    if (task.isSuccessful) {
                        Log.d(dTAG, "signInWithCredential:success")
                        warningText.visibility = View.GONE
                        sendVerifyBut.visibility = View.GONE
                        email = auth.currentUser?.email
                        if (email != null) {
                            DBHelper.emailCheck(email!!, ::onEmailCheckResult)
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(dTAG, "signInWithCredential:failure", task.exception)
                        loadingBar(false)
                        warningText.text = getString((R.string.warning_google_signin_failed))
                        warningText.visibility = View.VISIBLE
                        updateUI(null)
                    }
                }
        }
    }

    private fun onEmailCheckResult(result: Boolean){
        if(result){
            toMainActivity()
        }
        else{
            email?.let { toRegFragment(email, false) }
        }
    }

    private fun onEmailCheckResultAuto(result: Boolean){
        if(result){
            toMainActivity()
        }
        else{
            Log.w(dTAG, "No data about user")
            loadingBar(false)
            updateUI(null)
        }
    }
}