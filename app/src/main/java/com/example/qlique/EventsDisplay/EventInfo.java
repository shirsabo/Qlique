package com.example.qlique.EventsDisplay;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.qlique.Map.ShowEventMap;
import com.example.qlique.NewMessageActivity;
import com.example.qlique.Profile.User;
import com.example.qlique.R;
import com.example.qlique.ChatLogActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.example.qlique.CreateEvent.Event;
import com.example.qlique.CreateEvent.EventMembers;
import org.jetbrains.annotations.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * EventInfo
 * This activity responsible for presenting the postInfo and adding the elements' functionality
 */
public class EventInfo extends AppCompatActivity {
    @Override
    /**
     * Sets content, event listeners of elements in the posts, loads the data from firebase
     * @param savedInstanceState the parameter that passed to super.onCreate.
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_info_in_events_display);
        Intent i = getIntent();
        Event event = i.getParcelableExtra("event");
        ImageView membersInfo =findViewById(R.id.members_info);
        //when member icon is clicked the EventMembers activity starts.
        membersInfo.setOnClickListener(v -> {
            Intent i1 = new Intent(v.getContext(), EventMembers.class);
            i1.putExtra("eventobj", (Serializable) event);//passes the event obj to the new intent
            v.getContext().startActivity(i1);
        });
        String curUser = FirebaseAuth.getInstance().getUid();//get the uid of current user
        FirebaseDatabase.getInstance().getReference("/users/"+ event.uid).addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            /**
             * Sets content, event listeners of elements in the posts, loads the data from firebase
             * @param dataSnapshot from this object we get the data of the user.
             */
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                assert user != null;
                if(user.url!=null){
                   ImageView author_image =findViewById(R.id.user_profile_info);
                    Picasso.get().load(user.url).into(author_image);//loads profile picture
                }
                TextView userName = findViewById(R.id.user_name_info);
                userName.setText(Objects.requireNonNull(dataSnapshot.child("firstName").getValue()).toString()+" "+ Objects.requireNonNull(dataSnapshot.child("lastName").getValue()).toString());
                TextView desc = findViewById(R.id.description_post_info_events); //sets description
                desc.setText(event.description);
                TextView title = findViewById(R.id.title);
                title.setText(event.header);//sets title of the post
                TextView date = findViewById(R.id.date);
                TextView hour = findViewById(R.id.hour);
                //sets date and hour of the post
                date.setText(event.date);
                hour.setText(event.hour);
                //sets and loads the image of the postt
                ImageView photo_info =findViewById(R.id.image_home_info);
                Picasso.get().load( event.photoUrl).into(photo_info);
                ImageView chat = findViewById(R.id.info_image_chat_btn);
                if(checkIfAuthorIsCUrUser(user.uid,curUser)) {
                    chat.setVisibility(View.GONE);// user can not send to himself a message
                }
                else{
                    //when the chat button is clicked the chat activity opens.
                    chat.setOnClickListener(v -> {
                        Intent intent = new Intent(v.getContext(), ChatLogActivity.class);
                        // passing the user_key to the intent
                        intent.putExtra(NewMessageActivity.USER_KEY, user);
                        startActivity(intent);
                    });
                }
                ImageView mapImageView = findViewById(R.id.map_image_view); //gets the map icon ImageView
                mapImageView.setOnClickListener(v -> {
                    Intent intent = new Intent(v.getContext(), ShowEventMap.class);
                    // passing the event obj to the intent
                    intent.putExtra("event", (Serializable) event);
                    startActivity(intent);
                });
            }
            /**
             * Checks if both author and current user are equal.
             * @param author - the uid of the author
             * @param curUserIn - the current logged user's uid.
             */
            private boolean checkIfAuthorIsCUrUser(String author, String curUserIn){
              return author.equals(curUserIn);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });


    }

}