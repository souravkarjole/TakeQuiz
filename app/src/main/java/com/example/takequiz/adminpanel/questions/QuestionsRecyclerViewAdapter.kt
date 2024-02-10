package com.example.takequiz.adminpanel.questions

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.takequiz.Questions
import com.example.takequiz.QuizModel
import com.example.takequiz.R
import com.example.takequiz.adminpanel.HomeActivity
import com.example.takequiz.adminpanel.RecyclerViewAdapter
import com.example.takequiz.adminpanel.ShowDeletedOptions
import com.example.takequiz.adminpanel.questions.CreateQuestionsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class QuestionsRecyclerViewAdapter(
    private var context: Context,
    private var showDeletedOptions: ShowDeletedOptions,
    private var array: MutableList<Questions>): RecyclerView.Adapter<QuestionsRecyclerViewAdapter.ViewHolder>() {


    var newholder:ViewHolder? = null
    var pos:Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionsRecyclerViewAdapter.ViewHolder {
        val view:View = LayoutInflater.from(context).inflate(R.layout.item_questions,parent,false)

        return ViewHolder(view,showDeletedOptions)
    }



    override fun getItemCount(): Int {
        return array.size
    }

    override fun onBindViewHolder(holder: QuestionsRecyclerViewAdapter.ViewHolder, position: Int) {
        setFadingAnimation(holder.itemView)
        holder.question.text = array[position].questions


        holder.edit.setOnClickListener {
            var intent = Intent(context, CreateQuestionsActivity::class.java)

            ListQuestionsActivity.editedPosition = holder.adapterPosition
            intent.putExtra("root", array[holder.adapterPosition].root)

            intent.putExtra("question", array[holder.adapterPosition].questions)
            intent.putExtra("op1", array[holder.adapterPosition].op1)
            intent.putExtra("op2", array[holder.adapterPosition].op2)
            intent.putExtra("op3", array[holder.adapterPosition].op3)
            intent.putExtra("op4", array[holder.adapterPosition].op4)
            intent.putExtra("correctAnswer", array[holder.adapterPosition].correctAns)

            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            context.startActivity(intent)
        }


        holder.checkBox.setOnLongClickListener {
            holder.checkBox.isSelected = true
            newholder = holder
            pos = holder.adapterPosition
            showDeletedOptions.showDeletedBox(true)
            true
        }


    }

    fun unselectCheckBox(){
        newholder?.checkBox?.isSelected = false
    }

    fun deleteTheData(){
        var uid = FirebaseAuth.getInstance().uid.toString()

        var databaseReference:DatabaseReference = FirebaseDatabase.getInstance().reference
            .child("Admin")
            .child(uid)
            .child(HomeActivity.ID)
            .child("Questions")
            .child(array[pos].root)

        databaseReference.removeValue()
        array.removeAt(pos)
        notifyItemRemoved(pos)
    }

    fun setFadingAnimation(itemView: View) {
        itemView.alpha = 0f
        itemView.animate()
            .alpha(1f)
            .setDuration(700) // Adjust the duration as needed
            .start()
    }


    inner class ViewHolder(
        itemView: View,
        showDeletedOptions: ShowDeletedOptions
    ) :RecyclerView.ViewHolder(itemView) {


        var checkBox: ConstraintLayout
        var edit: CardView
        var question:TextView
        var showDeletedOptions:ShowDeletedOptions

        init {
            question = itemView.findViewById(R.id.question)
            edit = itemView.findViewById(R.id.edit)
            checkBox = itemView.findViewById(R.id.checkbox)

            this.showDeletedOptions = showDeletedOptions
        }
    }
}
