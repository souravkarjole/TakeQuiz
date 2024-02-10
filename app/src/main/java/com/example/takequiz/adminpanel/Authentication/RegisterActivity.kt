package com.example.takequiz.adminpanel.Authentication

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.os.Bundle
import android.util.Patterns
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.example.takequiz.R
import com.example.takequiz.adminpanel.HomeActivity
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    lateinit var auth:FirebaseAuth
    lateinit var createBtn: AppCompatButton
    lateinit var adminName: EditText
    lateinit var email: EditText
    lateinit var password: EditText

    var str_email = "email"
    var str_pass = "pass"
    var name = "name"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        window.statusBarColor = ContextCompat.getColor(this,R.color.app_dark_color)

        adminName = findViewById(R.id.adminName)
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        createBtn = findViewById(R.id.createBtn)

        auth = FirebaseAuth.getInstance()

        var isClicked = false

        createBtn.setOnClickListener {

            if(!isClicked && Validation()) {
                isClicked = true

                auth.createUserWithEmailAndPassword(str_email, str_pass)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            val sharedPreferences: SharedPreferences = getSharedPreferences("ADMIN_NAME",
                                MODE_PRIVATE)

                            val editor:Editor = sharedPreferences.edit()
                            editor.putString("NAME",adminName.toString())
                            editor.apply()

                            var intent = Intent(this, HomeActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            finish()
                        } else {
                            // If sign in fails, display a message to the user.
                            isClicked = false
                            Toast.makeText(
                                baseContext,
                                "Email already registered or server problem",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
            }
        }
    }


    fun Validation():Boolean {
        name = adminName.text.toString()
        str_email = email.text.toString()
        str_pass = password.text.toString()

        if(name.isEmpty()){
            adminName.error = "Enter your name"
            adminName.requestFocus()
            return false
        }

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
        if(strPass.length < 7){
            return false
        }
        return true
    }
}