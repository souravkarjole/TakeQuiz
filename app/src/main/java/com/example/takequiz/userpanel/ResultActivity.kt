package com.example.takequiz.userpanel

import android.app.ActionBar.LayoutParams
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RatingBar
import android.widget.RatingBar.OnRatingBarChangeListener
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatRatingBar
import androidx.core.content.ContextCompat
import com.airbnb.lottie.LottieAnimationView
import com.example.takequiz.MainActivity
import com.example.takequiz.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ResultActivity : AppCompatActivity() {


    lateinit var dataBaseReference:DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        window.statusBarColor = ContextCompat.getColor(this,R.color.app_dark_color)

        val sadAnimation:LottieAnimationView = findViewById(R.id.sadAnimation)
        val congratsAnimation:LottieAnimationView = findViewById(R.id.congratsAnimation)
        var comment:TextView = findViewById(R.id.comment)
        var correctAnswers:TextView = findViewById(R.id.correctAnswers)
        var wrongAnswers:TextView = findViewById(R.id.wrongAnswers)
        var notAttempted:TextView = findViewById(R.id.notAttempted)
        var solvedQuestion:TextView = findViewById(R.id.solvedQuestions)
        var totalQuestion:TextView = findViewById(R.id.totalQuestions)
        var button:AppCompatButton = findViewById(R.id.newQuiz)


        var C = intent.getIntExtra("C",0)
        var W = intent.getIntExtra("W",0)
        var NA = intent.getIntExtra("NA",0)
        var TQ = intent.getIntExtra("TQ",0)


        if((C.toFloat().div(TQ.toFloat())*100 >= 65)){
            congratsAnimation.visibility = View.VISIBLE
        }else{
            sadAnimation.visibility = View.VISIBLE
            solvedQuestion.setTextColor(ContextCompat.getColor(this,R.color.red))
            comment.text = "Better luck next time"
        }

        correctAnswers.text = C.toString()
        wrongAnswers.text = W.toString()
        notAttempted.text = NA.toString()
        solvedQuestion.text = C.toString()
        totalQuestion.text = " / ${TQ}"


        button.setOnClickListener {
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }

        loadIntoFirebase()
    }

    private fun loadIntoFirebase(){
        dataBaseReference = FirebaseDatabase.getInstance().reference.child("Ratings")

        dataBaseReference.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.child(MainActivity.email.replace(Regex("[^a-zA-Z]"), "")).exists()) {
                    showRatingDialog()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
    private fun showRatingDialog(){
        val dialog:Dialog = Dialog(this)
        val view:View = layoutInflater.inflate(R.layout.rating,null)

        dialog.setContentView(view)

        val submitBtn:AppCompatButton = dialog.findViewById(R.id.submitBtn)
        val noThanks:AppCompatButton = dialog.findViewById(R.id.noThanks)
        val ratingBar:AppCompatRatingBar = dialog.findViewById(R.id.rating)


        ratingBar.setOnRatingBarChangeListener(object :OnRatingBarChangeListener{
            override fun onRatingChanged(rating: RatingBar?, changedRating: Float, p2: Boolean) {
                if(p2) {
                    rating?.rating = changedRating
                }
            }
        })


        submitBtn.setOnClickListener {
            dataBaseReference.child(MainActivity.email.replace(Regex("[^a-zA-Z]"), ""))
                .setValue(ratingBar.rating)
            dialog.dismiss()
        }

        noThanks.setOnClickListener {
            dialog.dismiss()
        }


        dialog.show()
        dialog.setCancelable(false)
        dialog.window?.setLayout(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
}