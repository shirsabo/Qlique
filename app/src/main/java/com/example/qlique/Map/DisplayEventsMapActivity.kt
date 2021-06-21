package com.example.qlique.Map

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.qlique.CreateEvent.CalendarEvent
import com.example.qlique.CreateEvent.Event
import com.example.qlique.NewMessageActivity.Companion.USER_KEY
import com.example.qlique.Profile.User
import com.example.qlique.R
import com.example.qlique.chatLogActivity
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso


class DisplayEventsMapActivity :EventsMap(), RequestRadiusDialog.OnCompleteListener {
    private var radius :Double = 0.0

    override fun onMapReady(googleMap: GoogleMap) {
        super.onMapReady(googleMap)
        // Change the information presented in the basic map.
        chanceInfoText()
        mMap!!.setOnMarkerClickListener { marker ->
            Firebase.database.reference.child("posts").child(marker.tag.toString()!!)
                .addValueEventListener(object :
                    ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val event = dataSnapshot.getValue(Event::class.java)
                        if (event != null) {
                            event.uid = dataSnapshot.child("uid").value.toString()
                            event.description = dataSnapshot.child("description").value.toString()
                            val bottomSheetDialogIn: BottomSheetDialog = BottomSheetDialog(
                                this@DisplayEventsMapActivity,
                                R.style.BottomSheetDialogTheme
                            )
                            val bottom_sheet_view:View =
                                LayoutInflater.from(this@DisplayEventsMapActivity).inflate(
                                    R.layout.post_in_feed, findViewById(
                                        R.id.bottomContainer
                                    )
                                )
                            updateViewOfBottomDialog(bottom_sheet_view,event)
                            bottom_sheet_view.findViewById<TextView>(R.id.description_post).text =
                                "hey"
                            updateViewOfBottomDialog(bottom_sheet_view,event)
                            bottomSheetDialogIn.setContentView(bottom_sheet_view)
                            bottomSheetDialogIn.show()

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        //Failed to read value
                    }
                })
            false
        }
        // Request from the user the wanted radius and display all the events in that radius.
        requestRadiusFromUserAndDisplayEvents()
    }

    private fun loadUser(snapshot: DataSnapshot):User{
        val user :User = User()
        user.firstName = snapshot.child("firstName").value.toString()
        user.lastName = snapshot.child("lastName").value.toString()
        user.email  =  snapshot.child("email").value.toString()
        user.city =  snapshot.child("city").value.toString()
        user.gender =  snapshot.child("gender").value.toString()
        user.uid = snapshot.child("uid").value.toString()
        user. url = snapshot.child("url").value.toString()
        return user;
    }
    private fun addRadiusImageView() {
        val radiusImage: ImageView = findViewById(R.id.radiusImageView)
        radiusImage.visibility = View.VISIBLE
        radiusImage.setOnClickListener {
            radiusButtonClicked(it)
        }
    }
       private fun chanceInfoText(){
        val infoImageView: TextView = findViewById(R.id.info_text)
        infoImageView.text = "Move the map or search for a location where you want to see events"
    }
    /*
    Request Radius from the user with the dialog and display the events in this radius.
     */
    private fun requestRadiusFromUserAndDisplayEvents(){
        RequestRadiusDialog().show(supportFragmentManager, "MyCustomFragment")
    }
    /*
    Request Radius from the user with the dialog when clicking the radius button on the map.
     */
    private fun radiusButtonClicked(view: View){
        requestRadiusFromUserAndDisplayEvents()
    }

    private fun displayEventsNearby(events: ArrayList<Event>, uid: String) {
        // get the locations of the events nearby and add markers in their locations.
        for (event in events){
            val hobby = if (event.hobbiesRelated != null && event.hobbiesRelated.size > 0){
                event.hobbiesRelated[0]
            } else {
                ""
            }
            val marker = mMap?.addMarker(
                MarkerOptions()
                    .position(LatLng(event.latitude, event.longitude))
                    .anchor(0.5f, 0.5f)
                    .icon(
                        getBitmapDescriptorFromVector(
                            applicationContext, getImageByHobby(hobby)
                        )
                    )
            )
            marker?.tag = uid
        }
    }

    private fun addEventFromFirebase(uid: String) {
        val events: ArrayList<Event> = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("/posts/$uid")
         ref.addListenerForSingleValueEvent(object : ValueEventListener {
             override fun onDataChange(snapshot: DataSnapshot) {
                 val event = snapshot.getValue(Event::class.java)
                 event?.uid = snapshot.child("uid").value.toString()
                 event?.description = snapshot.child("description").value.toString()
                 if (event != null) {
                     if(CalendarEvent.isEventPassed(event.date,event.hour)) { // show only future events
                        return;
                     }
                     events.add(event)
                 }
                 event?.latitude = snapshot.child("latitude").getValue(Double::class.java)
                 event?.longitude = snapshot.child("longitude").getValue(Double::class.java)
                 displayEventsNearby(events, uid)
             }

             override fun onCancelled(po: DatabaseError) {
             }
         })

    }
    private fun fetchNearbyEvents(){

            val firebaseDatabase = FirebaseDatabase.getInstance()
            val ref: DatabaseReference = firebaseDatabase.getReference("geoFire")
            val geoFire = GeoFire(ref)
            val geoQuery: GeoQuery = geoFire.queryAtLocation(
                GeoLocation(32.0528, 34.8219),
                radius
            )
            geoQuery.addGeoQueryEventListener(object : GeoQueryEventListener {
                override fun onKeyEntered(key: String, location: GeoLocation) {
                    println(
                        String.format(
                            "Key %s entered the search area at [%f,%f]",
                            key,
                            location.latitude,
                            location.longitude
                        )
                    )
                    addEventFromFirebase(key) // fetches to uid of post_in_feed from firebase and adds it to list of events
                }


                override fun onKeyExited(key: String) {
                    println(String.format("Key %s is no longer in the search area", key))
                }

                override fun onKeyMoved(key: String, location: GeoLocation) {
                    println(
                        String.format(
                            "Key %s moved within the search area to [%f,%f]",
                            key,
                            location.latitude,
                            location.longitude
                        )
                    )

                }

                override fun onGeoQueryReady() {
                    println("All initial data has been loaded and events have been fired!")
                }

                override fun onGeoQueryError(error: DatabaseError) {
                    System.err.println("There was an error with this query: $error")
                }
            })
        }

    /*
    After the dialog fragment completes, it calls this callback.
     */
    override fun onComplete(r: String) {
        radius = r.toDouble()
        fetchNearbyEvents()
    }

}



