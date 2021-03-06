package com.example.qlique

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.activity_profile_page.*

class NewMessageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)
        supportActionBar?.title="Select User"
        val adapter =GroupAdapter<com.xwray.groupie.GroupieViewHolder>()
        adapter.add(UserItem())
        adapter.add(UserItem())
        adapter.add(UserItem())
        newMessageRecycle.adapter= adapter
        adapter.setOnItemClickListener{item,view->
            val intent = Intent (view.context,chatLogActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
class UserItem: Item<com.xwray.groupie.GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        // later on..
    }
    override fun getLayout(): Int {
       return R.layout.user_row_new_message
    }
}

