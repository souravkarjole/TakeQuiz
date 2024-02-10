package com.example.takequiz.adminpanel

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.takequiz.QuizModel
import com.example.takequiz.R
import com.example.takequiz.adminpanel.questions.CreateQuestionsActivity
import com.example.takequiz.adminpanel.questions.ListQuestionsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RecyclerViewAdapter(
    private var context: Context,
    private var showDeletedMenu: ShowDeletedOptions,
    private var copyToClipBoard: CopyToClipBoard,
    private var array: MutableList<QuizModel>): RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

    private var isEnabled:Boolean = false
    private var selectDeletedItems:MutableList<Int> = arrayListOf()
    private var selectedCheckBoxes:MutableList<ViewHolder> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view:View = LayoutInflater.from(context).inflate(R.layout.item_rooms,parent,false)

        return ViewHolder(view,showDeletedMenu,copyToClipBoard)
    }

    override fun getItemCount(): Int {
        return array.size
    }

    fun setDefault(){
        selectedCheckBoxes.forEach { it ->
            it.checkBox.isSelected = false
        }
        if(selectDeletedItems.isNotEmpty() || selectedCheckBoxes.isNotEmpty()) {
            selectDeletedItems.clear()
            selectedCheckBoxes.clear()
            isEnabled = false
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        setFadingAnimation(holder.itemView)
        holder.roomID.text = array[position].roomID
        holder.topic.text = array[position].topic
        holder.providedQuestions.text = array[position].providedQuestions
        holder.duration.text = array[position].duration
        holder.roomNo.text = array[position].roomNo.toString()


        holder.copy.setOnClickListener {
            val text = "Key \n${FirebaseAuth.getInstance().uid.toString()}\n\nid \n${holder.roomID.text}"

            holder.copyToClipBoard.copyToClipBoard(text)
        }

        holder.checkBox.setOnLongClickListener {
            if(!isEnabled) {
                isEnabled = true
                holder.showDeletedMenu.showDeletedMenu(true)
                holder.checkBox.isSelected = true
                selectDeletedItems.add(position)
                selectedCheckBoxes.add(holder)
            }
            true
        }


        holder.checkBox.setOnClickListener {
            if(isEnabled) {
                if (selectDeletedItems.contains(position)) {
                    holder.checkBox.isSelected = false
                    selectDeletedItems.remove(position)
                    selectedCheckBoxes.remove(holder)
                    if (selectDeletedItems.isEmpty()) {
                        isEnabled = false
                        holder.showDeletedMenu.showDeletedMenu(false)
                    }
                } else {
                    isEnabled = true
                    holder.checkBox.isSelected = true
                    selectDeletedItems.add(position)
                    selectedCheckBoxes.add(holder)
                }
            }else{
                var intent: Intent = Intent(context, ListQuestionsActivity::class.java)
                HomeActivity.ID = array[position].roomID
                HomeActivity.providedQuestions = array[position].providedQuestions.toInt()
                HomeActivity.position = holder.adapterPosition

                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

                context.startActivity(intent)
            }
        }
    }

    fun onDeleteTasks(){
        val uid:String = FirebaseAuth.getInstance().uid.toString()
        val dataBaseRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Admin").child(uid)

        val itemsToDelete = ArrayList<QuizModel>()

        for(i in 0..< selectDeletedItems.size){
            val roomId = array[selectDeletedItems[i]].roomID

            dataBaseRef.child(roomId).removeValue()
            itemsToDelete.add(array[selectDeletedItems[i]])
        }

        for (item in itemsToDelete) {
            array.remove(item)
        }


        // Notify the adapter
        notifyDataSetChanged()
        setDefault()
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
        showDeletedMenu: ShowDeletedOptions,
        copyToClipBoard: CopyToClipBoard
    ) :RecyclerView.ViewHolder(itemView) {
        var roomID:TextView
        var roomNo:TextView
        var topic:TextView
        var providedQuestions:TextView
        var duration:TextView
        var checkBox: ConstraintLayout
        var showDeletedMenu: ShowDeletedOptions
        var copyToClipBoard: CopyToClipBoard
        var copy:ImageView

        init {
            roomID = itemView.findViewById(R.id.roomId)
            roomNo = itemView.findViewById(R.id.roomNo)
            topic = itemView.findViewById(R.id.topic)
            providedQuestions = itemView.findViewById(R.id.providedQuestions)
            duration = itemView.findViewById(R.id.duration)
            copy = itemView.findViewById(R.id.copy)

            checkBox = itemView.findViewById(R.id.checkbox)

            this.showDeletedMenu = showDeletedMenu
            this.copyToClipBoard = copyToClipBoard
        }
    }
}
