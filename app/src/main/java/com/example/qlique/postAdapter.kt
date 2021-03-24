package com.example.qlique

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class postAdapter(val events: ArrayList<Event>) :RecyclerView.Adapter<postAdapter.ViewHolder>(){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): postAdapter.ViewHolder {
        val view:View=LayoutInflater.from(parent.context).inflate(R.layout.post,parent,false)
        return ViewHolder(view)

    }

    override fun getItemCount(): Int {
        return events.size

    }

    override fun onBindViewHolder(holder: postAdapter.ViewHolder, position: Int) {
       holder.username.text = "Clique"

    }
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val username:TextView = itemView.findViewById(R.id.user_name_post)

    }
}
