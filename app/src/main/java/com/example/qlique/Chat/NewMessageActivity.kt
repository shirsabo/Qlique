package com.example.qlique
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.qlique.Profile.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.user_row_new_message.view.*

var adapter=GroupAdapter<com.xwray.groupie.GroupieViewHolder>()
val friendsAdded = arrayListOf<String>()

class NewMessageActivity : AppCompatActivity() {
    companion object {
        const val USER_KEY="USER_KEY"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)
        supportActionBar?.title="Select User"
        fetchFriends()
        newMessageRecycle.adapter= adapter
        adapter.setOnItemClickListener{item,view->
            val userItemObj = item as UserItem
            val intent = Intent (view.context,ChatLogActivity::class.java)
            intent.putExtra(USER_KEY,userItemObj.user)
            startActivity(intent)
            finish()
        }
    }
}
private fun loadUser(snapshot: DataSnapshot):User{
    val user = User()
    user.firstName = snapshot.child("firstName").value.toString()
    user.lastName = snapshot.child("lastName").value.toString()
    user.email  =  snapshot.child("email").value.toString()
    user.city =  snapshot.child("city").value.toString()
    user.gender =  snapshot.child("gender").value.toString()
    user.uid = snapshot.child("uid").value.toString()
    user. url = snapshot.child("url").value.toString()
    return user
}
private fun fetchFriends(){
    val mFirebaseInstance= FirebaseDatabase.getInstance()
    val mFirebaseDatabase= mFirebaseInstance.getReference("users")
    val userId=FirebaseAuth.getInstance().uid
    mFirebaseDatabase.child(userId!!).addValueEventListener(object: ValueEventListener{
        override fun onDataChange(dataSnapshot: DataSnapshot){
            val user=dataSnapshot.getValue(User::class.java)
            if (user!=null){
                for (friendKey in user.getFriends()){
                    if (friendKey!=null){
                        val newUser =Firebase.database.reference.child("users").child(friendKey).addValueEventListener(object :ValueEventListener{
                            override fun onDataChange(dataSnapshot: DataSnapshot){
                                val user1= loadUser(dataSnapshot)
                                if (!friendsAdded.contains(friendKey)){
                                    adapter.add(UserItem(user1))
                                    friendsAdded.add(friendKey)
                                }
                            }
                            override fun onCancelled(error: DatabaseError){
                            }
                        })
                    }
                }
            }
        }
        override fun onCancelled(error: DatabaseError){
        }
    })

}

class UserItem(val user: User): Item<com.xwray.groupie.GroupieViewHolder>(){
    @SuppressLint("SetTextI18n")
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
       viewHolder.itemView.user_name.text= user.firstName+" "+user.lastName
        val ur = user.url
       Picasso.get().load(ur).into(viewHolder.itemView.ProfileCircularImage)
    }
    override fun getLayout(): Int {
       return R.layout.user_row_new_message
    }
}

