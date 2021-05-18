package Feed

import CreateEvent.Event
import android.app.AlertDialog
import android.content.Intent
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.qlique.NewMessageActivity
import com.example.qlique.Profile.ProfilePage
import com.example.qlique.Profile.User
import com.example.qlique.R
import com.example.qlique.chatLogActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.post.view.*


class postAdapter(val events: ArrayList<Event>) :RecyclerView.Adapter<postAdapter.ViewHolder>(){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view:View=LayoutInflater.from(parent.context).inflate(R.layout.post, parent, false)
        return ViewHolder(view)

    }

    override fun getItemCount(): Int {
        return events.size

    }
    private fun openDialog(holder: ViewHolder){
        val view = View.inflate(holder.itemView.context, R.layout.join_event_dialog, null)
        val builder = AlertDialog.Builder(holder.itemView.context)
        builder.setView(view)
        val dialog = builder.create()
        val width = (DisplayMetrics().widthPixels)
        val height = (DisplayMetrics().heightPixels * 0.4).toInt()
       dialog.window?.setLayout(width, height)
        dialog.show()



    }
     fun fetchAuthor(uid: String, holder: ViewHolder, position: Int){

         val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
         ref.addListenerForSingleValueEvent(object : ValueEventListener {
             override fun onDataChange(snapshot: DataSnapshot) {
                 val user: User? = snapshot.getValue(User::class.java)
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
                 holder.itemView.post_image_chat_btn.setOnClickListener {
                     val intent = Intent(holder.itemView.context, chatLogActivity::class.java)
                     intent.putExtra(NewMessageActivity.USER_KEY, user)
                     holder.itemView.context.startActivity(intent)
                 }

                 holder.itemView.post_image_like_btn.setOnClickListener {
                     openDialog(holder)
                 }
             }

             override fun onCancelled(po: DatabaseError) {
             }
         })

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