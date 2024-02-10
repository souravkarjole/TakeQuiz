package com.example.takequiz

import android.app.ActionBar.LayoutParams
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.isVisible
import com.example.takequiz.adminpanel.HomeActivity
import com.example.takequiz.adminpanel.Authentication.LoginActivity
import com.example.takequiz.userpanel.StartingQuizActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    lateinit var userEmail:EditText
    lateinit var key:EditText
    lateinit var roomID:EditText

    var str_userEmail = "name"
    var str_key = "key"
    var str_roomID = "roomId"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        if(FirebaseAuth.getInstance().currentUser != null){
            startActivity(Intent(this,HomeActivity::class.java))
            finish()
        }else{
            showDialog()
        }
    }

    private fun showDialog() {
        val dialog: Dialog = Dialog(this)
        val view: View = layoutInflater.inflate(R.layout.dialog_box,null)

        dialog.setContentView(view)

        val adminBtn:AppCompatButton = dialog.findViewById(R.id.adminBtn)
        val userBtn:AppCompatButton = dialog.findViewById(R.id.userBtn)
        val userPanel:LinearLayout = dialog.findViewById(R.id.userCredentials)
        val save: TextView = dialog.findViewById(R.id.save)
        userEmail = dialog.findViewById(R.id.userEmail)
        key = dialog.findViewById(R.id.key)
        roomID = dialog.findViewById(R.id.roomId)

        adminBtn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        userBtn.setOnClickListener {
            if(!userPanel.isVisible){
                userPanel.visibility = View.VISIBLE
            }
        }

        var isChecked = false
        save.setOnClickListener {
            if(!isChecked && validation()){
                isChecked = true

                str_key = str_key.trim()
                str_roomID = str_roomID.trim()
                val dataReference:DatabaseReference = FirebaseDatabase.getInstance().reference.child("Admin").child(str_key).child(str_roomID)

                dataReference.addListenerForSingleValueEvent(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists()){
                            email = str_userEmail
                            val intent = Intent(applicationContext,StartingQuizActivity::class.java)
                            intent.putExtra("key",str_key)
                            intent.putExtra("ID",str_roomID)
                            startActivity(intent)
                            finish()
                        }else{
                            isChecked = false
                            Toast.makeText(applicationContext,"Invalid Key or ID",Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(applicationContext,"Invalid Key or ID",Toast.LENGTH_SHORT).show()
                    }

                })
            }
        }

        dialog.show()
        dialog.setCancelable(false)
        dialog.window?.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }


    private fun validation():Boolean{
        str_userEmail = userEmail.text.toString()
        str_key = key.text.toString()
        str_roomID = roomID.text.toString()


        if (str_userEmail.isEmpty()) {
            userEmail.error = "Please fill field"
            userEmail.requestFocus()
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(str_userEmail).matches()) {
            userEmail.error = "Please enter valid email"
            userEmail.requestFocus()
            return false
        }

        if (str_key.isEmpty()) {
            key.error = "Please fill field"
            key.requestFocus()
            return false
        }
        if (str_roomID.isEmpty()) {
            roomID.error = "Please fill field"
            roomID.requestFocus()
            return false
        }
        return true
    }

    companion object{
        var email = "abc@gmail.com"
    }
}