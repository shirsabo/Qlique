package com.example.qlique.CreateEvent;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.example.qlique.NewMessageActivity;
import com.example.qlique.Profile.User;
import com.example.qlique.R;
import com.example.qlique.chatLogActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ktx.Firebase;
import com.squareup.picasso.Picasso;

public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.ViewHolder> {
    private LayoutInflater inflater;
    private String[] members;

    MembersAdapter(Context context,String[] events){
        this.inflater = LayoutInflater.from(context);
        this.members = events;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = inflater.inflate(R.layout.member_row_event,viewGroup,false);
        return new ViewHolder(view);
    }

   private User loadUser(DataSnapshot snapshot){
        User user = new User();
        user.firstName = snapshot.child("firstName").getValue().toString();
        user.lastName = snapshot.child("lastName").getValue().toString();
        user.email  =  snapshot.child("email").getValue().toString();
        user.city =  snapshot.child("city").getValue().toString();
        user.gender =  snapshot.child("gender").getValue().toString();
        user.uid = snapshot.child("uid").getValue().toString();
        user. url = snapshot.child("url").getValue().toString();
        return user;
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("users/"+members[i]);
        // Attach a listener to read the data at our posts reference
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = loadUser(dataSnapshot);
                String  url_profile = user.url;
                ImageView targetAuthorImageView = viewHolder.itemView.findViewById(R.id.photo_event_new);
                TextView targetAuthor = viewHolder.itemView.findViewById(R.id.member_username);
                Picasso.get().load(url_profile).into(targetAuthorImageView);
                targetAuthor.setText(user.firstName+" "+user.lastName);
                ImageView chat = viewHolder.itemView.findViewById(R.id.send_msg_member);
                if (user.uid.equals(FirebaseAuth.getInstance().getUid())){
                    chat.setVisibility(View.GONE);
                } else {
                    chat.setOnClickListener(v -> {
                        Intent intent = new Intent(viewHolder.itemView.getContext(), chatLogActivity.class);
                        intent.putExtra(NewMessageActivity.USER_KEY, user);
                        viewHolder.itemView.getContext().startActivity(intent);
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });

    }

    @Override
    public int getItemCount() {
        return members.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // implement onClick
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*
                    Intent i = new Intent(v.getContext(),EventInfo.class);
                    // send story title and contents through recyclerview to detail activity
                    Event event =events[getAdapterPosition()];
                    i.putExtra("event", (Parcelable) events[getAdapterPosition()]);
                    v.getContext().startActivity(i);
                    */
                }
            });
        }
    }

}

