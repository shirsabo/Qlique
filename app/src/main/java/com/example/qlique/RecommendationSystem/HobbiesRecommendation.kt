package com.example.qlique.RecommendationSystem

import android.app.Activity
import android.content.ContentValues
import android.location.Location
import android.util.Log
import com.example.qlique.CreateEvent.CalendarEvent
import com.example.qlique.CreateEvent.Event
import com.example.qlique.Feed.PostAdapter
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.util.*

class HobbiesRecommendationSystem(var activity: Activity, var feedAdapter: PostAdapter) :
    RecommendationModel {
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    private var mLocationPermissionsGranted = false
    var user: FirebaseUser? = null


    /**
    displays the events in the wanted activity according to location, hobbies and registered events.
     */
    override fun getRecommendedEvents() {
        // get the current user instance
        val auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!
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
            if (mLocationPermissionsGranted) {
                val location: Task<*> =
                    mFusedLocationProviderClient!!.lastLocation
                location.addOnCompleteListener(activity) { task ->
                    if (task.isSuccessful) {
                        Log.d(ContentValues.TAG, "onComplete: found location!")
                        val currentLocation: Location? = task.result as Location?
                        if (currentLocation != null) {
                            fetchNearbyEvents(currentLocation.latitude, currentLocation.longitude)
                        }
                    } else {
                        Log.d(ContentValues.TAG, "onComplete: current location is null")
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e(ContentValues.TAG, "getDeviceLocation: SecurityException: " + e.message)
        }
    }

    /**
     *
     */
    private fun fetchEventFromFirebase(uid: String) {
        val events: ArrayList<Event> = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("/posts/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val event = snapshot.getValue(Event::class.java)
                event?.uid = snapshot.child("uid").value.toString()
                event?.description = snapshot.child("description").value.toString()
                if (event != null) {
                    if (CalendarEvent.isEventPassed(event.date, event.hour)) {
                        // show only future events
                        return
                    }
                    events.add(event)
                }
                event?.latitude = snapshot.child("latitude").getValue(Double::class.java)
                event?.longitude = snapshot.child("longitude").getValue(Double::class.java)
                // filter the events list according to the user's hobbies.
                /*********************************************************************/
                // sets the PostAdapter which receives the array of event objects that were just fetched
                feedAdapter = PostAdapter(events)
            }

            override fun onCancelled(po: DatabaseError) {
            }
        })
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
}