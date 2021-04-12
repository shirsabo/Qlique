package com.example.qlique
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.*
import kotlin.collections.ArrayList

class NewEvent : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_event)
        createEvent()
    }
    private fun createEvent(){
        var curUser =  FirebaseAuth.getInstance().currentUser.uid
        var mDatabase = FirebaseDatabase.getInstance().reference
        var photo = "https://the18.com/sites/default/files/styles/feature_image_with_focal/public/feature-images/20200313-The18-Image-Coronavirus-Soccer.jpg?itok=nHsVIAhh"
        var hobbies = ArrayList<String>(2)
        hobbies.add("Soccer")
        var event:Event =Event(photo,curUser,"hey",hobbies)
        mDatabase.child("/posts").push().setValue(event)
    }
}
