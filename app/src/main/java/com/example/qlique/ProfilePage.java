package com.example.qlique;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.ktx.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.ktx.Firebase;

import java.util.Objects;

public class ProfilePage extends AppCompatActivity{
    private Button instagram;
    private TextView name, city, profileName, eventsNumber;
    private ListView hobbies;
    private User user;
    private Context context;
    ArrayAdapter<String> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);
        context = this;
        name = findViewById(R.id.name);
        city = findViewById(R.id.city);
        hobbies = findViewById(R.id.multiple_list_view);
        instagram =  findViewById(R.id.instagram);
        profileName =findViewById(R.id.profile_name);
        eventsNumber = findViewById(R.id.events_number);
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        String currentUser = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        mDatabase.child("users").child(currentUser).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    Log.d("firebase", String.valueOf(Objects.requireNonNull(task.getResult()).getValue()));
                    user = task.getResult().getValue(User.class);
                    assert user != null;
                    String nameCombine = user.firstName + " " + user.lastName;
                    name.setText(nameCombine);
                    city.setText(user.city);
                    adapter=new ArrayAdapter<String>(context,
                            android.R.layout.simple_list_item_1,
                            user.hobbies);
                    hobbies.setAdapter(adapter);
                    profileName.setText(nameCombine);
                    eventsNumber.setText("0");
                }
            }
        });
        instagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (user.instagramUserName == null || user.instagramUserName.length() == 0){
                    return;
                }
                Uri uri = Uri.parse("http://instagram.com/_u/" + user.instagramUserName);
                Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);
                likeIng.setPackage("com.instagram.android");
                try {
                    startActivity(likeIng);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://instagram.com/xxx")));
                }
            }
        });
    }


}