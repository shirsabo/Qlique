package com.example.qlique.EventsDisplay;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.qlique.Profile.User;
import com.example.qlique.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import com.example.qlique.CreateEvent.Event;

public class EventsManagerAdapter extends RecyclerView.Adapter<EventsManagerAdapter.ViewHolder> {

    private LayoutInflater inflater;
    private Event[] events;

    EventsManagerAdapter(Context context,Event[] events){
        this.inflater = LayoutInflater.from(context);
        this.events = events;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = inflater.inflate(R.layout.event_custom,viewGroup,false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("users/"+events[i].uid);
// Attach a listener to read the data at our posts reference
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                String  uri = events[i].photoUrl;
                ImageView targetImageView = viewHolder.itemView.findViewById(R.id.cardImage);
                ImageView targetAuthorImageView = viewHolder.itemView.findViewById(R.id.photo_event_new);
                TextView targetTextView = viewHolder.itemView.findViewById(R.id.desc_card);
                Picasso.get().load(uri).into(targetImageView);
                Picasso.get().load(user.url).into(targetAuthorImageView);
                targetImageView.setColorFilter(Color.argb(155, 0, 0, 0), PorterDuff.Mode.SRC_ATOP);
                TextView targetAuthor =viewHolder.itemView.findViewById(R.id.member_username);
                targetAuthor.setText(user.firstName+" "+user.lastName);
                targetTextView.setText(events[i].description);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });

    }

    @Override
    public int getItemCount() {
        return events.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // implement onClick
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent i = new Intent(v.getContext(), EventInfo.class);
                    // send story title and contents through recyclerview to detail activity
                    Event event =events[getAdapterPosition()];
                    i.putExtra("event", (Parcelable) events[getAdapterPosition()]);
                    v.getContext().startActivity(i);
                }
            });
        }
    }

}

