package com.example.qlique.RecommendationSystem

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.qlique.CreateEvent.CalendarEvent
import com.example.qlique.CreateEvent.Event
import com.example.qlique.Feed.MainActivity
import com.example.qlique.Feed.PostAdapter
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.util.*

class HobbiesRecommendationSystem(
    var activity: MainActivity,
    var feed: RecyclerView
) : RecommendationModel, GoogleApiClient.OnConnectionFailedListener {
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    private var mLocationPermissionsGranted = false
    var user: FirebaseUser? = null
    private val FINE_LOCATION: String = Manifest.permission.ACCESS_FINE_LOCATION
    private val COURSE_LOCATION: String = Manifest.permission.ACCESS_COARSE_LOCATION
    private val LOCATION_PERMISSION_REQUEST_CODE = 1234
    var events: ArrayList<Event> = ArrayList()
    val HobbiestoIndex = mapOf("Sport" to 0, "Ball Game" to 1, "Biking" to 2,  "Initiative" to 3,
        "Business" to 4, "Fashion" to 5, "Social" to 6,  "Entertainment" to 7,"Cooking" to 8,
        "Study" to 9, "Art" to 10,  "Beauty and style" to 11, "Comedy" to 12, "Food" to 13,
        "Animals" to 14,  "Talent" to 15, "Cars" to 16, "Love and dating" to 17,
        "Fitness and health" to 18,  "Dance" to 19,
        "Outdoor activities" to 20, "Home and garden" to 21,"Gaming" to 22)
    /**
     * Connection Failed.
     */
    override fun onConnectionFailed(p0: ConnectionResult) {
    }

    /**
    displays the events in the wanted activity according to location, hobbies and registered events.
     */
    override fun getRecommendedEvents() {
        events = ArrayList()
        getLocationPermission()
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        // displays the events in the wanted activity according to location, hobbies and registered events.
        requestFilteredEvents()
    }

    /**
     * getting the device's current location and fetching the events in the radius of this location.
     */
    private fun requestFilteredEvents() {
        // Get device location.
        Log.d(ContentValues.TAG, "getDeviceLocation: getting the devices current location")
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)
        try {
            mFusedLocationProviderClient!!.lastLocation
                .addOnSuccessListener { location: Location? ->
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        fetchNearbyEvents(location.latitude, location.longitude)
                    } else {
                        requestNewLocationData()
                    }
                }
        } catch (e: SecurityException) {
            Log.e(ContentValues.TAG, "getDeviceLocation: SecurityException: " + e.message)
        }
    }

    var mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
            // When we will receive the user's location this will be executed.
            fetchNearbyEvents(mLastLocation.latitude, mLastLocation.longitude)
        }
    }

    /**
     * requests location updates on the user's location in case the location we receives was null..
     */
    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)
        if (ActivityCompat.checkSelfPermission(
                activity.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                activity.applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // There are'nt permissions.
            return
        }
        mFusedLocationProviderClient!!.requestLocationUpdates(
            mLocationRequest, this.mLocationCallback,
            Looper.myLooper()
        )

    }

    /**
     *
     */
    private fun fetchEventFromFirebase(uid: String) {
        val ref = FirebaseDatabase.getInstance().getReference("/posts/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val event = snapshot.getValue(Event::class.java)
                event?.uid = snapshot.child("uid").value.toString()
                event?.description = snapshot.child("description").value.toString()
                event?.hour = snapshot.child("hour").value.toString()
                event?.date = snapshot.child("date").value.toString()
                event?.setEventUid(snapshot.key)
                if (event != null) {
                    if (CalendarEvent.isEventPassed(event.date, event.hour)) {
                        // show only future events
                        return
                    }
                    println("Added event")
                    // filter the events list according to the user's hobbies.
                    filterEvent()
                    /*********************************************************************/
                    events.add(event)
                }
                else{
                    return
                }
                // sets the PostAdapter which receives the array of event objects that were just fetched
                feed.adapter = PostAdapter(events)
            }

            override fun onCancelled(po: DatabaseError) {
            }
        })
    }
    private fun filterEvent() {
        TODO("Not yet implemented")
    }

    /**
     * adds events from a nearby radius.
     */
    private fun fetchNearbyEvents(lat: Double, long: Double) {
        val firebaseDatabase = FirebaseDatabase.getInstance()
        val ref: DatabaseReference = firebaseDatabase.getReference("geoFire")
        val geoFire = GeoFire(ref)
        val geoQuery: GeoQuery = geoFire.queryAtLocation(
            GeoLocation(lat, long),
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
                // fetches to uid of post_in_feed from firebase and adds it to list of events
                fetchEventFromFirebase(key)
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

    /**
     * getting location permissions.
     */
    private fun getLocationPermission() {
        Log.d(ContentValues.TAG, "getLocationPermission: getting location permissions")
        val permissions = arrayOf<String>(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (ContextCompat.checkSelfPermission(activity.applicationContext, FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            if (ContextCompat.checkSelfPermission(activity.applicationContext, COURSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                mLocationPermissionsGranted = true
            } else {
                ActivityCompat.requestPermissions(
                    activity,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        } else {
            ActivityCompat.requestPermissions(
                activity,
                permissions,
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

}