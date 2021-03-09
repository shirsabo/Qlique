package com.example.qlique

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat_log.*

class chatLogActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        supportActionBar?.title= user?.firstName+ " "+user?.lastName
        val adapter = GroupAdapter<com.xwray.groupie.GroupieViewHolder>()
        adapter.add(ChatFromItem())
        adapter.add(ChatToItem())
        adapter.add(ChatFromItem())
        adapter.add(ChatToItem())
        recyclerView_chat.adapter= adapter
    }
}
class ChatFromItem: Item<com.xwray.groupie.GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder,position:Int){

    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }

}
class ChatToItem: Item<com.xwray.groupie.GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder,position:Int){

    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }

}