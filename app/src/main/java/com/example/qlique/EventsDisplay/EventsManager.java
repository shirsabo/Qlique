package com.example.qlique.EventsDisplay;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.qlique.R;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Arrays;

import com.example.qlique.CreateEvent.Event;

public class EventsManager extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    RecyclerView recyclerView;
    EventsManagerAdapter adapter;
    Event[] events={};
    void fetchEvents(){
        for (int i=0;i<10;i++){
            events = Arrays.copyOf(events, events.length+1);
            String curUser =  "FTNv4hPQYgMz4ScpvBhUasCjm6B3";
            String photo = "https://miro.medium.com/max/11630/0*C5Y8W-6e9OVIB3AM";
            ArrayList<String> hobbies = new ArrayList<String>();
            hobbies.add("Soccer");
            Event event =  new Event(photo,curUser,"Basketball game tonight at 7:00 PM",hobbies);
            event.addMember("FTNv4hPQYgMz4ScpvBhUasCjm6B3");
            events[events.length - 1]=event;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_manager);
        recyclerView = findViewById(R.id.manager_rec);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        fetchEvents();
        adapter = new EventsManagerAdapter(this,events); // our adapter takes two string array
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        if(menuItem.getItemId() == R.id.home){
            Toast.makeText(this, "Home btn Clicked.", Toast.LENGTH_SHORT).show();
        }
        return true;
    }
}
