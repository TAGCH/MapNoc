package org.classapp.mapnoc_project

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.cardview.widget.CardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LogInViewActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mapApiKey = BuildConfig.MAPS_API_KEY
        Log.i("MAP_API_KEY", "MAP API KEY = " + mapApiKey)

        setContentView(R.layout.activity_log_in_view)

        auth = Firebase.auth

        val SendSignInbtn: Button = findViewById(R.id.SendSignInbtn)
        val signInPanel: CardView = findViewById(R.id.signInPanel)
        val LeadToSUbtn: TextView = findViewById(R.id.LeadToSUbtn)
        val signUpPanel: CardView = findViewById(R.id.signUpPannel)
        val LeadToSIbtn: TextView = findViewById(R.id.LeadToSIbtn)

        val usernameTxt: EditText = findViewById(R.id.usernameTxt)
        val pwdTxt: EditText = findViewById(R.id.pwdTxt)

        val usernameTxtSU: EditText = findViewById(R.id.usernameTxtSU)
        val pwdTxtSU: EditText = findViewById(R.id.pwdTxtSU)
        val pwdTxtSU2: EditText = findViewById(R.id.pwdTxtSU2)

        val signInBtn: Button = findViewById(R.id.signInBtn)

        signInBtn.setOnClickListener { signInPanel.visibility = View.VISIBLE }

        SendSignInbtn.setOnClickListener {
            val username = usernameTxt.text.toString()
            val password = pwdTxt.text.toString()
            signIn(username, password)
        }

        LeadToSUbtn.setOnClickListener {
            signInPanel.visibility = View.GONE
            signUpPanel.visibility = View.VISIBLE
        }

        LeadToSIbtn.setOnClickListener {
            signUpPanel.visibility = View.GONE
            signInPanel.visibility = View.VISIBLE
        }

        val SendSignUpbtn: Button = findViewById(R.id.SendSignUpbtn)
        SendSignUpbtn.setOnClickListener {
            val username = usernameTxtSU.text.toString()
            val password = pwdTxtSU.text.toString()
            val confirmPassword = pwdTxtSU2.text.toString()
            if (password == confirmPassword) {
                signUp(username, password)
            } else {
                // Show error message
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    // Proceed to next screen or activity
                    startActivity(Intent(this, MainActivity::class.java))
                    // Show toast message with email
                    Toast.makeText(this, "Welcome ${user?.email}!!", Toast.LENGTH_LONG).show()
                } else {
                    // If sign in fails, display a message to the user.
                    // Show error message
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signUp(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign up success, update UI with the signed-up user's information
                    val user = auth.currentUser
                    // Proceed to next screen or activity
                    startActivity(Intent(this, MainActivity::class.java))
                    // Show toast message with email
                    Toast.makeText(this, "Welcome ${user?.email}!!", Toast.LENGTH_LONG).show()
                } else {
                    // If sign up fails, display a message to the user.
                    // Show error message
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}


