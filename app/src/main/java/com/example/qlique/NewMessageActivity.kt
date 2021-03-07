package com.example.qlique

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_new_message.*


class NewMessageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)
        supportActionBar?.title="Select User"
        val adapter = GroupAdapter<com.xwray.groupie.GroupieViewHolder>()
        fetchFriends()
        newMessageRecycle.adapter= adapter
        adapter.setOnItemClickListener{item,view->
            val intent = Intent (view.context,chatLogActivity::class.java)
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
            if(user!=null){
                for (friend in user.getFriends()){ val q: Query = mFirebaseDatabase.child("users").orderByChild("email")
                        .equalTo(friend)
                    if(q!=null){
                        q.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                for (data in dataSnapshot.children) {
                                    val models: User? = data.getValue(User::class.java)
                                    Log.d("tag",models?.getFirstName())
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {}
                        })


                    }

                }
            }



        }
        override fun onCancelled(error: DatabaseError){
            //Failed to read value
        }
    })

}


class UserItem: Item<com.xwray.groupie.GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        // later on..
    }
    override fun getLayout(): Int {
       return R.layout.user_row_new_message
    }
}

