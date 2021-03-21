package com.example.qlique;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class ProfilePage extends AppCompatActivity{
    private Button instagram;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);
        instagram =  findViewById(R.id.imp_instagram);
    }
    /*
     instagram.setOnClickListener{
            val uri = Uri.parse("http://instagram.com/_u/nikolbabai")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.instagram.android")
            try {
                startActivity(intent)
            } catch (e : ActivityNotFoundException){
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://instagram.com/_u/nikolbabai")))
            }
        }
     */
}