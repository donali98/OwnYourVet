package com.pdm.ownyourvet.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.pdm.ownyourvet.*
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {

    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val mGoogleApiClient: GoogleApiClient by lazy { getGoogleApiClient() }
    private  val RC_GOOGLE_SIGN_IN = 99

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        button_LogIn.setOnClickListener {
            val email = editText_Email.text.toString()
            val password = editText_Password.text.toString()
            if (isValidEmail(email) && isValidPassword(password)){
                logInByEmail(email, password)
            } else {
                toast("Please make sure all the data is correct")
            }
        }

        textView_ForgotPassword.setOnClickListener {
            goToActivity<ForgotPasswordActivity> {  }
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }

        button_CreateAccount.setOnClickListener {
            goToActivity<SignUpActivity> {  }
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }

        editText_Email.validate {
            editText_Email.error = if (isValidEmail(it)) null else "Email is not valid"
        }

        editText_Password.validate {
            editText_Password.error = if (isValidPassword(it)) null else "Password must contain 1 lowercase, 1 uppercase, 1 number, 1 special character and 6 characters length at least"
        }

        button_LogInGoogle.setOnClickListener {
            val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
            startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)
        }

    }

    private fun getGoogleApiClient():GoogleApiClient{
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleApiClient.Builder(this)
            .enableAutoManage(this,this)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build()
    }

    private fun logInByEmail(email:String, password:String){
        mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(this){task ->
                    if (task.isSuccessful){
                        if (mAuth.currentUser!!.isEmailVerified){
                            goToActivity<MainActivity>()
                            toast("User is now logged in.")
                        } else {
                            toast("User must confirm email first.")
                        }
                    } else {
                        toast("An unexpected error occurred, please try again.")
                    }
                }
    }

    private fun loginByGoogleAccountIntoFirebase(googleAccount:GoogleSignInAccount){
        val credential = GoogleAuthProvider.getCredential(googleAccount.idToken, null)
        mAuth.signInWithCredential(credential).addOnCompleteListener(this){
            if (mGoogleApiClient.isConnected){
                Auth.GoogleSignInApi.signOut(mGoogleApiClient)
            }
            goToActivity<MainActivity> {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            toast("Signed In by Google")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_GOOGLE_SIGN_IN){
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess){
                val account = result.signInAccount
                loginByGoogleAccountIntoFirebase(account!!)
            }
        }
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        toast("Connection Failed!!")
    }
}