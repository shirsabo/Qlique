package com.example.qlique

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.qlique.Profile.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.post.view.*


class postAdapter(val events: ArrayList<Event>) :RecyclerView.Adapter<postAdapter.ViewHolder>(){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): postAdapter.ViewHolder {
        val view:View=LayoutInflater.from(parent.context).inflate(R.layout.post,parent,false)
        return ViewHolder(view)

    }

    override fun getItemCount(): Int {
        return events.size

    }
     fun fetchAuthor(uid:String,holder: postAdapter.ViewHolder,position: Int){

         val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
         ref.addListenerForSingleValueEvent(object: ValueEventListener {
             override fun onDataChange(snapshot: DataSnapshot) {
                 val user :User? = snapshot.getValue(User::class.java)
                 val targetImageView = holder.itemView.member_photo
                 Picasso.get().load( user?.url).into(targetImageView)
                 val targetName = holder.itemView.user_name_post
                 targetName.text=user?.firstName+" "+user?.lastName
                 val description = holder.itemView.description_post
                 description.text = events[position].description
                 holder.itemView.post_image_chat_btn.setOnClickListener {
                     val intent = Intent (holder.itemView.context,chatLogActivity::class.java)
                     intent.putExtra(NewMessageActivity.USER_KEY,user)
                     holder.itemView.context.startActivity(intent)
                 }

             }
             override fun onCancelled(po: DatabaseError) {
             }
         })

    }

    override fun onBindViewHolder(holder: postAdapter.ViewHolder, position: Int) {
        val uri = events[position].photoUrl
        val targetImageView = holder.itemView.post_image_home
        Picasso.get().load(uri).into(targetImageView)
        fetchAuthor(events[position].uid,holder,position)


    }
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val username:TextView = itemView.findViewById(R.id.user_name_post)

    }

}
