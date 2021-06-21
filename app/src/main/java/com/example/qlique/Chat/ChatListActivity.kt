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
import com.example.qlique.chatLogActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_chat_list.*
import kotlinx.android.synthetic.main.latest_message_row.view.*

class ChatListActivity: AppCompatActivity()  {
    val adapter = GroupAdapter<com.xwray.groupie.GroupieViewHolder>()
    private lateinit var back: Button
    @Override

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)
        recycleView_latestMessages.adapter=adapter
        recycleView_latestMessages.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))
        adapter.setOnItemClickListener{item, view ->
            val intent = Intent(this, chatLogActivity::class.java)
            val row = item as LatestMessageRow
            intent.putExtra(NewMessageActivity.USER_KEY,row.chatPartnerUser)
            startActivity(intent)

        }
        listenForLatestMessages()
        //setupDummyRows()
        fetchCurrentUser()
        verifyUserIsLoggedIn()
        back = findViewById(R.id.back_button)
        back.setOnClickListener {
            finish()
        }
    }


    class LatestMessageRow(val chatMessage: chatLogActivity.chatMessage): com.xwray.groupie.Item<GroupieViewHolder>() {
        var chatPartnerUser : User?=null
        private fun loadUser(snapshot: DataSnapshot):User{
            val user :User = User()
            /*
                public String firstName, lastName, email, city, gender, uid, url, instagramUserName;
        public List<String> friends;
        public List<String> hobbies;
        public List<String> events;*/
            user.firstName = snapshot.child("firstName").value.toString()
            user.lastName = snapshot.child("lastName").value.toString()
            user.email  =  snapshot.child("email").value.toString()
            user.city =  snapshot.child("city").value.toString()
            user.gender =  snapshot.child("gender").value.toString()
            user.uid = snapshot.child("uid").value.toString()
            user. url = snapshot.child("url").value.toString()
            return user;
        }
        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
            viewHolder.itemView.latest_msg_latest_message.text=chatMessage.text
            var chatPartnerId:String
            if(chatMessage.fromId==FirebaseAuth.getInstance().uid){
                chatPartnerId = chatMessage.toId
            }else{
                chatPartnerId = chatMessage.fromId
            }
            val ref = FirebaseDatabase.getInstance().getReference("users/$chatPartnerId")
            ref.addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                   chatPartnerUser = loadUser(snapshot)
                    viewHolder.itemView.user_name_latest.text= chatPartnerUser?.firstName +" " + chatPartnerUser?.lastName
                    val targetImageView = viewHolder.itemView.circularImageViewLatestMsg
                    if(chatPartnerUser?.url!=null&&chatPartnerUser?.url!=""){
                        Picasso.get().load(chatPartnerUser?.url).into(targetImageView)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }

            })
        }

        override fun getLayout(): Int {
            return R.layout.latest_message_row
        }
    }
    val latestMessagesMap = HashMap<String, chatLogActivity.chatMessage>()
    private fun refreshRecycleViewMessages(){
        adapter.clear()
        latestMessagesMap.toList().sortedBy { (_, value) -> value.timeStamp}.toMap()
        latestMessagesMap.toSortedMap(reverseOrder())
        latestMessagesMap.values.forEach{
            adapter.add(LatestMessageRow(it))
        }
    }
    private fun listenForLatestMessages(){
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")
        ref.addChildEventListener(object :ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(chatLogActivity.chatMessage::class.java)?:return
                latestMessagesMap[snapshot.key!!]=chatMessage
                refreshRecycleViewMessages()
            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMsg = snapshot.getValue(chatLogActivity.chatMessage::class.java)?:return
                latestMessagesMap[snapshot.key!!]=chatMsg
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
    private fun loadUser(snapshot: DataSnapshot):User{
        val user :User = User()
        /*
            public String firstName, lastName, email, city, gender, uid, url, instagramUserName;
    public List<String> friends;
    public List<String> hobbies;
    public List<String> events;*/
        user.firstName = snapshot.child("firstName").value.toString()
        user.lastName = snapshot.child("lastName").value.toString()
        user.email  =  snapshot.child("email").value.toString()
        user.city =  snapshot.child("city").value.toString()
        user.gender =  snapshot.child("gender").value.toString()
        user.uid = snapshot.child("uid").value.toString()
        user. url = snapshot.child("url").value.toString()
        return user;
    }
    private fun fetchCurrentUser(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                SignupActivity.currentUser =loadUser(snapshot)
            }
            override fun onCancelled(po: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    }
    private fun verifyUserIsLoggedIn(){
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null){
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
           R.id.menu_sign_out ->{
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }

        }
        return super.onOptionsItemSelected(item)
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }
}