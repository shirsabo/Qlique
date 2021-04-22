package com.example.qlique.Map

import com.google.android.gms.maps.GoogleMap

class DisplayEventsMapActivity :BasicMapActivity() {
    override fun onMapReady(googleMap: GoogleMap) {
        super.onMapReady(googleMap)
        displayEventsNearby()
    }
    private fun displayEventsNearby(){
        // get the locations of the events nearby and add markers in their locations.
    }
}