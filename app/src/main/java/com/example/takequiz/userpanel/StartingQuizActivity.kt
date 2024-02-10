package com.example.takequiz.userpanel

import android.animation.ObjectAnimator
import android.app.ActionBar.LayoutParams
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.example.takequiz.MainActivity
import com.example.takequiz.Questions
import com.example.takequiz.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class StartingQuizActivity : AppCompatActivity() {

    private lateinit var countDown:TextView
    private lateinit var totalQuestions:TextView
    private lateinit var cardView:CardView
    private lateinit var questions: TextView
    private lateinit var topic_: TextView
    private lateinit var option1Btn: AppCompatButton
    private lateinit var option2Btn: AppCompatButton
    private lateinit var option3Btn: AppCompatButton
    private lateinit var option4Btn: AppCompatButton
    private lateinit var nextBtn:AppCompatButton

    lateinit var databaseReference:DatabaseReference

    var str_topic:String = "topic"
    var str_totalQuestions = "13"
    var str_duration:String = "12"

    lateinit var dialog: Dialog

    var array:MutableList<Questions> = mutableListOf()
    var questionNo:Int = 0

    var countDownTimer:CountDownTimer? = null


    var wrongAnswers = 0
    var correctAnswers = 0
    var notAttempted = 0

    lateinit var optionButtons:Array<AppCompatButton>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_starting_quiz)

        window.statusBarColor = ContextCompat.getColor(this,R.color.app_dark_color)

        val key:String = intent.getStringExtra("key").toString()
        val ID:String = intent.getStringExtra("ID").toString()


        databaseReference = FirebaseDatabase.getInstance().reference.child("Admin").child(key).child(ID)

        countDown = findViewById(R.id.timer)
        totalQuestions = findViewById(R.id.totalQuestions)
        nextBtn = findViewById(R.id.next)
        cardView = findViewById(R.id.card)
        questions = findViewById(R.id.questions)
        topic_ = findViewById(R.id.topic)
        option1Btn = findViewById(R.id.option1)
        option2Btn = findViewById(R.id.option2)
        option3Btn = findViewById(R.id.option3)
        option4Btn = findViewById(R.id.option4)


        showDialog()

        nextBtn.setOnClickListener {
            countDownTimer?.cancel()
            resetOptions()
            showQuestions()
        }

        optionButtons = arrayOf(option1Btn, option2Btn, option3Btn, option4Btn)

        for (button in optionButtons) {
            button.setOnClickListener {
                if(!nextBtn.isEnabled) {
                    countDownTimer?.cancel()
                    nextBtn.isEnabled = true

                    val selectedOption = button.text.toString()
                    val correctAnswer = array[questionNo].correctAns

                    // Set background resource based on the correctness
                    if(selectedOption == correctAnswer){
                        button.setBackgroundResource(R.drawable.correct_answer)
                        correctAnswers++
                    }else{
                        button.setBackgroundResource(R.drawable.wrong_answer)
                        wrongAnswers++
                    }

                    // Set background resource for the correct answer
                    optionButtons.firstOrNull { it.text == correctAnswer }
                        ?.setBackgroundResource(R.drawable.correct_answer)

                    questionNo++
                }
            }
        }

    }


    private fun resetOptions(){
        for(i in optionButtons.indices){
            optionButtons[i].setBackgroundResource(R.drawable.transparent_button)
        }
    }

    private fun showQuestions(){
        if(questionNo < str_totalQuestions.toInt()){
            nextBtn.isEnabled = false
            countDownTimer()
            questions.text = array[questionNo].questions
            option1Btn.text = array[questionNo].op1
            option2Btn.text = array[questionNo].op2
            option3Btn.text = array[questionNo].op3
            option4Btn.text = array[questionNo].op4
            totalQuestions.text = "${questionNo+1}/${str_totalQuestions}"

            playAnimation()

        }else{
            var intent = Intent(this,ResultActivity::class.java)

            intent.putExtra("C",correctAnswers)
            intent.putExtra("W",wrongAnswers)
            intent.putExtra("NA",str_totalQuestions.toInt()-(correctAnswers+wrongAnswers))
            intent.putExtra("TQ",str_totalQuestions.toInt())
            startActivity(intent)
            finish()
        }
    }

    private fun playAnimation(){
        cardView.alpha = 0f

        questions.scaleX = 0f
        questions.scaleY = 0f
        option1Btn.scaleX = 0f
        option1Btn.scaleY = 0f
        option2Btn.scaleX = 0f
        option2Btn.scaleY = 0f
        option3Btn.scaleX = 0f
        option3Btn.scaleY = 0f
        option4Btn.scaleX = 0f
        option4Btn.scaleY = 0f

        var cardViewAnimator = ObjectAnimator.ofFloat(cardView,"alpha",0f,1f)
        var questionsAnimatorX = ObjectAnimator.ofFloat(questions,"scaleX",0f,1f)
        var questionsAnimatorY = ObjectAnimator.ofFloat(questions,"scaleY",0f,1f)
        var option1BtnAnimatorX = ObjectAnimator.ofFloat(option1Btn,"scaleX",0f,1f)
        var option1BtnAnimatorY = ObjectAnimator.ofFloat(option1Btn,"scaleY",0f,1f)
        var option2BtnAnimatorX = ObjectAnimator.ofFloat(option2Btn,"scaleX",0f,1f)
        var option2BtnAnimatorY = ObjectAnimator.ofFloat(option2Btn,"scaleY",0f,1f)
        var option3BtnAnimatorX = ObjectAnimator.ofFloat(option3Btn,"scaleX",0f,1f)
        var option3BtnAnimatorY = ObjectAnimator.ofFloat(option3Btn,"scaleY",0f,1f)
        var option4BtnAnimatorX = ObjectAnimator.ofFloat(option4Btn,"scaleX",0f,1f)
        var option4BtnAnimatorY = ObjectAnimator.ofFloat(option4Btn,"scaleY",0f,1f)

        val duration = 950L

        cardViewAnimator.startDelay = 0
        option1BtnAnimatorX.startDelay = 2*duration/4
        option1BtnAnimatorY.startDelay = 2*duration/4
        option2BtnAnimatorX.startDelay = 3*duration/5
        option2BtnAnimatorY.startDelay = 3*duration/5
        option3BtnAnimatorX.startDelay = 4*duration/6
        option3BtnAnimatorY.startDelay = 4*duration/6
        option4BtnAnimatorX.startDelay = 5*duration/7
        option4BtnAnimatorY.startDelay = 5*duration/7

        cardViewAnimator.duration = duration
        questionsAnimatorX.duration = duration-300
        questionsAnimatorY.duration = duration-300
        option1BtnAnimatorX.duration = duration
        option1BtnAnimatorY.duration = duration
        option2BtnAnimatorX.duration = duration
        option2BtnAnimatorY.duration = duration
        option3BtnAnimatorX.duration = duration
        option3BtnAnimatorY.duration = duration
        option4BtnAnimatorX.duration = duration
        option4BtnAnimatorY.duration = duration


        cardViewAnimator.start()
        questionsAnimatorX.start()
        questionsAnimatorY.start()
        option1BtnAnimatorX.start()
        option1BtnAnimatorY.start()
        option2BtnAnimatorX.start()
        option2BtnAnimatorY.start()
        option3BtnAnimatorX.start()
        option3BtnAnimatorY.start()
        option4BtnAnimatorX.start()
        option4BtnAnimatorY.start()
    }


    private fun countDownTimer(){
        countDownTimer = object :CountDownTimer(str_duration.toLong()*1000,1000){
            override fun onTick(milliSecondRemaining: Long) {
                countDown.text = (milliSecondRemaining/1000).toString()
            }

            override fun onFinish() {
                questionNo++
                resetOptions()
                showQuestions()
            }

        }.start()
    }

    private fun showDialog(){
        dialog = Dialog(this)
        val view:View = dialog.layoutInflater.inflate(R.layout.user_alert_dialog_box,null)

        dialog.setContentView(view)

        val topic:TextView = dialog.findViewById(R.id.topic)
        val totalQuestions:TextView = dialog.findViewById(R.id.totalQuestions)
        val duration:TextView = dialog.findViewById(R.id.duration)
        val ready:AppCompatButton = dialog.findViewById(R.id.ready)
        val cancel:AppCompatButton = dialog.findViewById(R.id.cancel)
        var flag = true


        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    str_topic = snapshot.child("topic").value.toString()
                    str_totalQuestions = snapshot.child("providedQuestions").value.toString()
                    str_duration = snapshot.child("duration").value.toString()

                    topic.text = str_topic
                    totalQuestions.text = str_totalQuestions
                    duration.text = str_duration

                    if(snapshot.child("Questions").exists()) {

                        for (children in snapshot.child("Questions").children) {
                            val correctAns = children.child("correctAnswer").value.toString()
                            val question = children.child("question").value.toString()
                            val option1 = children.child("option1").value.toString()
                            val option2 = children.child("option2").value.toString()
                            val option3 = children.child("option3").value.toString()
                            val option4 = children.child("option4").value.toString()

                            array.add(
                                Questions(
                                    "",
                                    question,
                                    correctAns,
                                    option1,
                                    option2,
                                    option3,
                                    option4
                                )
                            )
                        }
                    }else{
                        flag = false

                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        cancel.setOnClickListener {
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }

        ready.setOnClickListener {
            if(flag) {
                val relativeLayout: RelativeLayout = findViewById(R.id.relative)
                relativeLayout.alpha = 1f
                dialog.dismiss()

                if (array.isNotEmpty()) {
                    topic_.text = str_topic
                    showQuestions()
                }
            }else{
                Toast.makeText(applicationContext,"No Questions found",Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
        dialog.setCancelable(false)
        dialog.window?.setLayout(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

}