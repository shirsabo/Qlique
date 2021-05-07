package com.example.qlique.Map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.core.content.ContextCompat
import com.example.qlique.Event
import com.example.qlique.R
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.firebase.database.*


class DisplayEventsMapActivity :BasicMapActivity() {
    private val DEFAULT_ZOOM = 15f


    override fun onMapReady(googleMap: GoogleMap) {
        super.onMapReady(googleMap)
        mMap?.setInfoWindowAdapter(CustomInfoWindowAdapter(this));
        fetchNearbyEvents()
    }

    private fun displayEventsNearby(events: ArrayList<Event>) {

        // get the locations of the events nearby and add markers in their locations.
        for (event in events){
            mMap?.addMarker(
                MarkerOptions()
                    .position(LatLng(event.latitude, event.longitude))
                    .anchor(0.5f, 0.5f)
                    .title(event.header)?.icon(
                        getBitmapDescriptorFromVector(
                            applicationContext,R.drawable.ic_baseline_sports_soccer_24)
                    )
            )
            mMap?.moveCamera(CameraUpdateFactory.newLatLng(LatLng(event.latitude, event.longitude)));
        }
    }

    /* private fun createMarker(
         latitude: Double,
         longitude: Double,
         title: String?
         //,
         //snippet: String?,
         //iconResID: Int
     ): Marker? {
         return mMap?.addMarker(
             MarkerOptions()
                 .position(LatLng(latitude, longitude))
                 .anchor(0.5f, 0.5f)
                 .title(title)
                 //.snippet(snippet)
                 //.icon(BitmapDescriptorFactory.fromResource(iconResID))
         )
     } */
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
                displayEventsNearby(events)
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
            40.0
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


}



