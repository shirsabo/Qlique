package com.example.qlique.Map

import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.qlique.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class CreateEventMapActivity : BasicMapActivity() {
    override fun onMapReady(googleMap: GoogleMap) {
        super.onMapReady(googleMap)
        mMap?.setOnMarkerClickListener { marker ->
            prevMarker?.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            //leave Marker default color if re-click current Marker
            if (marker != prevMarker) {
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                prevMarker = marker
            } else {
                clearMap()
            }
            false
        }
        mMap?.setOnMapClickListener { latlng -> // Clears the previously touched position
            clearMap()
            // Animating to the touched position
            mMap?.animateCamera(CameraUpdateFactory.newLatLng(latlng))
            // Save the chosen location.
            chosenLat = latlng.latitude
            chosenLon = latlng.longitude
            val location = LatLng(latlng.latitude,latlng.longitude)
            mMap?.addMarker(MarkerOptions().position(location))
        }
    }
     override fun moveCamera(latLng: LatLng, zoom: Float, title: String) {
        Log.d(
            ContentValues.TAG,
            "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude
        )
        mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
        if (title != "My Location") {
            val options: MarkerOptions = MarkerOptions()
                .position(latLng)
                .title(title)
            mMap!!.addMarker(options)
        }

        hideSoftKeyboard()
    }
}