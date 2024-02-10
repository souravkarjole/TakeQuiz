package com.example.takequiz.adminpanel.Authentication

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.example.takequiz.R
import com.example.takequiz.adminpanel.HomeActivity
import com.google.firebase.auth.FirebaseAuth


class LoginActivity : AppCompatActivity() {

    lateinit var loginBtn: AppCompatButton
    lateinit var createAccountBtn: TextView
    lateinit var auth: FirebaseAuth
    lateinit var email: EditText
    lateinit var password: EditText


    var str_email = "email"
    var str_pass = "pass"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        window.statusBarColor = ContextCompat.getColor(this,R.color.app_dark_color)

        loginBtn = findViewById(R.id.loginBtn)
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        createAccountBtn = findViewById(R.id.createAccount)
        auth = FirebaseAuth.getInstance()

        createAccountBtn.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        var isClicked = false

        loginBtn.setOnClickListener {
            if (!isClicked && Validation()) {
                isClicked = true
                auth.signInWithEmailAndPassword(str_email, str_pass)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            startActivity(Intent(this, HomeActivity::class.java))
                        } else {
                            // If sign in fails, display a message to the user.
                            isClicked = false
                            Toast.makeText(
                                baseContext,
                                "Incorrect password or email not registered",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
            }
        }
    }


    fun Validation(): Boolean {
        str_email = email.text.toString()
        str_pass = password.text.toString()

        if (str_email.isEmpty()) {
            email.error = "Please fill field"
            email.requestFocus()
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(str_email).matches()) {
            email.error = "Please enter valid email"
            email.requestFocus()
            return false
        }
        if (str_pass.isEmpty()) {
            password.error = "Please fill field"
            password.requestFocus()
            return false
        } else if (!passwordValidation(str_pass)) {
            password.error = "Enter minimum 7 digits"
            password.requestFocus()
            return false
        }
        return true
    }

    fun passwordValidation(strPass: String): Boolean {
        if (strPass.length < 7) {
            return false
        }
        return true
    }

}