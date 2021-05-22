package com.example.qlique.EventsDisplay;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.qlique.CreateEvent.EventMembers;
import com.example.qlique.Profile.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qlique.R;
import com.google.android.material.navigation.NavigationView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

import com.example.qlique.CreateEvent.Event;


public class EventsManager extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    RecyclerView recyclerView;
    private GroupAdapter<GroupieViewHolder> groupieAdapter =
            new GroupAdapter<com.xwray.groupie.GroupieViewHolder>();
    private void addEvents(String eventIn){
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("posts/"+eventIn);
        // Attach a listener to read the data at our posts reference
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Event event = dataSnapshot.getValue(Event.class);
                event.uid = dataSnapshot.child("uid").getValue().toString();
                /** TO DO: Check that the event has not yet occurred **/
                if(event==null){return;}
                groupieAdapter.add(new EventItem(event));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }

        });


    }
    private void fetchEvents(){
        final Semaphore semaphore = new Semaphore(0);
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("users/"+ FirebaseAuth.getInstance().getCurrentUser().getUid());
        // Attach a listener to read the data at our posts reference
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                TextView noEventsText = findViewById(R.id.no_events_text_view);
                User user = dataSnapshot.getValue(User.class);
                if (user != null && user.events != null) {
                    noEventsText.setVisibility(View.GONE);
                    int n = user.events.size();
                    for (int i = 0; i < n; i++) {
                        addEvents(user.events.get(i));
                    }
                } else {
                    noEventsText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_manager);
        recyclerView = findViewById(R.id.manager_rec);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(groupieAdapter);
        fetchEvents();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        if(menuItem.getItemId() == R.id.home){
            Toast.makeText(this, "Home btn Clicked.", Toast.LENGTH_SHORT).show();
        }
        return true;
    }
    class EventItem extends Item<GroupieViewHolder>{
        Event event;
        public EventItem(Event event){
            this.event = event;
        }
        @Override
        public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
            viewHolder.itemView.setOnClickListener(v -> {

                Intent i = new Intent(v.getContext(), EventInfo.class);
                // send story title and contents through recyclerview to detail activity
                i.putExtra("event", (Serializable) event);
                v.getContext().startActivity(i);
            });
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            if(event ==null ||event.uid==null){
                return;
            }
            DatabaseReference ref = database.getReference("users/"+event.uid);
            // Attach a listener to read the data at our posts reference
            ref.addValueEventListener(new ValueEventListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    String  uri = event.photoUrl;
                    ImageView targetImageView = viewHolder.itemView.findViewById(R.id.cardImage);
                    ImageView targetAuthorImageView = viewHolder.itemView.findViewById(R.id.photo_event_new);
                    TextView targetTextView = viewHolder.itemView.findViewById(R.id.desc_card);
                    Picasso.get().load(uri).into(targetImageView);
                    assert user != null;
                    Picasso.get().load(user.url).into(targetAuthorImageView);
                    targetImageView.setColorFilter(Color.argb(155, 0, 0, 0), PorterDuff.Mode.SRC_ATOP);
                    TextView targetAuthor =viewHolder.itemView.findViewById(R.id.member_username);
                    targetAuthor.setText(user.firstName+" "+user.lastName);
                    targetTextView.setText(event.description);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }

            });
        }

        @Override
        public int getLayout() {
            return R.layout.event_custom;
        }
    }
}

