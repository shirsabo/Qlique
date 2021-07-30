package com.example.qlique.Map

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.qlique.CreateEvent.NewEvent
import com.example.qlique.R
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.Places.GEO_DATA_API
import com.google.android.gms.location.places.Places.PLACE_DETECTION_API
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import java.io.IOException
import java.util.*
import kotlin.properties.Delegates
/**
 * BasicMapActivity
 * This activity is responsible for fetching posts that are in a wanted radius and presenting them
 * in the map.
 **/
abstract class BasicMapActivity : AppCompatActivity() , OnMapReadyCallback , GoogleApiClient.OnConnectionFailedListener{
    private val FINE_LOCATION: String = Manifest.permission.ACCESS_FINE_LOCATION
    private val COURSE_LOCATION: String = Manifest.permission.ACCESS_COARSE_LOCATION
    private val LOCATION_PERMISSION_REQUEST_CODE = 1234
    private var mLocationPermissionsGranted = false
    protected var mMap: GoogleMap? = null
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    val DEFAULT_ZOOM = 15f
    private var mSearchText: AutocompleteSupportFragment? = null
    protected var mGps: ImageView? = null
    private lateinit var back: Button
    protected var prevMarker : Marker? = null
    private var mInfo : ImageView? = null
    private var mInfoTxt : TextView? = null
    private lateinit var mPlacesClient: PlacesClient
    private lateinit var mPlaceSearch: EditText
    protected var mGoogleApiClient: GoogleApiClient? = null
    private val LAT_LNG_BOUNDS: LatLngBounds = LatLngBounds(
        LatLng(-40.0, -168.0),
        LatLng(71.0, 136.0)
    )
    protected var chosenLat by Delegates.notNull<Double>()
    protected var chosenLon by Delegates.notNull<Double>()


    /**
     * when the map is ready we request the user location and checks the map has permissions.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        Log.d(TAG, "onMapReady: map is ready")
        mMap = googleMap
        if (mLocationPermissionsGranted) {
            getDeviceLocation()
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            mMap!!.isMyLocationEnabled = true;
            mMap!!.uiSettings.isMyLocationButtonEnabled = false;
            init()
        }
    }

    /**
     * initializes the buttons and text, finds the location the user typed in the search input.
     **/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        mGps = findViewById(R.id.ic_gps)
        mInfo = findViewById(R.id.ic_information)
        mInfoTxt = findViewById(R.id.info_text)
        getLocationPermission()
        back = findViewById(R.id.back_button)
        back.setOnClickListener {
            finish()
        }
        Places.initialize(this, resources.getString(R.string.google_maps_API_key))
        mPlacesClient = Places.createClient(this)
        mPlaceSearch = findViewById(R.id.input_search)
        mPlaceSearch.setOnEditorActionListener { _, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE
                || keyEvent.action == KeyEvent.ACTION_DOWN
                || keyEvent.action == KeyEvent.KEYCODE_ENTER
            ) {
                //execute our method for searching
                geoLocate()
            }
            false
        }
    }

    /**
     * counts 5 seconds with a timer and changes the visibility of the text view accordingly.
     */
    fun startTimeCounter(view: View) {
        val countTime: TextView = findViewById(R.id.info_text)
        object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                countTime.visibility = View.VISIBLE
            }
            override fun onFinish() {
                countTime.visibility = View.GONE
            }
        }.start()
    }

    /**
     * calls onActivityResult of Autocomplete Support Fragment.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mSearchText?.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * getting location permissions.
     */
    private fun getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions")
        val permissions = arrayOf<String>(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (ContextCompat.checkSelfPermission(this.applicationContext, FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.applicationContext, COURSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true
                initMap()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                permissions,
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    /**
     * if permission granted initializes the map.
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "onRequestPermissionsResult: called.")
        mLocationPermissionsGranted = false
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty()) {
                    var i = 0
                    while (i < grantResults.size) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false
                            Log.d(TAG, "onRequestPermissionsResult: permission failed")
                            return
                        }
                        i++
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted")
                    mLocationPermissionsGranted = true
                    //initialize our map
                    initMap()
                }
            }
        }
    }

    /**
     * getting the device's current location.
     */
    protected open fun getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the devices current location")
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        try {
            if (mLocationPermissionsGranted) {
                val location: Task<*> =
                    mFusedLocationProviderClient!!.lastLocation
                location.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "onComplete: found location!")
                        val currentLocation: Location? = task.result as Location?
                        if (currentLocation != null) {
                            moveCamera(
                                LatLng(
                                    currentLocation.latitude,
                                    currentLocation.longitude
                                ),
                                DEFAULT_ZOOM, "My Location"
                            )
                        }
                    } else {
                        Log.d(TAG, "onComplete: current location is null")
                        Toast.makeText(
                            this@BasicMapActivity,
                            "unable to get current location",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.message)
        }
    }

    /**
     * moving the camera to the latitude and longitude entered.
     */
    open fun moveCamera(latLng: LatLng, zoom: Float, title: String){
        Log.d(
            TAG,
            "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude
        )
        mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
        hideSoftKeyboard()
    }

    /**
     * hides Soft Keyboard.
     */
    protected fun hideSoftKeyboard() {
        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    /**
     * initializing the map.
     * being called after receiving permissions.
     */
    private fun initMap() {
        Log.d(TAG, "initMap: initializing map")
        val mapFragment : SupportMapFragment=
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this@BasicMapActivity)
    }

    /**
     * being called after the map is ready, creates google api client and sets the gps
     * on click listener.
     */
    open fun init() {
        Log.d(TAG, "init: initializing")
        mGoogleApiClient = GoogleApiClient.Builder(this)
            .addApi(GEO_DATA_API)
            .addApi(PLACE_DETECTION_API)
            .enableAutoManage(this, this)
            .build()
        mGps!!.setOnClickListener {
            Log.d(TAG, "onClick: clicked gps icon")
            getDeviceLocation()
        }
    }

    /**
    finds the location the user typed in the search input, saves the location and moves
    the map so the center will be in this location.
     **/
    private fun geoLocate() {
        clearMap()
        Log.d(TAG, "geoLocate: geolocating")
        val searchString = mPlaceSearch.text.toString()
        val geocoder = Geocoder(this@BasicMapActivity)
        var list: List<Address> = ArrayList()
        try {
            list = geocoder.getFromLocationName(searchString, 1)
        } catch (e: IOException) {
            Log.e(TAG, "geoLocate: IOException: " + e.message)
        }
        if (list.isNotEmpty()) {
            val address: Address = list[0]
            Log.d(TAG, "geoLocate: found a location: $address")
            // Save the chosen location.
            NewEvent.chosenLat = address.latitude
            NewEvent.chosenLon = address.longitude
            moveCamera(
                LatLng(
                    address.latitude,
                    address.longitude
                ), DEFAULT_ZOOM,
                address.getAddressLine(0)
            )
        }
    }

    /**
     * Connection Failed.
     */
    override fun onConnectionFailed(p0: ConnectionResult) {
    }

    /**
     * clears the previous marker that was clicked.
     */
    protected fun clearMap(){
        prevMarker = null
        mMap?.clear()
    }


}