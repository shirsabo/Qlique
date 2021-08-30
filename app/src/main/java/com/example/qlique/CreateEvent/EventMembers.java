package com.example.qlique.CreateEvent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.qlique.R;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
/**
 * Class EventMembers.
 * This activity is responsible for presenting the members of an event and adding functionality to the elements.
 */
public class EventMembers extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    RecyclerView recyclerView;
    MembersAdapter adapter; // holds all the member items
    String[] members={}; // holds the members' unique ids

  void addMmberIfExists(Integer indexEventMem, Event event ){
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("users");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.child(event.members.get(indexEventMem)).getValue()!=null) {
                    members = Arrays.copyOf(members, members.length+1);
                    members[members.length-1]= event.members.get(indexEventMem);
                    adapter = new MembersAdapter(EventMembers.this,members); // our adapter takes two string array
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed, how to handle?

            }

        });
    }
    /**
     * Fetches the members of the event from the Event object
     * @param event - the event
     */
    void fetchMembers(Event event){
        TextView noMembersText = findViewById(R.id.no_members_text_view);
        if (event.members == null || event.members.size() == 0){
            // if there are no members , shows there are no members to the user
            noMembersText.setVisibility(View.VISIBLE);
            return;
        }
        for (int i=0, insertedMembers=0;i<event.members.size();i++){
            noMembersText.setVisibility(View.GONE);
            addMmberIfExists(i,event);
        }
    }

    @Override
    /**
     * Configures the recyclerView, sets contentView, fetches the members of the event.
     * @param savedInstanceState - param fir the super.onCreate
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_members);
        Intent i = getIntent();
        Event event = i.getParcelableExtra("eventobj");//gets the event passed to this intent
        recyclerView = findViewById(R.id.members_rec);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        assert event != null;
        fetchMembers(event);
    }

    @Override
    /**
     * Called when Navigation item selected
     * @param MenuItem menuItem
     */
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        if(menuItem.getItemId() == R.id.home){
            Toast.makeText(this, "Home btn Clicked.", Toast.LENGTH_SHORT).show();
        }
        return true;
    }
}
