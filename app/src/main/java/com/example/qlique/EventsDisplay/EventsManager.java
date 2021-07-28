package com.example.qlique.EventsDisplay;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.qlique.CreateEvent.CalendarEvent;
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
import java.util.Objects;

import com.example.qlique.CreateEvent.Event;

import org.jetbrains.annotations.NotNull;
/**
 * Class EventsManage.
 * Responsible for showing to the user his/hers future events.
 */
public class EventsManager extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    RecyclerView recyclerView; //holds all the future events
    private GroupAdapter<GroupieViewHolder> groupieAdapter =
            new GroupAdapter<com.xwray.groupie.GroupieViewHolder>();
    ArrayList eventsLists = new ArrayList();
    /**
     * Adds the event item to the recyclesView's adapter and fetches the needed data from Firebase.
     * @param eventIn the event's uid to be added to the adapter.
     */
    private void addEvents(String eventIn){
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("posts/"+eventIn);
        if(eventsLists.contains(eventIn)){ // if it already there- return
            return;
        }
        // Attach a listener to read the data at our posts reference
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                Event event = dataSnapshot.getValue(Event.class);
                if(event==null) {
                    exitEventFromCurUser(eventIn);//remove this event from user's event
                    recreate();
                    return;
                }
                //update event's data from dataSnapshot
                event.uid = Objects.requireNonNull(dataSnapshot.child("uid").getValue()).toString();
                event.description = Objects.requireNonNull(dataSnapshot.child("description").getValue()).toString();
                event.date = Objects.requireNonNull(dataSnapshot.child("date").getValue()).toString();
                event.header = Objects.requireNonNull(dataSnapshot.child("header").getValue()).toString();
                event.photoUrl = Objects.requireNonNull(dataSnapshot.child("photoUrl").getValue()).toString();
                if(eventsLists.contains(eventIn)){ //checks if the user is signd
                    return;
                }
                if(!CalendarEvent.isEventPassed(event.date,event.hour)){
                    groupieAdapter.add(new EventItem(event));//adds the event item to the adapter
                    eventsLists.add(eventIn);//adds the event to the list
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    /**
     * Fetches the event to the recyclerView.
     */
    private void fetchEvents(){
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("users/"+ Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        // Attach a listener to read the data at our posts reference
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                TextView noEventsText = findViewById(R.id.no_events_text_view);
                User user = dataSnapshot.getValue(User.class);
                if (user != null && user.events != null) {
                    noEventsText.setVisibility(View.GONE);
                    int n = user.events.size();
                    for (int i = 0; i < n; i++) {
                        addEvents(user.events.get(i)); //adds all the events of the user to the adapter
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
    /**
     * Configures the recyclerView and calling to the super.onCreate function with savedInstanceState
     * @param savedInstanceState the Bundle which passes to the super.onCreate.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //configurations
        setContentView(R.layout.activity_events_manager);
        recyclerView = findViewById(R.id.manager_rec);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(groupieAdapter);
        fetchEvents();//gets all the events to the recyclerView
    }
    /**
     * responsible for home button
     * @param menuItem - MenuItem
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        if(menuItem.getItemId() == R.id.home){
            Toast.makeText(this, "Home btn Clicked.", Toast.LENGTH_SHORT).show();
        }
        return true;
    }
    /**
     * Class EventItem.
     * The class that represents the future event in the recyclerView.
     */
    class EventItem extends Item<GroupieViewHolder>{
        Event event; // the represented event
        /**
         * Responsible for opening leave dialog.
         * @param viewHolder - @NonNull GroupieViewHolder in order to get context.
         */
        private void openLeaveDialog(@NonNull GroupieViewHolder viewHolder){
            AlertDialog.Builder builder = new AlertDialog.Builder(viewHolder.itemView.getContext());
            LayoutInflater layoutInflaterAndroid = LayoutInflater.from(viewHolder.itemView.getContext());
            View view = layoutInflaterAndroid.inflate(R.layout.leave_dialog, null);
            builder.setView(view);
            builder.setCancelable(false);
            final AlertDialog alertDialog = builder.create();
            alertDialog.show(); // shows the dialog to user
            //when leave button is pressed the user exits from the event and it updates the DB.
            view.findViewById(R.id.leave_btn).setOnClickListener(v -> {
                handleExitEvent(event); //update the DB about the exit.
                alertDialog.cancel(); //closes the dialog to the user
            });
            //when the user decides not to exit the event it closes the dialog.
            view.findViewById(R.id.cancle_leave_btn).setOnClickListener(v -> {
                alertDialog.cancel();
            });
        }
        /**
         * Constructor.
         * @param event - the represented Event object.
         */
        public EventItem(Event event){
            this.event = event;
        }
        @Override
        /**
         * Binds betweeb the position to the actual represented data fetched from firebase.
         * @param viewHolder - GroupieViewHolder, in order to get the wanted itemView.
         * @param position - the position in the adapter in order to get the specified event object.
         */
        public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
            viewHolder.itemView.setOnClickListener(v -> {
                Intent i = new Intent(v.getContext(), EventInfo.class);
                // send story title and contents through recyclerview to detail activity
                i.putExtra("event", (Serializable) event); // passes the event object to EventInfo
                v.getContext().startActivity(i);
            }); //sets event listener whe the leave_btn_custom is pressed
            viewHolder.itemView.findViewById(R.id.leave_btn_custom).setOnClickListener(v -> {
                openLeaveDialog(viewHolder);
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
                /**
                 * loads the data of the event from firebase.
                 * @param dataSnapshot- with the DataSnapshot we can get the data return from the Async request.
                 */
                public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    if(user==null){
                        return;
                    }
                    String  uri = event.photoUrl; // the photo path in firebase.
                    //loads all the needed images from firebase
                    ImageView targetImageView = viewHolder.itemView.findViewById(R.id.cardImage);
                    ImageView targetAuthorImageView = viewHolder.itemView.findViewById(R.id.photo_event_new);
                    TextView targetTextView = viewHolder.itemView.findViewById(R.id.desc_card);
                    Picasso.get().load(uri).into(targetImageView);
                    Picasso.get().load(user.url).into(targetAuthorImageView);
                    targetImageView.setColorFilter(Color.argb(155, 0, 0, 0), PorterDuff.Mode.SRC_ATOP);
                    TextView targetAuthor =viewHolder.itemView.findViewById(R.id.member_username);
                    targetAuthor.setText(user.firstName+" "+user.lastName); //sets the name of the author
                    if(event.description!=null){
                        targetTextView.setText(event.header); //sets the event's description
                    }
                    if(event.date!=null){
                        TextView date = viewHolder.itemView.findViewById(R.id.date);
                        date.setText(event.date); //sets the event's date
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
        /**
         *returns the layout.
         */
        @Override
        public int getLayout() {
            return R.layout.event_custom_in_events_display;
        }
    }
    /**
     *If admin deleted event than the event is totally deleted from firebase.
     */
    private void handleDeleteEventByAdmin(String eventUid) {
        exitEventFromCurUser(eventUid);
        deleteEvent(eventUid);
    }
    /**
     *If admin deleted event than the event is totally deleted from firebase.
     * @param eventUid - the id of the event to be deleted.
     */
    private void deleteEvent(String eventUid) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference refPost = database.getReference("posts/"); // deletes from all posts
        refPost.child(eventUid).removeValue();
        DatabaseReference refGPSQuery = database.getReference("geoFire/");//deleted from geoFire
        refGPSQuery.child(eventUid).removeValue();
    }
    /**
     *Exists from event.
     * @param event, the event object to exit from
     */
    private void handleExitEvent(Event event) {
        if(event!=null){
            //delete eventUid from events in current user's events.
            if(event.uid.equals(FirebaseAuth.getInstance().getUid())){ // Admin deletes event.
                handleDeleteEventByAdmin(event.eventUid); //deletes user from the event.
                recreate(); //refresh the adapter
            }else{
                String eventUid = event.eventUid;
                exitEventFromCurUser(event.eventUid);
                deleteUserFromEventsMembers(eventUid);
            }
        }
    }

    private void exitEventFromCurUser(String eventUid) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("users/"+ Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        // Attach a listener to read the data at our posts reference
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                assert user != null;
                //update user's fields
                user.uid = Objects.requireNonNull(dataSnapshot.child("uid").getValue()).toString();
                user.url= Objects.requireNonNull(dataSnapshot.child("url").getValue()).toString();
                user.lastName= Objects.requireNonNull(dataSnapshot.child("lastName").getValue()).toString();
                if(eventUid==null){
                    return;
                }
                if(user.events==null){
                    return;
                }
                user.events.remove(eventUid);//remove event's uid
                ref.setValue(user);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    private void deleteUserFromEventsMembers(String eventUid){
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("posts/"+eventUid);
        // Attach a listener to read the data at our posts reference
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                Event event = dataSnapshot.getValue(Event.class);
                if(event==null){
                    exitEventFromCurUser(eventUid);
                    return;
                }
                //updates user's fields.
                event.uid = Objects.requireNonNull(dataSnapshot.child("uid").getValue()).toString();
                event.photoUrl= Objects.requireNonNull(dataSnapshot.child("photoUrl").getValue()).toString();
                event.description= Objects.requireNonNull(dataSnapshot.child("description").getValue()).toString();
                if(event.members!=null){
                    event.members.remove(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());//remove event's uid
                    ref.setValue(event);//update in firebase that the user is no longer in the event
                    recreate();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}

