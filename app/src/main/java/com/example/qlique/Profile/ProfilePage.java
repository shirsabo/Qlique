package com.example.qlique.Profile;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.qlique.LoginAndSignUp.SignupActivity;
import com.example.qlique.NewMessageActivity;
import com.example.qlique.NewMessageActivityKt;
import com.example.qlique.Profile.User;
import com.example.qlique.R;
import com.example.qlique.chatLogActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class ProfilePage extends AppCompatActivity{
    private TextView name, city, profileName, eventsNumber, gender;
    private User user;
    private String userIdProfile;

    void setProfile(String uid){
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("users/"+uid);
        // Attach a listener to read the data at our posts reference
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                String  url = user.url;
                if(url==null){
                    return;
                }
                ImageView profile = findViewById(R.id.ProfileCircularImage);
                if(user.url != null && !user.url.equals("")) {
                    Picasso.get().load(user.url).into(profile);
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
        setContentView(R.layout.activity_profile_page);
        Context context = this;
        name = findViewById(R.id.name);
        city = findViewById(R.id.city);
        Button instagram = findViewById(R.id.instagram);
        profileName =findViewById(R.id.profile_name);
        eventsNumber = findViewById(R.id.events_number);
        gender = findViewById(R.id.gender);
        Button chat = findViewById(R.id.envelop);
        com.mikhaellopez.circularimageview.CircularImageView profilePic = findViewById(R.id.ProfileCircularImage);
        userIdProfile = getIntent().getStringExtra("EXTRA_SESSION_ID");
        setProfile(userIdProfile);
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        assert userIdProfile != null;
        if (!userIdProfile.equals(FirebaseAuth.getInstance().getUid())){
            chat.setVisibility(View.VISIBLE);
        }
        mDatabase.child("users").child(userIdProfile).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
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
                    profileName.setText(nameCombine);
                    String events_num;
                    if (user.events != null) {
                        events_num = "" + user.events.size();
                    } else {
                        events_num = "0";
                    }
                    eventsNumber.setText(events_num);
                    gender.setText(user.gender);
                }
            }

        });
        instagram.setOnClickListener(view -> {
            if (user.instagramUserName == null || user.instagramUserName.length() == 0){
                return;
            }
            String inst = "http://instagram.com/_u/" + user.instagramUserName;
            Uri uri = Uri.parse(inst);
            Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);
            likeIng.setPackage("com.instagram.android");
            try {
                startActivity(likeIng);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse(inst)));
            }
        });
        chat.setOnClickListener(view -> {
            if (userIdProfile.equals(FirebaseAuth.getInstance().getUid())){
                // We view our own profile.
                return;
            }
            // We view other user's profile so we will add him to our friends list and start
            // a conversation with him.
            openChatActivity(view);

        });
        Button back = findViewById(R.id.back_button);
        back.setOnClickListener(view -> finish());
    }

    public void openChatActivity(View view) {
        Intent intent = new Intent(this, chatLogActivity.class);
        intent.putExtra(NewMessageActivity.USER_KEY, user);
        this.startActivity(intent);
    }
}