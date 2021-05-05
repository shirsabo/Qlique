package com.example.qlique.Map

import com.example.qlique.Event
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class DisplayEventsMapActivity :BasicMapActivity() {
    private val DEFAULT_ZOOM = 15f

    override fun onMapReady(googleMap: GoogleMap) {
        super.onMapReady(googleMap)
        mMap?.setInfoWindowAdapter(CustomInfoWindowAdapter(this));
        fetchEvents()
    }

    private fun displayEventsNearby(events: ArrayList<Event>) {
        // get the locations of the events nearby and add markers in their locations.
        for (event in events){
                mMap?.addMarker(
                    MarkerOptions()
                        .position(LatLng(event.latitude, event.longitude))
                        .anchor(0.5f, 0.5f)
                        .title(event.header)
            )
            //mMap?.moveCamera(CameraUpdateFactory.newLatLng(LatLng(event.latitude, event.longitude)));
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

    private fun fetchEvents() {
        var events: ArrayList<Event> = ArrayList()
        var mDatabase = FirebaseDatabase.getInstance().reference
        mDatabase.child("/posts").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val event = snapshot.getValue(Event::class.java)
                    event?.uid = snapshot.child("uid").value.toString()
                    event?.description = snapshot.child("description").value.toString()
                    if (event != null) {
                        events.add(event)
                    }
                    event?.latitude = snapshot.child("latitude").getValue(Double::class.java)
                    event?.longitude = snapshot.child("longitude").getValue(Double::class.java)
                }
                displayEventsNearby(events)
            }
        })
    }

}
