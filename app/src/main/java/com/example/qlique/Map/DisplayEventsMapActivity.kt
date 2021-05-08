package com.example.qlique.Map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.qlique.Event
import com.example.qlique.Profile.User
import com.example.qlique.R
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_chat_list.*


class DisplayEventsMapActivity :BasicMapActivity(), RequestRadiusDialog.OnCompleteListener {
    private var radius :Double = 0.0

    override fun onMapReady(googleMap: GoogleMap) {
        super.onMapReady(googleMap)
        mMap!!.setOnMarkerClickListener { marker ->
            Firebase.database.reference.child("posts").child(marker.tag.toString()!!)
                .addValueEventListener(object :
                    ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val event = dataSnapshot.getValue(Event::class.java)
                        if (event != null) {
                           val bottom_sheet_dialog : BottomSheetDialog =  BottomSheetDialog(this@DisplayEventsMapActivity,R.style.BottomSheetDialogTheme)
                            val bottom_sheet_view = LayoutInflater.from(this@DisplayEventsMapActivity).inflate(R.layout.layout_bottom_sheet_map,findViewById(R.id.bottomContainer))
                            bottom_sheet_dialog.setContentView(bottom_sheet_view)
                            bottom_sheet_dialog.show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        //Failed to read value
                    }
                })
            false
        }

        mMap?.setInfoWindowAdapter(CustomInfoWindowAdapter(this))
        requestRadiusFromUserAndDisplayEvents()

    }
    private fun requestRadiusFromUserAndDisplayEvents(){
        RequestRadiusDialog().show(supportFragmentManager, "MyCustomFragment")
    }
    private fun radiusButtonClicked(view: View?){
        requestRadiusFromUserAndDisplayEvents()
    }
    fun getBitmapDescriptorFromVector(context: Context, vectorDrawableResourceId: Int): BitmapDescriptor? {

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
               val marker = mMap?.addMarker(
                   MarkerOptions()
                       .position(LatLng(event.latitude, event.longitude))
                       .anchor(0.5f, 0.5f)
                       .title(event.header).icon(
                           getBitmapDescriptorFromVector(
                               applicationContext, R.drawable.ic_baseline_sports_soccer_24
                           )
                       )
               )
            if (marker != null) {
                marker.tag = uid
            }

            //mMap?.moveCamera(CameraUpdateFactory.newLatLng(LatLng(event.latitude, event.longitude)));
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



