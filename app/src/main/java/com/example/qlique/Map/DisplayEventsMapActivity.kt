package com.example.qlique.Map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.example.qlique.R
import androidx.core.content.ContextCompat
import com.example.qlique.Event
import com.example.qlique.Profile.User
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
import kotlinx.android.synthetic.main.layout_bottom_sheet_map.*


class DisplayEventsMapActivity :BasicMapActivity(), RequestRadiusDialog.OnCompleteListener {
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
                            event?.uid = dataSnapshot.child("uid").value.toString()
                            event?.description = dataSnapshot.child("description").value.toString()
                            val bottomSheetDialogIn: BottomSheetDialog = BottomSheetDialog(
                                this@DisplayEventsMapActivity,
                                R.style.BottomSheetDialogTheme
                            )
                            val bottom_sheet_view:View =
                                LayoutInflater.from(this@DisplayEventsMapActivity).inflate(
                                    R.layout.layout_bottom_sheet_map, findViewById(
                                        R.id.bottomContainer
                                    )
                                )
                            updateViewOfBottomDialog(bottom_sheet_view,event)
                            bottom_sheet_view.findViewById<TextView>(R.id.description_post_info_bottom).setText("hey")
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
        //addRadiusImageView()
    }
    private fun updateViewOfBottomDialog(view: View,event: Event){
        val textView =view.findViewById<View>(R.id.description_post_info_bottom) as TextView
        textView.text = event.description
        val view1:ImageView = view.findViewById(R.id.image_home_info_bottom)
        if(event.photoUrl!=null){

            Picasso.get().load(event.photoUrl).into(view1)
            //Picasso.get().load(event.photoUrl).into( view.findViewById<ImageView>(R.id.user_profile_info_bottom))

        }
        updateAuthor(view, event.uid)
    }
    private fun updateAuthor(view:View ,uid: String){
        FirebaseDatabase.getInstance().getReference("/users/$uid")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val user = dataSnapshot.getValue(
                        User::class.java
                    )
                    if (user!!.url != null) {
                        val authorImage = view.findViewById<ImageView>(R.id.user_profile_info_bottom)
                        Picasso.get().load(user.url).into(authorImage)
                    }
                    val userName = view.findViewById<TextView>(R.id.user_name_info_bottom)
                    userName.text =
                        dataSnapshot.child("firstName").value.toString() + " " + dataSnapshot.child(
                            "lastName"
                        ).value.toString()
                }

                override fun onCancelled(error: DatabaseError) {}
            })

    }
    private fun addRadiusImageView() {
        val radiusImage: ImageView = findViewById(R.id.radiusImageView)
        radiusImage.visibility = View.VISIBLE
        radiusImage.setOnClickListener {
            radiusButtonClicked(it)
        }
        /*
        val view = LinearLayout(this)
        setContentView(view)
        val radiusImage = ImageView(this)
        radiusImage.setImageResource(R.drawable.ic_radius)
        val width = 40
        val height = 40
        val params = LinearLayout.LayoutParams(width, height)
        radiusImage.layoutParams = params
        view.addView(radiusImage)*/
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
    private fun getBitmapDescriptorFromVector(context: Context, vectorDrawableResourceId: Int): BitmapDescriptor? {

        val vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId)
        val bitmap = Bitmap.createBitmap(
            vectorDrawable!!.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
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

    private fun getImageByHobby(hobby: String): Int {
        /*
        "Sport", "Initiative", "Business", "Fashion", "Social",
        "Entertainment", "Study", "Beauty and style", "Comedy", "Food", "Animals",
        "Talent", "Cars", "Love and dating", "Fitness and health",
        "Dance", "Outdoor activities", "Home and garden", "Gaming"
         */
        if(hobby == "Ball Games"){
            return R.drawable.ic_baseline_sports_soccer_24
        }
        if (hobby == "Sport"){
            return  R.drawable.ic_sport
        } else if (hobby == "Initiative"){
            return R.drawable.ic_light_bulb
        }else if (hobby == "Business"){
            return R.drawable.ic_buisnessicon
        }else if (hobby == "Fashion"){
            return R.drawable.ic_fashion
        }else if (hobby == "Social"){
            return R.drawable.ic_friends
        }else if (hobby == "Entertainment"){
            return R.drawable.ic_movies
        }else if (hobby == "Study"){
            return R.drawable.ic_studying
        }else if (hobby == "Beauty and style"){
            return R.drawable.ic_eye_treatment
        }else if (hobby == "Comedy"){
            return R.drawable.ic_lol
        }else if (hobby == "Food"){
            return R.drawable.ic_spaguetti
        }else if (hobby == "Animals"){
            return R.drawable.ic_pets
        }else if (hobby == "Talent"){
            return R.drawable.ic_talent
        }else if (hobby == "Cars"){
            return R.drawable.ic_cars1
        }else if (hobby == "Love and dating"){
            return R.drawable.ic_hearts
        }else if (hobby == "Fitness and health"){
            return R.drawable.ic_meditation
        }else if (hobby == "Dance"){
            return R.drawable.ic_dancing
        }else if (hobby == "Outdoor activities"){
            return R.drawable.ic_sport
        }else if (hobby == "Home and garden"){
            return R.drawable.ic_plant_pot
        }else if (hobby == "Gaming"){
            return R.drawable.ic_joystick
        } else {
            return R.drawable.ic_location
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
                    addEventFromFirebase(key) // fetches to uid of post from firebase and adds it to list of events
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



