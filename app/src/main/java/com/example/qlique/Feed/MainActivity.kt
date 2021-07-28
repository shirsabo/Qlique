package com.example.qlique.Feed

import com.example.qlique.CreateEvent.Event
import com.example.qlique.CreateEvent.NewEvent
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.qlique.Chat.ChatListActivity
import com.example.qlique.CreateEvent.CalendarEvent
import com.example.qlique.EventsDisplay.EventsManager
import com.example.qlique.LoginAndSignUp.LoginActivity
import com.example.qlique.LoginAndSignUp.UpdatePassword
import com.example.qlique.Map.DisplayEventsMapActivity
import com.example.qlique.Profile.ProfilePage
import com.example.qlique.R
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.nav_header_main.view.*
/**
 * Main activity
 * This activity responsible for fetching posts to feed, responsible for the side menu functionality,
 * configures the fcm token, google services etc(google map,account) and firebase.
 */
class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var navigationView: NavigationView
    private val ERROR_DIALOG_REQUEST = 9001
    private var floatingBtn: FloatingActionButton? =null
    /**
     * responsible for configurations (Title, Navigation menu,creating necessary instances,FCM)
     * fetches posts to feed from firebase , checks google services
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val lay: View = findViewById(R.id.app_bar_main_layout)
        val toolbar: Toolbar = lay.findViewById(R.id.toolbar_main)
        title = "Clique" // the title of the app
        setSupportActionBar(toolbar)
        drawer = findViewById(R.id.drawer_layout)
        toggle = ActionBarDrawerToggle(
            this,
            drawer,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer.addDrawerListener(toggle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        navigationView = findViewById(R.id.nav_view)
        auth = FirebaseAuth.getInstance() // get the current user instance
        if (auth.currentUser == null) { // if there is no instance then the user have to login again
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)//starts the activity
            finish()//finish main activity
        } else {
            Toast.makeText(this, "Already logged in", Toast.LENGTH_LONG).show()
        }
        //sets the listeners for each icon
        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.Profile -> profileClicked()
                R.id.Chat -> chatClicked()
                R.id.ChangePassword -> changePasswordClicked()
                R.id.EventsManager ->eventsManagerClicked()
                R.id.Logout -> logoutClicked()
                R.id.Map -> mapClicked()
            }
            return@setNavigationItemSelectedListener true
        }
        fetchPosts() //fetch the posts from firebase
        if(!isServicesOK()){ //checks if google service is OK
            Toast.makeText(this, "can't open map", Toast.LENGTH_LONG)
                .show()
        }
        //the floating button to upload event
        floatingBtn = findViewById(R.id.floating_action_button)
        //sets listener that opens new activity for creating an event
        floatingBtn!!.setOnClickListener{
            newEventClicked()
            fetchPosts()
        }
        updateFCM() //updates the fcm token of the user
    }
    /**
     * Checks if GooglePlay Services is Available ,checks to connection.
     * On error tries to fix it, if the error cant be fixed - showing the error to user.
     * @return true if service is Ok, else false
     */
    private fun isServicesOK(): Boolean {
        Log.d(TAG, "isServicesOK: checking google services version")
        val available =
            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this@MainActivity)
        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            return true
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occurred but we can resolve it
            val dialog: Dialog? = GoogleApiAvailability.getInstance()
                .getErrorDialog(this@MainActivity, available, ERROR_DIALOG_REQUEST)
            dialog?.show()
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show()
        }
        return false
    }
    /**
     * Updates the FCM token when main runs, so by that the sender will get the updated FCM
     * token and the receiver.
     */
    private fun updateFCM(){
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            val deviceToken = task.result
            val ref =FirebaseDatabase.getInstance().getReference("/users/${auth.currentUser?.uid+"/tokenFCM"}")
            ref.setValue(deviceToken)
            Log.d(TAG, "fcm token = $deviceToken")
        }
    }
    /**
     * Responsible for syncing the toggle (ActionBarDrawerToggle)
     */
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toggle.syncState()
    }
    /**
     * Updates the configuration of the toggle
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        toggle.onConfigurationChanged(newConfig)
    }
    /**
     * By [item] runs the function that matches the menu item
     * @return onSuccess- True, else False
     */
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    /**
     * Starts the [DisplayEventsMapActivity].
     */
    private fun mapClicked() {
        val intent = Intent(this, DisplayEventsMapActivity::class.java)
        startActivity(intent)
    }
    /**
     * Starts the [EventsManager].
     */
    private fun eventsManagerClicked() {
        val intent = Intent(this, EventsManager::class.java)
        startActivity(intent)
    }
    /**
     * When logout is clicked , signs out from [FirebaseAuth] and starts [LoginActivity]
     */
    private fun logoutClicked() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
    /**
     * When the icon for changing password is clicked, starts [UpdatePassword]
     */
    private fun changePasswordClicked() {
        val intent = Intent(this, UpdatePassword::class.java)
        startActivity(intent)
    }
    /**
     * When the icon for chat is clicked, starts [ChatListActivity]
     */
    private fun chatClicked() {
        val intent = Intent(this, ChatListActivity::class.java)
        startActivity(intent)
    }
    /**
     * When the icon for profile page is clicked, starts [ProfilePage]
     */
    private fun profileClicked() {
        val intent = Intent(this, ProfilePage::class.java)
        intent.putExtra("EXTRA_SESSION_ID", FirebaseAuth.getInstance().uid);
        startActivity(intent)
    }
    /**
     * When the icon for new event is clicked, starts [NewEvent]
     */
    private fun newEventClicked() {
        val intent = Intent(this, NewEvent::class.java)
        startActivity(intent)
    }
    /**
     * Fetches all the wanted posts from firebase DB
     *
     */
    private fun  fetchPosts(){
        val events : ArrayList<Event> = ArrayList()
        var mDatabase = FirebaseDatabase.getInstance().reference
        feed.layoutManager = LinearLayoutManager(this)
        mDatabase.child("/posts").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) { //gets all the posts from "/posts"
                    val event =
                        snapshot.getValue(Event::class.java)
                    //updates the event's fields
                    event?.uid = snapshot.child("uid").value.toString()
                    event?.description = snapshot.child("description").value.toString()
                    event?.hour = snapshot.child("hour").value.toString()
                    event?.date = snapshot.child("date").value.toString()
                    if (event != null) {
                       if(CalendarEvent.isEventPassed(event.date,event.hour)) {
                           // show only future events
                           continue;
                       }
                        event.setEventUid(snapshot.key)
                        events.add(event)
                    }
                }
                // sets the PostAdapter which receives the array of event objects that were just fetched
                feed.adapter= PostAdapter(events)
            }})
    }
}