package com.example.qlique.Map

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Parcelable
import android.text.method.ScrollingMovementMethod
import android.util.DisplayMetrics
import android.view.KeyEvent
import android.view.View
import android.view.View.GONE
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.qlique.CreateEvent.Event
import com.example.qlique.CreateEvent.EventMembers
import com.example.qlique.NewMessageActivity
import com.example.qlique.Profile.User
import com.example.qlique.R
import com.example.qlique.ChatLogActivity
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.join_event_dialog.view.*

/**
 * EventsMap
 * displays events in the map, ShowEventMap and DisplayEventsMapActivity implements this class.
 */
open class EventsMap : BasicMapActivity() {
    var isJoinDialogOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPlaceSearch = findViewById(R.id.input_search)
        mPlaceSearch.setOnEditorActionListener { _, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE
                || keyEvent.action == KeyEvent.ACTION_DOWN
                || keyEvent.action == KeyEvent.KEYCODE_ENTER
            ) {
                // execute our method for searching
                geoLocate(false)
            }
            false
        }
    }


    /**
     * updates the event's description.
     */
    protected fun updateViewOfBottomDialog(view: View, event: Event){
        val map = view.findViewById<View>(R.id.map_image_view) as ImageView
        map.visibility = GONE
        val textView =view.findViewById<View>(R.id.description_post) as TextView
        val title =view.findViewById<View>(R.id.headerPost) as TextView
        textView.text = event.description
        title.text = event.header
        textView.movementMethod = ScrollingMovementMethod()
        title.movementMethod = ScrollingMovementMethod()
        val date =view.findViewById<View>(R.id.date) as TextView
        val hour =view.findViewById<View>(R.id.hour) as TextView
        date.text = event.date
        hour.text = event.hour
        val view1: ImageView = view.findViewById(R.id.post_image_home)
        if(event.photoUrl!=null){
            Picasso.get().load(event.photoUrl).into(view1)
        }
        val members = view.findViewById<View>(R.id.members_info_bottom)
        members.setOnClickListener {
            val i = Intent(view.context, EventMembers::class.java)
            i.putExtra("eventobj",  event as Parcelable)
            view.context.startActivity(i)
        }
        val joinBtn = view.findViewById<ImageView>(R.id.post_image_like_btn)
        joinBtn.setOnClickListener {
            openJoinDialog(view.context,event.eventUid)
        }
        updateAuthor(view, event.uid)
    }

    /**
     * joins to the event.
     */
    private fun joinCurUserToEvent(eventUid :String){
        val curUidUser = FirebaseAuth.getInstance().currentUser?.uid ?: return
        if(eventUid.isEmpty()){
            return
        }
        addMemberToEvent(eventUid, curUidUser)
    }

    /**
     * saves the event in the firebase.
     */
    fun saveEvent(event: Event){
        val refPost = FirebaseDatabase.getInstance().getReference("/posts/${event.eventUid}")
        refPost.setValue(event)
    }

    /**
     * adds the event to the user's list of events in tje firebase.
     */
    private fun addEventToCurUser(eventUid: String){
        val refPost = FirebaseDatabase.getInstance().getReference("/users/${FirebaseAuth.getInstance().currentUser?.uid}")
        refPost.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user: User = (snapshot.getValue(User::class.java))
                    ?: return
                if (user.events == null) {
                    user.events = java.util.ArrayList()
                }
                user.events.add(eventUid)
                refPost.setValue(user)
            }

            override fun onCancelled(po: DatabaseError) {
            }
        })

    }

    /**
     * adds the user to the event.
     */
    private fun addMemberToEvent(eventUid: String, curUidUser: String) {
        val refPost = FirebaseDatabase.getInstance().getReference("/posts/$eventUid")
        refPost.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val event: Event? = snapshot.getValue(Event::class.java) ?: return
                event?.setEventUid(eventUid)
                event?.description = snapshot.child("description").value.toString()
                event?.uid = snapshot.child("uid").value.toString()
                if (event?.members == null) {
                    event?.members = java.util.ArrayList()
                }
                if (event?.membersCapacity!! > event.members?.size!!) {
                    if (event.members.contains(curUidUser)) {
                        Toast.makeText(
                           applicationContext,
                            "You are already registered for this event",
                            Toast.LENGTH_LONG
                        ).show()
                        return
                    } else {
                        event.members.add(curUidUser)
                        saveEvent(event)
                        addEventToCurUser(eventUid)
                        return
                    }
                } else {
                    // It is not possible to register because there is no space available at the event.
                    Toast.makeText(applicationContext, "Failed to register", Toast.LENGTH_LONG)
                        .show()
                }
            }

            override fun onCancelled(po: DatabaseError) {
            }
        })

    }

    /**
     * opens the join dialog after the user selects the join button to make sure he wants
     * to sign in.
     */
    private fun openJoinDialog(applicationContext: Context, eventUid: String) {
        isJoinDialogOpen = true
        val view = View.inflate(applicationContext, R.layout.join_event_dialog, null)
        val builder = androidx.appcompat.app.AlertDialog.Builder(applicationContext)
        builder.setView(view)
        val dialog: Dialog = builder.create()
        val width = (DisplayMetrics().widthPixels)
        val height = (DisplayMetrics().heightPixels * 0.4).toInt()
        dialog.window?.setLayout(width, height)
        dialog.show()
        view.join_btn.setOnClickListener {
            isJoinDialogOpen = true
            joinCurUserToEvent(eventUid)
            dialog.cancel()
            setTimer()
        }
        view.cancle_join_btn.setOnClickListener {
            isJoinDialogOpen = true
            dialog.cancel()
            setTimer()
        }
        isJoinDialogOpen = false
    }

    /**
     * sets a timer of 3 econds.
     */
    private fun setTimer(){
        object : CountDownTimer(3000, 1000) {

            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                isJoinDialogOpen = false
            }
        }.start()
    }

    /**
     * update the author of the post so the user will be able to send him a message in the chat.
     */
    private fun updateAuthor(view:View, uid: String){
        FirebaseDatabase.getInstance().getReference("/users/$uid")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val user = (dataSnapshot).getValue(User::class.java)
                    if (user!!.url != null) {
                        val authorImage = view.findViewById<ImageView>(R.id.photo_event_new)
                        Picasso.get().load(user.url).into(authorImage)
                    }
                    val userName = view.findViewById<TextView>(R.id.user_name_post)
                    userName.text =
                        dataSnapshot.child("firstName").value.toString() + " " + dataSnapshot.child(
                            "lastName"
                        ).value.toString()
                    val chat = view.findViewById<View>(R.id.post_image_chat_btn) as ImageView
                    if (user.uid == FirebaseAuth.getInstance().uid){
                        chat.visibility = GONE
                        return
                    }
                    chat.setOnClickListener {
                        val intent = Intent(view.context, ChatLogActivity::class.java)
                        intent.putExtra(NewMessageActivity.USER_KEY, user)
                        view.context.startActivity(intent)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    /**
     * gets the bitmap descriptor.
     */
    fun getBitmapDescriptorFromVector(context: Context, vectorDrawableResourceId: Int): BitmapDescriptor? {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId)
        val bitmap = Bitmap.createBitmap(
            vectorDrawable!!.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    /**
     * returns an image of the hobby entered in order to display this image in
     * the map for this event.
     */
    fun getImageByHobby(hobby: String): Int {
        when (hobby) {
            "Ball Games" -> {
                return R.drawable.ic_baseline_sports_soccer_24
            }
            "Sport" -> {
                return  R.drawable.ic_sport
            }
            "Initiative" -> {
                return R.drawable.ic_light_bulb
            }
            "Business" -> {
                return R.drawable.ic_buisnessicon
            }
            "Fashion" -> {
                return R.drawable.ic_fashion
            }
            "Social" -> {
                return R.drawable.ic_friends
            }
            "Entertainment" -> {
                return R.drawable.ic_movies
            }
            "Study" -> {
                return R.drawable.ic_studying
            }
            "Beauty and style" -> {
                return R.drawable.ic_eye_treatment
            }
            "Comedy" -> {
                return R.drawable.ic_lol
            }
            "Food" -> {
                return R.drawable.ic_spaguetti
            }
            "Animals" -> {
                return R.drawable.ic_pets
            }
            "Talent" -> {
                return R.drawable.ic_talent
            }
            "Cars" -> {
                return R.drawable.ic_cars1
            }
            "Love and dating" -> {
                return R.drawable.ic_hearts
            }
            "Fitness and health" -> {
                return R.drawable.ic_meditation
            }
            "Dance" -> {
                return R.drawable.ic_dancing
            }
            "Outdoor activities" -> {
                return R.drawable.ic_sport
            }
            "Home and garden" -> {
                return R.drawable.ic_plant_pot
            }
            "Gaming" -> {
                return R.drawable.ic_joystick
            }
            else -> {
                return R.drawable.ic_location
            }
        }
    }

}