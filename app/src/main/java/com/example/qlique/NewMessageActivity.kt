package com.example.qlique

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.user_row_new_message.view.*

public var adapter=GroupAdapter<com.xwray.groupie.GroupieViewHolder>()

class NewMessageActivity : AppCompatActivity() {
    companion object {
        val USER_KEY="USER_KEY"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)
        supportActionBar?.title="Select User"
        //adapter = GroupAdapter<com.xwray.groupie.GroupieViewHolder>()

        fetchFriends()
        newMessageRecycle.adapter= adapter
        adapter.setOnItemClickListener{item,view->
            val userItemObj = item as UserItem
            val intent = Intent (view.context,chatLogActivity::class.java)
            intent.putExtra(USER_KEY,userItemObj.user)
            startActivity(intent)
            finish()
        }

    }
}

private fun fetchFriends(){
    val mFirebaseInstance= FirebaseDatabase.getInstance()
    val mFirebaseDatabase=mFirebaseInstance!!.getReference("users")
    val userId=FirebaseAuth.getInstance().uid
    mFirebaseDatabase!!.child(userId!!).addValueEventListener(object: ValueEventListener{
        override fun onDataChange(dataSnapshot: DataSnapshot){
            val user=dataSnapshot.getValue(User::class.java)
            if (user!=null){
                for (friendKey in user.getFriends()){
                    if (friendKey!=null){
                        val  userFriend=mFirebaseInstance!!.getReference("users/$friendKey")
                        if(userFriend!=null){
                            val newUser =Firebase.database.reference.child("users").child(friendKey).addValueEventListener(object :ValueEventListener{
                                override fun onDataChange(dataSnapshot: DataSnapshot){
                                    val user1=dataSnapshot.getValue(User::class.java)
                                    if (user1!=null){
                                        adapter.add(UserItem(user1))
                                    }
                                }
                                override fun onCancelled(error: DatabaseError){
                                    //Failed to read value
                                }
                            })
                            if(newUser!=null){
                                Log.d("tag","hello")
                                val res =newUser
                            }

                        }
                    }

                }
            }

        }
        override fun onCancelled(error: DatabaseError){
            //Failed to read value
        }
    })

}
/*
private fetchUser(){

}
*/

class UserItem(val user: User): Item<com.xwray.groupie.GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
       viewHolder.itemView.user_name.text= user.firstName+" "+user.lastName
        val ur = user.url
        val mStorageRef = FirebaseStorage.getInstance().getReference();
       Picasso.get().load(ur).into(viewHolder.itemView.circularImageView)
    }
    override fun getLayout(): Int {

       return R.layout.user_row_new_message
    }
}

