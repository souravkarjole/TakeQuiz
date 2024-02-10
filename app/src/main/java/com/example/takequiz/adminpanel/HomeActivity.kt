package com.example.takequiz.adminpanel

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.takequiz.MainActivity
import com.example.takequiz.QuizModel
import com.example.takequiz.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeActivity : AppCompatActivity(), ShowDeletedOptions,CopyToClipBoard {

    lateinit var recyclerView: RecyclerView
    lateinit var array: MutableList<QuizModel>
    lateinit var recyclerViewAdapter: RecyclerViewAdapter

    lateinit var toggle: ActionBarDrawerToggle
    lateinit var animation: LottieAnimationView
    lateinit var blankScreenLayout: LinearLayout
    lateinit var fab:FloatingActionButton


    lateinit var roomId:EditText
    lateinit var topic:EditText
    lateinit var duration:EditText
    lateinit var createRoom:AppCompatButton

    var str_roomID = "123"
    var str_topic = "topic"
    var str_provided_Ques = "0"
    var str_duration = "20"

    lateinit var mainMenu: Menu

    private lateinit var databaseReference:DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        window.statusBarColor = ContextCompat.getColor(this,R.color.app_dark_color)
        val uid:String = FirebaseAuth.getInstance().currentUser?.uid.toString()

        databaseReference = FirebaseDatabase.getInstance().reference.child("Admin").child(uid)

        val navigationView:NavigationView = findViewById(R.id.nav_view)
        fab = findViewById(R.id.fab)

        val appHomePageLayout = findViewById<CoordinatorLayout>(R.id.app_home_page)
        animation = appHomePageLayout.findViewById(R.id.loading_anim)

        blankScreenLayout = appHomePageLayout.findViewById(R.id.blankScreen_layout)

        val toolbar:Toolbar = appHomePageLayout.findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)

        val drawerLayout:DrawerLayout = findViewById(R.id.drawerLayout)

        toggle = ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open,R.string.close)
        toggle.drawerArrowDrawable.color = Color.WHITE

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener(object :NavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                when(item.itemId){
                    R.id.logout -> {
                        window.setFlags(
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        )
                        drawerLayout.closeDrawer(GravityCompat.START)
                        animation.visibility = View.VISIBLE

                        Handler(Looper.getMainLooper()).postDelayed({
                            Toast.makeText(applicationContext,"Logged Out",Toast.LENGTH_SHORT).show()
                            animation.visibility = View.GONE
                            Firebase.auth.signOut()
                            startActivity(Intent(applicationContext,MainActivity::class.java))
                            finish()
                        }, 1500)
                    }
                }
                return true
            }
        })

        fab.setOnClickListener {
            showDialog()
        }

        recyclerView = appHomePageLayout.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        array = mutableListOf()

        databaseReference.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(children in snapshot.children){
                        str_topic = children.child("topic").value.toString()
                        str_duration = children.child("duration").value.toString()
                        str_provided_Ques = children.child("providedQuestions").value.toString()

                        array.add(QuizModel(array.size+1,children.key.toString(),str_topic,str_provided_Ques,str_duration))
                    }

                    recyclerViewAdapter = RecyclerViewAdapter(
                        applicationContext,
                        this@HomeActivity,
                        this@HomeActivity,
                        array
                    )
                    recyclerView.adapter = recyclerViewAdapter
                    animation.visibility = View.GONE
                }else{
                    animation.visibility = View.GONE

                    blankScreenAnimation(0f,1f)
                    recyclerViewAdapter = RecyclerViewAdapter(
                        applicationContext,
                        this@HomeActivity,
                        this@HomeActivity,
                        array
                    )
                    recyclerView.adapter = recyclerViewAdapter
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        mainMenu = menu
        menuInflater.inflate(R.menu.menu_, mainMenu)
        return true
    }


    fun blankScreenAnimation(p0:Float,p1:Float){
        blankScreenLayout.alpha = 0f
        val blankScreenLayoutAnimation = ObjectAnimator.ofFloat(blankScreenLayout,"alpha",p0,p1)
        blankScreenLayoutAnimation.duration = 1000L
        blankScreenLayoutAnimation.start()
        blankScreenLayout.visibility = if(blankScreenLayout.isVisible) View.GONE else View.VISIBLE
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.just_delete -> {
                // Handle delete action
                showDeletedBox()
                return true
            }
            R.id.cancel ->{
                recyclerViewAdapter.setDefault()
                showDeletedMenu(false)
                return true
            }
            // Handle other menu items if needed

            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun showDialog(){
        val dialog:Dialog = Dialog(this)
        val view:View = layoutInflater.inflate(R.layout.create_room,null)

        dialog.setContentView(view)

        roomId = dialog.findViewById(R.id.roomId)
        topic = dialog.findViewById(R.id.topic)
        duration = dialog.findViewById(R.id.duration)
        createRoom = dialog.findViewById(R.id.createRoom)
        val reload:ImageView = dialog.findViewById(R.id.reload)

        reload.setOnClickListener {
            roomId.setText(databaseReference.push().key.toString().substring(1))
        }


        createRoom.setOnClickListener {
            if(validation()){
                databaseReference.child(str_roomID).addListenerForSingleValueEvent(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists()){
                            roomId.error = "Unique Room id is required"
                            roomId.requestFocus()
                        }else{
                            if(blankScreenLayout.isVisible) {
                                blankScreenAnimation(1f, 0f)
                            }
                            val map:MutableMap<String,String> = mutableMapOf()
                            map["topic"] = str_topic
                            map["duration"] = str_duration
                            map["providedQuestions"] = "0"

                            databaseReference.child(str_roomID).setValue(map)

                            array.add(QuizModel(array.size+1,str_roomID,str_topic,"0",str_duration))
                            recyclerViewAdapter.notifyItemInserted(array.size-1)
                        }
                        dialog.cancel()
                    }
                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
            }
        }

        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onResume() {
        super.onResume()

        if(deletedFlag){
            deletedFlag = false
            databaseReference.child(ID).addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    var i = snapshot.child("providedQuestions").value.toString().toInt()
                    i -= totalDeletedItems

                    databaseReference.child(ID).child("providedQuestions").setValue(i)
                    array[position].providedQuestions = i.toString()
                    recyclerViewAdapter.notifyItemChanged(position)
                    totalDeletedItems = 0
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }
        else if (addedFlag){
            addedFlag = false

            databaseReference.child(ID).addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    array[position].providedQuestions = snapshot.child("providedQuestions").value.toString()
                    recyclerViewAdapter.notifyItemChanged(position)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

        }
    }

    private fun validation():Boolean{
        str_roomID = roomId.text.toString()
        str_topic = topic.text.toString()
        str_duration = duration.text.toString()


        if (str_roomID.isEmpty()) {
            roomId.error = "Please fill field"
            roomId.requestFocus()
            return false
        }

        if (str_topic.isEmpty()) {
            topic.error = "Please fill field"
            topic.requestFocus()
            return false
        }

        if (str_duration.isEmpty()) {
            duration.error = "Please fill field"
            duration.requestFocus()
            return false
        }

        return true
    }

    override fun showDeletedMenu(show: Boolean) {
        mainMenu.findItem(R.id.just_delete).isVisible = show
        mainMenu.findItem(R.id.cancel).isVisible = show
    }

    override fun showDeletedBox(show: Boolean) {
        val alertDialogBuild: AlertDialog.Builder = AlertDialog.Builder(this)
        val view:View = layoutInflater.inflate(R.layout.alert_dialog,null)

        alertDialogBuild.setView(view)
        val alertDialog = alertDialogBuild.create()

        alertDialog.show()
        alertDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val cancel:TextView = view.findViewById(R.id.cancel)
        val delete:TextView = view.findViewById(R.id.delete)

        delete.setOnClickListener {
            recyclerViewAdapter.onDeleteTasks()
            if(array.isEmpty()) blankScreenAnimation(0f,1f)

            alertDialog.dismiss()
            showDeletedMenu(false)
        }
        cancel.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    override fun copyToClipBoard(text: String) {
        // Get the clipboard manager
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        // Create a ClipData object to store the text
        val clipData = ClipData.newPlainText("Copied Text", text)

        // Set the data to the clipboard
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(this,"copied",Toast.LENGTH_SHORT).show()
    }

    companion object {
        var position:Int = 0
        var addedFlag = false
        var ID = "123"
        var providedQuestions = 0
        var deletedFlag = false
        var totalDeletedItems = 0
    }
}