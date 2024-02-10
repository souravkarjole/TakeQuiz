package com.example.takequiz.adminpanel.questions

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.takequiz.Questions
import com.example.takequiz.R
import com.example.takequiz.adminpanel.HomeActivity
import com.example.takequiz.adminpanel.ShowDeletedOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ListQuestionsActivity : AppCompatActivity() ,ShowDeletedOptions{

    lateinit var recyclerView:RecyclerView
    lateinit var recyclerViewAdapter: QuestionsRecyclerViewAdapter
    lateinit var createQuestions: CardView
    lateinit var emptyScreenDisplay: LinearLayout

    lateinit var array:MutableList<Questions>
    lateinit var databaseReference:DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_questions)

        window.statusBarColor = ContextCompat.getColor(this,R.color.app_dark_color)

        array = arrayListOf()
        emptyScreenDisplay = findViewById(R.id.emptyScreenDisplay)
        recyclerView = findViewById(R.id.recyclerView_)

        setTheRecyclerView()

        createQuestions = findViewById(R.id.createQuestion)

        createQuestions.setOnClickListener {
            startActivity(Intent(this,CreateQuestionsActivity::class.java))
        }

        val uid = FirebaseAuth.getInstance().uid.toString()
        val roomId:String = HomeActivity.ID

        databaseReference = FirebaseDatabase.getInstance().reference.child("Admin").child(uid).child(roomId).child("Questions")
        displayQuestion()
    }

    private fun setTheRecyclerView(){
        recyclerView.layoutManager = LinearLayoutManager(this)

        recyclerViewAdapter = QuestionsRecyclerViewAdapter(
            applicationContext,
            this,
            array
        )

        recyclerView.adapter = recyclerViewAdapter
    }

    override fun onResume() {
        super.onResume()

        if(addedQuestionFlag) {
            addedQuestionFlag = false
            displayQuestion()
        }

        if(editedQuestionFlag){
            editedQuestionFlag = false
            databaseReference.addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()) {
                        array[editedPosition].questions = snapshot.child(rootID).child("question").value.toString()
                        array[editedPosition].op1 = snapshot.child(rootID).child("option1").value.toString()
                        array[editedPosition].op2 = snapshot.child(rootID).child("option4").value.toString()
                        array[editedPosition].op3 = snapshot.child(rootID).child("option3").value.toString()
                        array[editedPosition].op4 = snapshot.child(rootID).child("option4").value.toString()
                        array[editedPosition].correctAns = snapshot.child(rootID).child("correctAnswer").value.toString()

                        rootID = "o"
                        recyclerViewAdapter.notifyItemChanged(editedPosition)
                    }else{
                        Toast.makeText(applicationContext,"Invalid ID",Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}

            })
        }
    }

    private fun displayQuestion(){
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    emptyScreenDisplay.visibility = View.GONE

                    var i = 0
                    var flag:Boolean
                    for (childerns in snapshot.children) {
                        if (array.isEmpty() or (i == array.size)) {
                            flag = true
                        } else {
                            flag = array[i].root != childerns.key
                        }
                        if (flag) {
                            var root = childerns.key.toString()
                            var question = childerns.child("question").value.toString()
                            var correctAnswer = childerns.child("correctAnswer").value.toString()
                            var op1 = childerns.child("option1").value.toString()
                            var op2 = childerns.child("option2").value.toString()
                            var op3 = childerns.child("option3").value.toString()
                            var op4 = childerns.child("option4").value.toString()

                            array.add(Questions(root, question, correctAnswer, op1, op2, op3, op4))
                            recyclerViewAdapter.notifyItemInserted(i)
                        }
                        i++
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun blankScreenAnimation(p0:Float,p1:Float){
        emptyScreenDisplay.alpha = 0f
        val blankScreenLayoutAnimation = ObjectAnimator.ofFloat(emptyScreenDisplay,"alpha",p0,p1)
        blankScreenLayoutAnimation.duration = 1000L
        blankScreenLayoutAnimation.start()
        emptyScreenDisplay.visibility = if(emptyScreenDisplay.isVisible) View.GONE else View.VISIBLE
    }

    override fun showDeletedMenu(show: Boolean) {
        TODO("Not yet implemented")
    }

    override fun showDeletedBox(show: Boolean) {
        val alertDialogBuild: AlertDialog.Builder = AlertDialog.Builder(this)
        val view:View = layoutInflater.inflate(R.layout.alert_dialog,null)

        alertDialogBuild.setView(view)
        val alertDialog = alertDialogBuild.create()

        alertDialog.show()
        alertDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val cancel: TextView = view.findViewById(R.id.cancel)
        val delete: TextView = view.findViewById(R.id.delete)

        delete.setOnClickListener {
            HomeActivity.deletedFlag = true
            HomeActivity.totalDeletedItems++
            recyclerViewAdapter.deleteTheData()

            if(array.isEmpty()) blankScreenAnimation(0f,1f)

            alertDialog.dismiss()
        }
        cancel.setOnClickListener {
            alertDialog.dismiss()
            recyclerViewAdapter.unselectCheckBox()
        }

        alertDialog.setOnDismissListener {
            recyclerViewAdapter.unselectCheckBox()
        }
    }

    companion object{
        var addedQuestionFlag = false
        var editedQuestionFlag = false
        var editedPosition = -1
        var rootID = "abc"
    }
}