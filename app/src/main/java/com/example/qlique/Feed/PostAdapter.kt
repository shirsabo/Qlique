package com.example.qlique.Feed

import android.app.AlertDialog
import android.content.Intent
import android.os.Parcelable
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.qlique.CreateEvent.Event
import com.example.qlique.CreateEvent.EventMembers
import com.example.qlique.Map.ShowEventMap
import com.example.qlique.NewMessageActivity
import com.example.qlique.Profile.ProfilePage
import com.example.qlique.Profile.User
import com.example.qlique.R
import com.example.qlique.chatLogActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.join_event_dialog.view.*
import kotlinx.android.synthetic.main.post_in_feed.view.*
import java.io.Serializable


class PostAdapter(val events: ArrayList<Event>) :RecyclerView.Adapter<PostAdapter.ViewHolder>(){
    companion object{
        fun  addEventToUser(eventUid: String){
            val refPost = FirebaseDatabase.getInstance().getReference("/users/${FirebaseAuth.getInstance().currentUser?.uid}")
            refPost.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user: User = (snapshot.getValue(User::class.java))
                        ?: return//FirebaseAuth.getInstance().currentUser//snapshot.getValue(User::class.java) ?: return
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
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view:View=LayoutInflater.from(parent.context).inflate(
            R.layout.post_in_feed,
            parent,
            false
        )
        return ViewHolder(view)

    }

    override fun getItemCount(): Int {
        return events.size

    }
     fun saveEvent(event: Event){
        val refPost = FirebaseDatabase.getInstance().getReference("/posts/${event.eventUid}")
        refPost.setValue(event)
    }
    private fun saveUser(user: User){
        val refUser = FirebaseDatabase.getInstance().getReference("/users/${user.uid}")
        refUser.setValue(user)
    }
    private fun addMe(ref: DatabaseReference, root: String, member: String){
        val map: MutableMap<String, String> = HashMap()
        map[member]= ""
        ref.child(root).updateChildren(map as Map<String, String>)

    }

    fun addMemberToEvent(eventUid: String, memberUID: String, holder: ViewHolder){
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
                    if (event.members.contains(memberUID)) {
                        Toast.makeText(
                            holder.itemView.context,
                            "You are already registered for this event",
                            Toast.LENGTH_LONG
                        ).show()
                        return
                    } else {
                        event.members.add(memberUID)
                        saveEvent(event)
                        addEventToUser(eventUid)
                    }
                } else {
                    // It is not possible to register because there is no space available at the event.
                    Toast.makeText(holder.itemView.context, "Failed to register", Toast.LENGTH_LONG)
                        .show()
                }


            }

            override fun onCancelled(po: DatabaseError) {
            }
        })
    }
     fun joinEvent(eventUid: String, holder: ViewHolder){
        val curUidUser = FirebaseAuth.getInstance().currentUser?.uid ?: return
        if(eventUid.isEmpty()){
            return
        }
        addMemberToEvent(eventUid, curUidUser, holder)
    }



    private fun openDialog(holder: ViewHolder, eventUid: String){
        val view = View.inflate(holder.itemView.context, R.layout.join_event_dialog, null)
        val builder = AlertDialog.Builder(holder.itemView.context)
        builder.setView(view)
        val dialog = builder.create()
        val width = (DisplayMetrics().widthPixels)
        val height = (DisplayMetrics().heightPixels * 0.4).toInt()
       dialog.window?.setLayout(width, height)
        dialog.show()
        view.leave_btn.setOnClickListener {
            joinEvent(eventUid, holder)
            dialog.cancel()
        }
        view.cancle_leave_btn.setOnClickListener {
            dialog.cancel()

        }
    }
    private fun loadEvent(snapshot: DataSnapshot):Event{
        val event :Event = Event()
        event.photoUrl = snapshot.child(" photoUrl").value.toString()
        event.uid = snapshot.child("uid").value.toString()
        event.eventUid =  snapshot.child("eventUid").value.toString()
        event.description =  snapshot.child("city").value.toString()
        return event;

    }
    private fun loadUser(snapshot: DataSnapshot):User{
        val user :User = User()
        user.firstName = snapshot.child("firstName").value.toString()
        user.lastName = snapshot.child("lastName").value.toString()
        user.email  =  snapshot.child("email").value.toString()
        user.city =  snapshot.child("city").value.toString()
        user.gender =  snapshot.child("gender").value.toString()
        user.uid = snapshot.child("uid").value.toString()
        user. url = snapshot.child("url").value.toString()
        return user;
    }
     private fun fetchAuthor(uid: String, holder: ViewHolder, position: Int){
         val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
         ref.addListenerForSingleValueEvent(object : ValueEventListener {
             override fun onDataChange(snapshot: DataSnapshot) {
                 val user: User? = loadUser(snapshot)
                 val targetImageView = holder.itemView.photo_event_new
                 targetImageView.setOnClickListener {
                     openProfile(holder, user)
                 }
                 Picasso.get().load(user?.url).into(targetImageView)
                 val targetName = holder.itemView.user_name_post
                 targetName.text = user?.firstName + " " + user?.lastName
                 targetName.setOnClickListener {
                     openProfile(holder, user)
                 }
                 val description = holder.itemView.description_post
                 description.text = events[position].description
                 val header = holder.itemView.headerPost
                 header.text = events[position].header
                 if (!checkIfAuthorIsCUrUser(user!!.uid,  FirebaseAuth.getInstance().currentUser?.uid)){
                     holder.itemView.post_image_chat_btn.setOnClickListener {
                         val intent = Intent(holder.itemView.context, chatLogActivity::class.java)
                         intent.putExtra(NewMessageActivity.USER_KEY, user)
                         holder.itemView.context.startActivity(intent)
                     }
                 }else{
                     holder.itemView.post_image_chat_btn.visibility=View.GONE
                 }
                 holder.itemView.members_info_bottom.setOnClickListener {
                     val i = Intent(holder.itemView.context, EventMembers::class.java)
                     i.putExtra("eventobj", events[position] as Parcelable?)
                     holder.itemView.context.startActivity(i)
                 }
                 holder.itemView.post_image_like_btn.setOnClickListener {
                     openDialog(holder, events[position].eventUid)
                 }
                 val mapImageView = holder.itemView.map_image_view
                 mapImageView.setOnClickListener {
                     openShowEventInMap(holder, events[position])
                 }
                 val date = holder.itemView.date
                 date.text = events[position].date
                 val hour = holder.itemView.hour
                 hour.text = events[position].hour
                 /*
                 val trash = holder.itemView.trash
                 if (user != null && user.uid == FirebaseAuth.getInstance().currentUser?.uid) {
                     // The user who created the event is the current user.
                     trash.visibility = View.VISIBLE
                 } else {
                     trash.visibility = View.GONE
                 }*/
             }

             override fun onCancelled(po: DatabaseError) {
             }
         })

    }

    private fun checkIfAuthorIsCUrUser(uidAuthor: String?, curUseruid: String?): Boolean {
        return uidAuthor.equals(curUseruid)
    }

    private fun openShowEventInMap(holder: ViewHolder, event: Event) {
        val intent = Intent(holder.itemView.context, ShowEventMap::class.java)
        intent.putExtra("event", event as Serializable)
        holder.itemView.context.startActivity(intent)
    }

    fun openProfile(holder: ViewHolder, user: User?){
        val intent = Intent(holder.itemView.context, ProfilePage::class.java)
        if (user != null) {
            intent.putExtra("EXTRA_SESSION_ID", user.uid)
        }
        holder.itemView.context.startActivity(intent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val uri = events[position].photoUrl
        val targetImageView = holder.itemView.post_image_home
        Picasso.get().load(uri).into(targetImageView)
        fetchAuthor(events[position].uid, holder, position)


    }
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val username:TextView = itemView.findViewById(R.id.user_name_post)

    }


}
