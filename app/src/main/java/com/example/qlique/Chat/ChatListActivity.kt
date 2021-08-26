package com.example.qlique.Chat

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.qlique.LoginAndSignUp.LoginActivity
import com.example.qlique.LoginAndSignUp.SignupActivity
import com.example.qlique.NewMessageActivity
import com.example.qlique.R
import com.example.qlique.Profile.User
import com.example.qlique.ChatLogActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_chat_list.*
import kotlinx.android.synthetic.main.latest_message_row.view.*
import kotlin.collections.HashMap
/**
 * Class ChatListActivity.
 * This class is responsible for showing the chat activity with other user, get and post messages
 * live in/from Firebase
 */
class ChatListActivity: AppCompatActivity()  {
    val adapter = GroupAdapter<com.xwray.groupie.GroupieViewHolder>()
    private lateinit var back: Button
    @Override
    /**
     * Init the recycleView , listens for messages checks if user is logged in
     * @return parts- the data
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)
        recycleView_latestMessages.adapter = adapter
        recycleView_latestMessages.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))
        adapter.setOnItemClickListener{item, view ->
            val intent = Intent(this, ChatLogActivity::class.java)
            //posting the message to Firebase as last message
            val row = item as LatestMessageRow
            intent.putExtra(NewMessageActivity.USER_KEY,row.chatPartnerUser)
            startActivity(intent)

        }
        listenForLatestMessages()
        fetchCurrentUser() //Fetches user's Data
        verifyUserIsLoggedIn()
        back = findViewById(R.id.back_button)
        back.setOnClickListener { //when clicked- we go back to the previous activity
            finish()
        }
    }

    /**
     * The object which the adapter holds.
     */
    class LatestMessageRow(val chatMessage: ChatLogActivity.ChatMessage): com.xwray.groupie.Item<GroupieViewHolder>() {
        var chatPartnerUser : User?=null
        /**
         * Loads the user by the snapshot returned from Firebase
         * @param snapshot - snapshot returned from Firebase
         */
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
        /**
         * Binding between the element and what needed to be shown by the position
         * @param viewHolder- GroupieViewHolder, position: the element's position
         */
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.latest_msg_latest_message.text = chatMessage.text
            var chatPartnerId:String
            //checks how if this message is internal or external
            if(chatMessage.fromId == FirebaseAuth.getInstance().uid){
                chatPartnerId = chatMessage.toId
            }else{
                chatPartnerId = chatMessage.fromId
            }
            val ref = FirebaseDatabase.getInstance().getReference("users/$chatPartnerId")
            ref.addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //data configurations
                   chatPartnerUser = loadUser(snapshot)
                    viewHolder.itemView.user_name_latest.text =
                        chatPartnerUser?.firstName +" " + chatPartnerUser?.lastName
                    val targetImageView = viewHolder.itemView.circularImageViewLatestMsg
                    if(chatPartnerUser?.url!=null&&chatPartnerUser?.url!=""){
                        //show the picture according to the url
                        Picasso.get().load(chatPartnerUser?.url).into(targetImageView)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }

            })
        }
        /**
         * Returns the ID  to the element.
         */
        override fun getLayout(): Int {
            return R.layout.latest_message_row
        }
    }
    val latestMessagesMap = HashMap<String, ChatLogActivity.ChatMessage>()
    /**
     * Refresh RecycleVie: clears adapter and  sorts by timeStamp.
     */
    private fun refreshRecycleViewMessages(){
        adapter.clear() // in order to refresh we first need to clear
        var messages = mutableListOf<ChatLogActivity.ChatMessage>()
        latestMessagesMap.values.forEach{
            messages.add(it) // get all the messages from dictionary
        }
        messages.sortBy { it.timeStamp} //sort the messages with increasing order
        // reverse the lists so that it will show the latest messages first!
        messages= messages.asIterable().reversed() as MutableList<ChatLogActivity.ChatMessage>
        messages.forEach {
            adapter.add(LatestMessageRow(it))
        }

    }
    /**
     * listens to the event which message is posted to Firebase.
     */
    private fun listenForLatestMessages(){
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")
        //listens for new message added to the path
        ref.addChildEventListener(object :ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatLogActivity.ChatMessage::class.java)?:return
                latestMessagesMap[snapshot.key!!] = chatMessage // inserts to map
                refreshRecycleViewMessages() // refreshes recyclerView
            }
            // Not relevant
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }
            /**
             * When message is changed - refresh.
             * @param snapshot- DataSnapshot
             * @param previousChildName- String?
             */
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMsg = snapshot.getValue(ChatLogActivity.ChatMessage::class.java)?:return
                latestMessagesMap[snapshot.key!!] = chatMsg
                refreshRecycleViewMessages()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
    /**
     * Loads user's data.
     * @param snapshot- DataSnapshot returned from Firebase
     */
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
    /**
     * Fetches current user's data.
     */
    private fun fetchCurrentUser(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //Sets current user.
                SignupActivity.currentUser = loadUser(snapshot)
            }
            override fun onCancelled(po: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
    /**
     * Checks whether user is still logged i in in order to show its messages
     * history and allow to send messages.
     */
    private fun verifyUserIsLoggedIn(){
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null){
            // If user is not logged in.
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
    /**
     * When Selected users is logged out.
     * @param item - MenuItem
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
           R.id.menu_sign_out ->{
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }

        }
        return super.onOptionsItemSelected(item)
    }
    /**
     * Configures menu and super class functionality
     * @param menu - Menu?
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }
}