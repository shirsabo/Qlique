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

import java.util.Arrays;

public class EventMembers extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    RecyclerView recyclerView;
    MembersAdapter adapter;
    String[] members={};
    void fetchMembers(Event event){
        TextView noMembersText = findViewById(R.id.no_members_text_view);
        if (event.members == null || event.members.size() == 0){
            noMembersText.setVisibility(View.VISIBLE);
            return;
        }
        for (int i=0;i<event.members.size();i++){
            noMembersText.setVisibility(View.GONE);
            members = Arrays.copyOf(members, members.length+1);
            members[i]= event.members.get(i);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_members);
        Intent i = getIntent();
        Event event = i.getParcelableExtra("eventobj");
        recyclerView = findViewById(R.id.members_rec);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        fetchMembers(event);
        adapter = new MembersAdapter(this,members); // our adapter takes two string array
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
