package com.example.qlique.EventsDisplay;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.qlique.Map.ShowEventMap;
import com.example.qlique.NewMessageActivity;
import com.example.qlique.Profile.User;
import com.example.qlique.R;
import com.example.qlique.chatLogActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import com.example.qlique.CreateEvent.Event;
import com.example.qlique.CreateEvent.EventMembers;

import java.io.Serializable;
import java.util.Objects;

public class EventInfo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_info_in_events_display);
        Intent i = getIntent();
        Event event = i.getParcelableExtra("event");
        ImageView membersInfo =findViewById(R.id.members_info);
        membersInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), EventMembers.class);
                i.putExtra("eventobj", (Serializable) event);
                v.getContext().startActivity(i);
            }
        });
        FirebaseDatabase.getInstance().getReference("/users/"+ event.uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                assert user != null;
                if(user.url!=null){
                   ImageView author_image =findViewById(R.id.user_profile_info);
                    Picasso.get().load(user.url).into(author_image);
                }
                TextView userName = findViewById(R.id.user_name_info);
                userName.setText(dataSnapshot.child("firstName").getValue().toString()+" "+dataSnapshot.child("lastName").getValue().toString());
                TextView desc = findViewById(R.id.description_post_info_events);
                desc.setText(event.description);
                TextView title = findViewById(R.id.title);
                title.setText(event.header);
                TextView date = findViewById(R.id.date);
                TextView hour = findViewById(R.id.hour);
                date.setText(event.date);
                hour.setText(event.hour);
                ImageView photo_info =findViewById(R.id.image_home_info);
                Picasso.get().load( event.photoUrl).into(photo_info);
                ImageView chat = findViewById(R.id.info_image_chat_btn);
                chat.setOnClickListener(v -> {
                    Intent intent = new Intent(v.getContext(), chatLogActivity.class);
                    intent.putExtra(NewMessageActivity.USER_KEY, user);
                    startActivity(intent);
                });
                ImageView mapImageView = findViewById(R.id.map_image_view);
                mapImageView.setOnClickListener(v -> {
                    Intent intent = new Intent(v.getContext(), ShowEventMap.class);
                    intent.putExtra("event", (Serializable) event);
                    startActivity(intent);
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });


    }

}