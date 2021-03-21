package com.example.qlique

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*


class chatLogActivity : AppCompatActivity() {
    val adapter = GroupAdapter<com.xwray.groupie.GroupieViewHolder>()
    //var toUser:User?=intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)
        recyclerView_chat.adapter= adapter
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        supportActionBar?.title= user?.firstName+ " "+user?.lastName
        //dummySetUP()
        sendBtn.setOnClickListener{
            performSendMessage()
            text_message_chat.text.clear()
        }
        listenForMessages()
    }
    class chatMessage(val id:String, val text: String,
    val fromId:String, val toId:String,val timeStamp: Long){
        constructor(): this("","","",
            "", -1)

    }
    fun performSendMessage(){
        val textInput = text_message_chat.text.toString()
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val fromId =  FirebaseAuth.getInstance().uid
        val toId = user.uid
        val ref = FirebaseDatabase.getInstance().getReference("/messages").push()
        val chatMsg = chatMessage(ref.key!!,textInput,fromId!!,toId,System.currentTimeMillis()/1000)
        ref.setValue(chatMsg)
    }
    fun listenForMessages(){
        val ref = FirebaseDatabase.getInstance().getReference("/messages")
        ref.addChildEventListener(object :ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val msg = snapshot.getValue(chatMessage::class.java)
                val toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
                if (msg!=null){
                    if(msg.fromId==FirebaseAuth.getInstance().uid && msg.toId==toUser!!.uid ){
                        val curUser = ChatListActivity.currentUser
                        adapter.add(ChatFromItem(msg.text,curUser!!))
                    }else if (msg.fromId==toUser!!.uid && msg.toId==FirebaseAuth.getInstance().uid){
                            adapter.add(ChatToItem(msg.text,toUser!!))
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

        })

    }
}
class ChatFromItem(val string: String,val user:User): Item<com.xwray.groupie.GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder,position:Int){
        viewHolder.itemView.messageFrom.text= string
        //load user image
        val uri = user.url
        val targetImageView = viewHolder.itemView.circularImageViewFrom
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }

}
class ChatToItem(val string: String,val user:User): Item<com.xwray.groupie.GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder,position:Int){
        viewHolder.itemView.messageTo.text = string
        val uri = user.url
        val targetImageView = viewHolder.itemView.circularImageViewTo
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }

}