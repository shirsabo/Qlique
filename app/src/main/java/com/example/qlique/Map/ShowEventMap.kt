package com.example.qlique.Map

import com.example.qlique.CreateEvent.Event
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.example.qlique.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog


class ShowEventMap : EventsMap() {
    override fun onMapReady(googleMap: GoogleMap) {
        super.onMapReady(googleMap)
        val event :Event= intent.getSerializableExtra("event") as Event
        // Move the map's center to be the event chosen.
        val latLng = CameraUpdateFactory.newLatLng(LatLng(event.latitude, event.longitude)) //= LatLng(event.latitude, event.longitude)
        val zoom = CameraUpdateFactory.zoomTo(DEFAULT_ZOOM)//: Float = DEFAULT_ZOOM
       // mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
        // Add a marker to the map.
        addMarkerToMap(event)
        mMap!!.moveCamera(latLng);
        mMap!!.animateCamera(zoom);
        // Hide the information image view.
        val imgView: ImageView = findViewById<View>(R.id.ic_information) as ImageView
        imgView.visibility = View.GONE
        // Display the information of the event.
        val bottomSheetDialogIn = BottomSheetDialog(
            this@ShowEventMap,
            R.style.BottomSheetDialogTheme
        )
        val bottomSheetView:View =
            LayoutInflater.from(this@ShowEventMap).inflate(
                R.layout.layout_bottom_sheet_map, findViewById(
                    R.id.bottomContainer
                )
            )
        updateViewOfBottomDialog(bottomSheetView,event)
        bottomSheetView.findViewById<TextView>(R.id.description_post_info_bottom).text =
            "hey"
        updateViewOfBottomDialog(bottomSheetView,event)
        bottomSheetDialogIn.setContentView(bottomSheetView)
        bottomSheetDialogIn.show()
    }
    private fun addMarkerToMap(event: Event){
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
        marker?.tag = event.uid

    }


}