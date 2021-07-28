package com.example.qlique.Feed

import android.annotation.SuppressLint
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
import com.example.qlique.ChatLogActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.join_event_dialog.view.*
import kotlinx.android.synthetic.main.post_in_feed.view.*
import java.io.Serializable
/**
 * PostAdapter.
 * holds [events] fetched rom firebase
 * configures the fcm token, google services etc(google map,account) and firebase.
 */
class PostAdapter(val events: ArrayList<Event>) :RecyclerView.Adapter<PostAdapter.ViewHolder>(){
    val heightPixelPrecents=0.4
    companion object{
        /**
         * Static function which can be called from outside the class, adds the event to the user
         * by the [eventUid].
         */
        fun  addEventToUser(eventUid: String){
            val refPost = FirebaseDatabase.getInstance().getReference("/users/${FirebaseAuth.getInstance().currentUser?.uid}")
            refPost.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user: User = (snapshot.getValue(User::class.java))
                        ?: return
                    if (user.events == null) {
                        user.events = java.util.ArrayList() //creates empty list
                    }
                    if( user.events.contains(eventUid)){ //already signed
                        return
                    }
                    user.events.add(eventUid)
                    refPost.setValue(user)//updates the user's data (the new event)
                }
                override fun onCancelled(po: DatabaseError) {
                }
            })

        }
    }
    /**
     * By [parent] ViewGroup, [viewType] Int returns the configured [ViewHolder].
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view:View=LayoutInflater.from(parent.context).inflate(
            R.layout.post_in_feed,
            parent,
            false
        )
        return ViewHolder(view) // in order to get the configured ViewHolder
    }
    /**
     * @return the number of items in the adapter , in our case, the number of events.
     */
    override fun getItemCount(): Int {
        return events.size

    }
    /**
     * Saves event in [FirebaseDatabase] when update is needed.
     */
     fun saveEvent(event: Event){
        val refPost = FirebaseDatabase.getInstance().getReference("/posts/${event.eventUid}")
        refPost.setValue(event) // saves the updated users in the DB
    }
    /**
     * Adds [memberUID] to the specified event by its [eventUid].
     */
    private fun addMemberToEvent(eventUid: String, memberUID: String, holder: ViewHolder){
        val refPost = FirebaseDatabase.getInstance().getReference("/posts/$eventUid")
        refPost.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val event: Event = snapshot.getValue(Event::class.java) ?: return
                event.setEventUid(eventUid)
                event.description = snapshot.child("description").value.toString()
                event.uid = snapshot.child("uid").value.toString()
                if (event.members == null) {
                    event.members = java.util.ArrayList() //creates empty array
                }
                // if there is a place for another member
                if (event.membersCapacity > event.members?.size!!) {
                    //if the user is already a member of this event
                    if (event.members.contains(memberUID)) {
                        Toast.makeText(
                            holder.itemView.context,
                            "You are already registered for this event",
                            Toast.LENGTH_LONG
                        ).show()
                        return
                    } else {
                        //if not a member already, adds the user to the event and update it in the event's field and the user's field
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
    /**
     * Adds the eventUid of the joined event to the current user's events list.
     */
    private fun joinEvent(eventUid: String, holder: ViewHolder){
        val curUidUser = FirebaseAuth.getInstance().currentUser?.uid ?: return // if null return
        if(eventUid.isEmpty()){
            return
        }
        addMemberToEvent(eventUid, curUidUser, holder)
    }
    /**
     * Opens the Dialog for joining to an event.
     * The event is known by [eventUid]
     * with [holder] we can get the context of the itemView
     */
    private fun openDialog(holder: ViewHolder, eventUid: String){
        val view = View.inflate(holder.itemView.context, R.layout.join_event_dialog, null)
        val builder = AlertDialog.Builder(holder.itemView.context)
        builder.setView(view)
        //sets the dialog's size and configurations
        val dialog = builder.create()
        val width = (DisplayMetrics().widthPixels)
        val height = (DisplayMetrics().heightPixels * heightPixelPrecents).toInt()
       dialog.window?.setLayout(width, height) //sets the size
        dialog.show()
        //when the "Join" button is clicked the joinEvent function is called
        view.join_btn.setOnClickListener {
            joinEvent(eventUid, holder)
            dialog.cancel()
        }
        //when the "Cancel" button is clicked the dialog is canceled
        view.cancle_join_btn.setOnClickListener {
            dialog.cancel()
        }
    }
    /**
     *Sets the users fields from [snapshot]
     * @return updated User object.
     */
    private fun loadUser(snapshot: DataSnapshot):User{
        val user = User()
        user.firstName = snapshot.child("firstName").value.toString()
        user.lastName = snapshot.child("lastName").value.toString()
        user.email  =  snapshot.child("email").value.toString()
        user.city =  snapshot.child("city").value.toString()
        user.gender =  snapshot.child("gender").value.toString()
        user.uid = snapshot.child("uid").value.toString()
        user. url = snapshot.child("url").value.toString()
        return user;
    }
    /**
     *Responsible for collecting and presenting all of the Author's data of the post and the
     *  functionality by the Author's [uid] and the position of the post in the adapter
     *  that matches the[events] array.
     */
     private fun fetchAuthor(uid: String, holder: ViewHolder, position: Int){
         val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
         ref.addListenerForSingleValueEvent(object : ValueEventListener {
             @SuppressLint("SetTextI18n")
             override fun onDataChange(snapshot: DataSnapshot) {
                 val user: User = loadUser(snapshot)
                 val targetImageView = holder.itemView.photo_event_new
                 //when clicking the image, the profile activity will start
                 targetImageView.setOnClickListener {
                     openProfile(holder, user)
                 }
                 //loads the specified photo into the ImageView
                 Picasso.get().load(user.url).into(targetImageView)
                 val targetName = holder.itemView.user_name_post
                 targetName.text = user.firstName + " " + user.lastName
                 //when clicking of the username's name , the profile activity will start
                 targetName.setOnClickListener {
                     openProfile(holder, user)
                 }
                 val description = holder.itemView.description_post
                 //gets the description by the position of the event in the adapter
                 description.text = events[position].description
                 val header = holder.itemView.headerPost // the header pf the event
                 header.text = events[position].header
                 if (!checkIfAuthorIsCUrUser(user.uid,  FirebaseAuth.getInstance().currentUser?.uid)){
                     //sets onclick event which opens the ChatLogActivity
                     holder.itemView.post_image_chat_btn.setOnClickListener {
                         val intent = Intent(holder.itemView.context, ChatLogActivity::class.java)
                         //passes the use's key to the intent
                         intent.putExtra(NewMessageActivity.USER_KEY, user)
                         holder.itemView.context.startActivity(intent)
                     }
                 }else{
                     //if it is the same person we wont able sending messages
                     holder.itemView.post_image_chat_btn.visibility=View.GONE
                 }
                 //when members icon is clicked the members dialog shows
                 holder.itemView.members_info_bottom.setOnClickListener {
                     val i = Intent(holder.itemView.context, EventMembers::class.java)
                     //passes the event obj to the activity
                     i.putExtra("eventobj", events[position] as Parcelable?)
                     holder.itemView.context.startActivity(i)
                 }
                 //when the "join" icon is clicked it opens the Dialog for joining the event.
                 holder.itemView.post_image_like_btn.setOnClickListener {
                     openDialog(holder, events[position].eventUid)
                 }
                 val mapImageView = holder.itemView.map_image_view
                 //when the Map icon is clicked it opens the activity that shows the event in the map
                 mapImageView.setOnClickListener {
                     openShowEventInMap(holder, events[position])
                 }
                 //sets the date and hour of the event
                 val date = holder.itemView.date
                 date.text = events[position].date
                 val hour = holder.itemView.hour
                 hour.text = events[position].hour
             }
             override fun onCancelled(po: DatabaseError) {
             }
         })
    }
    /**
     * Checks whether [uidAuthor] and [curUseruid] are equal.
     * @return True if equals , else False
     */
    private fun checkIfAuthorIsCUrUser(uidAuthor: String?, curUserUid: String?): Boolean {
        return uidAuthor.equals(curUserUid)
    }
    /**
     *With the [holder] it gets the itemView's context and open the specified event in [ShowEventMap].
     */
    private fun openShowEventInMap(holder: ViewHolder, event: Event) {
        val intent = Intent(holder.itemView.context, ShowEventMap::class.java)
        intent.putExtra("event", event as Serializable)//passes the event object to the intent
        holder.itemView.context.startActivity(intent)
    }
    /**
     *With the [holder] it gets the itemView's context and open the specified event in [ProfilePage]
     * with the user.uid in order to show the Author's data.
     */
    fun openProfile(holder: ViewHolder, user: User?){
        val intent = Intent(holder.itemView.context, ProfilePage::class.java)
        if (user != null) {
            intent.putExtra("EXTRA_SESSION_ID", user.uid)//passes the user.uid object to the intent
            holder.itemView.context.startActivity(intent)
        }

    }
    /**
     *by the [holder] it gets the itemView's [post_image_home] and presents the author's data
     *  of the post.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // gets the url of the event by the position in the adapter
        val url = events[position].photoUrl
        val targetImageView = holder.itemView.post_image_home
        Picasso.get().load(url).into(targetImageView)//loads the photo to the target ImageView  by the url
        fetchAuthor(events[position].uid, holder, position)
    }
    /**
     *Class ViewHolder that holds the [username] of the posts by the [itemView].
     */
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        //by the itemView, we can get the wanted username
        val username:TextView = itemView.findViewById(R.id.user_name_post)
    }
}
