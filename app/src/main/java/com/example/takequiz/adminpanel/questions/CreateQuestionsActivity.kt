package com.example.takequiz.adminpanel.questions

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.example.takequiz.R
import com.example.takequiz.adminpanel.HomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CreateQuestionsActivity : AppCompatActivity() {

    lateinit var question:EditText
    lateinit var option_1:EditText
    lateinit var option_2:EditText
    lateinit var option_3:EditText
    lateinit var option_4:EditText
    lateinit var correctAnswer:EditText
    lateinit var databaseReference:DatabaseReference

    var str_question = "question"
    var str_option_1 = "1"
    var str_option_2 = "1"
    var str_option_3 = "1"
    var str_option_4 = "1"
    var str_correctAnswer = "1"
    var root:String = "0"

    var uid = "xyz"
    var roomID = "iii"


    var isClicked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_questions)


        uid = FirebaseAuth.getInstance().uid.toString()
        roomID = HomeActivity.ID

        databaseReference = FirebaseDatabase.getInstance().reference.child("Admin")
            .child(uid)
            .child(roomID)
            .child("Questions")

        window.statusBarColor = ContextCompat.getColor(this,R.color.app_page_color)


        question = findViewById(R.id.question)
        option_1 = findViewById(R.id.option1)
        option_2 = findViewById(R.id.option2)
        option_3 = findViewById(R.id.option3)
        option_4 = findViewById(R.id.option4)
        correctAnswer = findViewById(R.id.correctAnswer)

        var upload:AppCompatButton = findViewById(R.id.upload)

        if(intent.getStringExtra("root") != null) {
            root = intent.getStringExtra("root").toString()
            getData(root)
            intent.removeExtra("root")
        }


        upload.setOnClickListener {
            if(validation() && !isClicked){ loadData() }
        }
    }

    private fun getData(root:String){
        databaseReference.child(root).addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    question.setText(snapshot.child("question").value.toString())
                    option_1.setText(snapshot.child("option1").value.toString())
                    option_2.setText(snapshot.child("option2").value.toString())
                    option_3.setText(snapshot.child("option3").value.toString())
                    option_4.setText(snapshot.child("option4").value.toString())
                    correctAnswer.setText(snapshot.child("correctAnswer").value.toString())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun loadData(){
        isClicked = true
        databaseReference.addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val map: MutableMap<String, String> = mutableMapOf()
                map["correctAnswer"] = str_correctAnswer
                map["question"] = str_question
                map["option1"] = str_option_1
                map["option2"] = str_option_2
                map["option3"] = str_option_3
                map["option4"] = str_option_4

                ListQuestionsActivity.addedQuestionFlag = true
                if(root == "0") {
                    var i = databaseReference.push().key.toString()
                    databaseReference.child(i).setValue(map)
                    HomeActivity.addedFlag = true
                    incrementProvidedQuestions()
                    Toast.makeText(applicationContext, "Question Added", Toast.LENGTH_SHORT).show()
                    isClicked = false
                }else{
                    ListQuestionsActivity.editedQuestionFlag = true
                    ListQuestionsActivity.rootID = root
                    databaseReference.child(root).setValue(map)
                    Toast.makeText(applicationContext, "Updated", Toast.LENGTH_SHORT).show()
                    isClicked = false
                }
            }
            override fun onCancelled(error: DatabaseError) {
                isClicked = false
                Toast.makeText(applicationContext,"Problem in firebase",Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun incrementProvidedQuestions(){
        var dbref = FirebaseDatabase.getInstance().reference.child("Admin").child(uid).child(roomID)
        dbref.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var providedQuestion = snapshot.child("providedQuestions").value.toString().toInt() + 1

                dbref.child("providedQuestions").setValue(providedQuestion)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
    private fun validation():Boolean{
        str_question = question.text.toString()
        str_option_1 = option_1.text.toString()
        str_option_2 = option_2.text.toString()
        str_option_3 = option_3.text.toString()
        str_option_4 = option_4.text.toString()
        str_correctAnswer = correctAnswer.text.toString()


        if (str_question.isEmpty()) {
            question.error = "Please fill field"
            question.requestFocus()
            return false
        }

        if (str_option_1.isEmpty()) {
            option_1.error = "Please fill field"
            option_1.requestFocus()
            return false
        }

        if (str_option_2.isEmpty()) {
            option_2.error = "Please fill field"
            option_2.requestFocus()
            return false
        }

        if (str_option_3.isEmpty()) {
            option_3.error = "Please fill field"
            option_3.requestFocus()
            return false
        }

        if (str_option_4.isEmpty()) {
            option_4.error = "Please fill field"
            option_4.requestFocus()
            return false
        }

        if (str_correctAnswer.isEmpty()) {
            correctAnswer.error = "Please fill field"
            correctAnswer.requestFocus()
            return false
        }

        return true
    }
}