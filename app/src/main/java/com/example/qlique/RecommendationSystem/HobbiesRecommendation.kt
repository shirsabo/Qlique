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
import com.example.qlique.Profile.User
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlin.collections.ArrayList



class HobbiesRecommendationSystem(
    var activity: MainActivity,
    var feed: RecyclerView
) : RecommendationModel, GoogleApiClient.OnConnectionFailedListener {
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    private var mLocationPermissionsGranted = false
    private val radius = 40.0
    private val fineLocation: String = Manifest.permission.ACCESS_FINE_LOCATION
    private val courseLocation: String = Manifest.permission.ACCESS_COARSE_LOCATION
    private val locationPermissionsRequestCode = 1234
    private var insertedEvents: ArrayList<Pair<Event, Double>> = ArrayList()
    private val hobbiesToIndex = mapOf(
        "Ball Game" to 0, "Sport" to 1, "Biking" to 2, "Outdoor activities" to 3, "Dance" to 4,
        "Fitness and health" to 5, "Food" to 6, "Cooking" to 7, "Study" to 8, "Gaming" to 9,
        "Cars" to 10, "Business" to 11, "Initiative" to 12, "Art" to 13, "Fashion" to 14,
        "Beauty and style" to 15, "Home and garden" to 16, "Animals" to 17, "Love and dating" to 18,
        "Social" to 19, "Entertainment" to 20, "Comedy" to 21, "Talent" to 22
    )

    /**
     * Connection Failed.
     */
    override fun onConnectionFailed(p0: ConnectionResult) {
    }

    /**
    displays the events in the wanted activity according to location, hobbies and registered events.
     */
    override fun getRecommendedEvents() {
        insertedEvents = ArrayList()
        getLocationPermission()
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        // displays the events in the wanted activity according to hobbies and registered events.
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
     * Requests location updates on the user's location in case the location we receives was null.
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
     * Fetch the event from the firebase according to the event id.
     */
    private fun fetchEventFromFirebase(eventId: String) {
        val ref = FirebaseDatabase.getInstance().getReference("/posts/$eventId")
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
                    // filter the events list according to the user's hobbies.
                    filterEvent(event)
                } else {
                    return
                }

            }

            override fun onCancelled(po: DatabaseError) {
            }
        })
    }

    /**
     * Checks if the event's hobbies are related to the user's hobbies and displays in the feed
     * only the events with the highest similarity.
     */
    private fun filterEvent(optionalEvent: Event?) {
        val ref = FirebaseDatabase.getInstance()
            .getReference("users/${FirebaseAuth.getInstance().currentUser?.uid}")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user: User? = snapshot.getValue<User>(
                    User::class.java
                )
                val userHobbies = user?.hobbies
                if (optionalEvent == null || (optionalEvent.hobbiesRelated.size == 0)) {
                    // Ignore.
                    return
                }
                if ((userHobbies == null || userHobbies.size == 0)) {
                    // If the user doesn't have any hobbies we will display all the events nearby.
                    addEventToEventsList(optionalEvent, 1.0)
                    return
                }
                // Check if the event's hobbies are related to the user's hobbies.
                val userHobbiesVec: BooleanArray = buildHobbiesVec(userHobbies)
                val optionalEventVec: BooleanArray = buildHobbiesVec(optionalEvent.hobbiesRelated)
                val hobbiesDiff = calcVectorsDistance(userHobbiesVec, optionalEventVec)
                val insertionProb = calcProbability(hobbiesDiff)
                if (insertionDecision(insertionProb)) {
                    addEventToEventsList(optionalEvent, insertionProb)
                } else {
                    println("event is not not inserted " + optionalEvent.eventUid)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    /**
     * Sets the PostAdapter which receives the array of event objects that were just fetched.
     */
    private fun addEventToEventsList(event: Event, prob: Double) {
        insertedEvents.add(Pair<Event, Double>(event, prob))
        // Sets the PostAdapter which receives the array of event objects that were just fetched.
        feed.adapter = PostAdapter(sortEventsByProb(insertedEvents))
    }

    /**
     * Sort the events by the probability so we can show to events with increased probability.
     */
    private fun sortEventsByProb(insertedEvents: ArrayList<Pair<Event, Double>>): ArrayList<Event> {
        // Sort the events by the probability.
        val sortedList = insertedEvents.sortedWith(compareBy { it.second }).reversed()
        // Add the sorted events to list.
        val events: ArrayList<Event> = ArrayList()
        for (event in sortedList) {
            // Show to events with increased probability.
            events.add(event.first)
        }
        return events
    }

    /**
     * Returns true if a ransom number has smaller value than the probability.
     */
    private fun insertionDecision(insertionProb: Double): Boolean {
        val randomNum: Int = (0..100).random()
        return randomNum.toDouble() < (insertionProb * 100)
    }

    /**
     * Calculate the probability according to the hobbies fidderence.
     */
    private fun calcProbability(hobbiesDiff: Int): Double {
        val hobbiesSize = hobbiesToIndex.size
        var insertionProb = 0.0
        if (hobbiesSize != 0) {
            insertionProb = (1 - (hobbiesDiff.toDouble() / hobbiesSize.toDouble()))
        }
        return insertionProb
    }

    /**
     * Calculate the distance between two vectors.
     */
    private fun calcVectorsDistance(vec1: BooleanArray, vec2: BooleanArray): Int {
        var minDiff = vec1.size
        for (i in vec1.indices) {
            for (j in vec1.indices) {
                val localDist = kotlin.math.abs(i - j)
                if ((vec1[i] && vec2[j]) && (minDiff > localDist)) {
                    minDiff = localDist
                }
            }
        }
        return minDiff
    }

    /**
     * Builds a vector according to the list of hobbies anf the hobbiesToIndex map.
     */
    private fun buildHobbiesVec(hobbies: List<String>): BooleanArray {
        val hobbiesVec = BooleanArray(hobbiesToIndex.keys.size) { false }
        for (hobby in hobbies) {
            if (hobbiesToIndex.containsKey(hobby)) {
                hobbiesVec[hobbiesToIndex[hobby]!!] = true
            }
        }
        return hobbiesVec
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
            radius
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
        if (ContextCompat.checkSelfPermission(activity.applicationContext, fineLocation) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            if (ContextCompat.checkSelfPermission(activity.applicationContext, courseLocation) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                mLocationPermissionsGranted = true
            } else {
                ActivityCompat.requestPermissions(
                    activity,
                    permissions,
                    locationPermissionsRequestCode
                )
            }
        } else {
            ActivityCompat.requestPermissions(
                activity,
                permissions,
                locationPermissionsRequestCode
            )
        }
    }

}