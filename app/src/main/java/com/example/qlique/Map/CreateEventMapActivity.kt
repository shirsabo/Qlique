package com.example.qlique.Map
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import CreateEvent.NewEvent
import com.example.qlique.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class CreateEventMapActivity : BasicMapActivity() {
    private lateinit var back: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        if (!intent.getBooleanExtra("FirstTime",false)){
            super.finish()
        }

        super.onCreate(savedInstanceState)
    }
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

                NewEvent.chosenLat = latlng.latitude
                NewEvent.chosenLon = latlng.longitude


            val location = LatLng(latlng.latitude, latlng.longitude)
            mMap?.addMarker(MarkerOptions().position(location))
        }
        back = findViewById(R.id.back_button)
        back.setOnClickListener {
            val intent = Intent()
            val coordinate = DoubleArray(2)
            coordinate[0] = NewEvent.chosenLon
            coordinate[1] = NewEvent.chosenLat
            intent.putExtra("coordinates", coordinate)
            setResult(RESULT_OK, intent)
            super.finish()
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
    override fun finish() {

        super.finish()

    }
    override fun onRestart() {
        super.onRestart()
        super.finish()
    }
    }



