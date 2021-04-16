package com.example.qlique;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;

public class EventsManager extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    RecyclerView recyclerView;
    EventsManagerAdapter adapter;
    Event[] events={};
    void fetchEvents(){
        for (int i=0;i<10;i++){
            events = Arrays.copyOf(events, events.length+1);
            String curUser =  "FTNv4hPQYgMz4ScpvBhUasCjm6B3";
            String photo = "https://www.soltlv.com/wp-content/uploads/2019/12/Sol_tlv-Yoga_Sculpt.jpg";
            ArrayList<String> hobbies = new ArrayList<String>();
            hobbies.add("Soccer");
            events[events.length - 1] = new Event(photo,curUser,"Yoga tonight at 7:00 PM",hobbies);
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
