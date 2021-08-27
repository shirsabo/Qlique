package com.example.qlique.Map
import android.content.ContentValues
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Button
import com.example.qlique.CreateEvent.NewEvent
import com.example.qlique.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*


/**
 * CreateEventMapActivity
 * when creating a new event the user needs to select the location of the event in this map.
 */
class CreateEventMapActivity : BasicMapActivity() {
    private lateinit var back: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        if (!intent.getBooleanExtra("FirstTime", false)){
            super.finish()
        }

        super.onCreate(savedInstanceState)
        mPlaceSearch = findViewById(R.id.input_search)
        mPlaceSearch.setOnEditorActionListener { _, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE
                || keyEvent.action == KeyEvent.ACTION_DOWN
                || keyEvent.action == KeyEvent.KEYCODE_ENTER
            ) {
                // execute our method for searching
                geoLocate(true)
            }
            false
        }
    }

    /**
     * when the map is ready we request the user location and checks the map has permissions.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        super.onMapReady(googleMap)
        mMap?.setOnMarkerClickListener { marker ->
            prevMarker?.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            //leave Marker default color if re-click current Marker.
            if (marker != prevMarker) {
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                prevMarker = marker
            } else {
                clearMap(true)
            }
            false
        }
        mMap?.setOnMapClickListener { latlng -> // Clears the previously touched position
            clearMap(true)
            // Animating to the touched position
            mMap?.animateCamera(CameraUpdateFactory.newLatLng(latlng))
            // Save the chosen location.
            NewEvent.chosenLat = latlng.latitude
            NewEvent.chosenLon = latlng.longitude
            val location = LatLng(latlng.latitude, latlng.longitude)
            mMap?.addMarker(MarkerOptions().position(location))
            getAddress(latlng.latitude, latlng.longitude)

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

    private fun getAddress(latitude: Double, longitude: Double) {
        val addresses: List<Address>
        val geocoder: Geocoder = Geocoder(this, Locale.getDefault())
        addresses = geocoder.getFromLocation(
            latitude,
            longitude,
            1
        ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        val address = addresses[0].getAddressLine(0)
        if(address != null){
            NewEvent.addressChosen = address.toString()
        }
    }

    /**
     * moving the camera to the latitude and longitude entered and adds the marker
     * of the event chosen.
     */
    override fun moveCamera(latLng: LatLng) {
        Log.d(
            ContentValues.TAG,
            "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude
        )
        mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM))
        if (title != "My Location") {
            val options: MarkerOptions = MarkerOptions()
                .position(latLng)
            mMap!!.addMarker(options)
        }
        hideSoftKeyboard()
    }

    /**
     * calls onRestart of super and finish.
     */
    override fun onRestart() {
        super.onRestart()
        super.finish()
    }
    }



