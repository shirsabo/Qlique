package com.example.qlique.Map

import androidx.recyclerview.widget.LinearLayoutManager
import com.example.qlique.Event
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_main.*


class DisplayEventsMapActivity :BasicMapActivity() {
    private val DEFAULT_ZOOM = 15f

    override fun onMapReady(googleMap: GoogleMap) {
        super.onMapReady(googleMap)
        mMap?.setInfoWindowAdapter(CustomInfoWindowAdapter(this));
        fetchEvents()
    }
    private fun displayEventsNearby(events:ArrayList<Event>){
        // get the locations of the events nearby and add markers in their locations.

        for (event in events) {
            var latLng = LatLng(event.latitude, event.longitude)
            mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM))
            mMap?.animateCamera(CameraUpdateFactory.newLatLng(LatLng(latLng.latitude,latLng.longitude)))
            // Save the chosen location.
            chosenLat = latLng.latitude
            chosenLon = latLng.longitude
            val location = LatLng(latLng.latitude, latLng.longitude)
            mMap?.addMarker(MarkerOptions().position(location))
        }
    }
    private fun fetchEvents() {
        var events : ArrayList<Event> = ArrayList()
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
                //displayEventsNearby(events)
    }
}) }
}
