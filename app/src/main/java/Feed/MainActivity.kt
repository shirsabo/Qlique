package Feed

import CreateEvent.Event
import CreateEvent.NewEvent
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.qlique.Chat.ChatListActivity
import com.example.qlique.EventsDisplay.EventsManager
import com.example.qlique.LoginAndSignUp.LoginActivity
import com.example.qlique.LoginAndSignUp.UpdatePassword
import com.example.qlique.Map.DisplayEventsMapActivity
//import com.example.qlique.Map.DisplayEventsMapActivity
import com.example.qlique.Profile.ProfilePage
import com.example.qlique.Profile.User
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
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.nav_header_main.view.*


class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var navigationView: NavigationView
    private lateinit var  profile:ImageView
    private val ERROR_DIALOG_REQUEST = 9001
    private var floatingBtn: FloatingActionButton? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val lay: View = findViewById(R.id.app_bar_main_layout)
        val toolbar: Toolbar = lay.findViewById(R.id.toolbar_main)
        val profilePic = lay.nav_ProfileCircularImage
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


        auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Already logged in", Toast.LENGTH_LONG).show()
        }

        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.Profile -> profileClicked()
                R.id.Chat -> chatClicked()
                //R.id.nav_item_three -> Toast.makeText(this, "Clicked item three", Toast.LENGTH_SHORT)
                //  .show()
                R.id.ChangePassword -> changePasswordClicked()
                R.id.EventsManager ->eventsManagerClicked()
                R.id.Logout -> logoutClicked()
                R.id.Map -> mapClicked()

            }

            return@setNavigationItemSelectedListener true
        }
        /*
        val events : ArrayList<Event> = ArrayList()
        for(i in 0..100){
            events.add(Event())
        }

        feed.layoutManager = LinearLayoutManager(this)
        feed.adapter= postAdapter(events)
         */
        fetchPosts()
        if(!isServicesOK()){
            Toast.makeText(this, "can't open map", Toast.LENGTH_LONG)
                .show()
        }
        floatingBtn = findViewById(R.id.floating_action_button)
        floatingBtn!!.setOnClickListener{
            newEventClicked()
            fetchPosts()
        }

    }


    private fun isServicesOK(): Boolean {
        Log.d(TAG, "isServicesOK: checking google services version")
        val available =
            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this@MainActivity)
        if (available == ConnectionResult.SUCCESS) {
            //everything is fine and the user can make map requests
            return true
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            //an error occured but we can resolve it
            val dialog: Dialog? = GoogleApiAvailability.getInstance()
                .getErrorDialog(this@MainActivity, available, ERROR_DIALOG_REQUEST)
            dialog?.show()
        } else {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show()
        }
        return false
    }


    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        toggle.onConfigurationChanged(newConfig)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun mapClicked() {
        val intent = Intent(this, DisplayEventsMapActivity::class.java)
        startActivity(intent)
    }
    private fun eventsManagerClicked() {
        val intent = Intent(this, EventsManager::class.java)
        startActivity(intent)
    }

    private fun logoutClicked() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun changePasswordClicked() {
        val intent = Intent(this, UpdatePassword::class.java)
        startActivity(intent)
    }

    private fun chatClicked() {
        val intent = Intent(this, ChatListActivity::class.java)
        startActivity(intent)
    }

    private fun profileClicked() {
        val intent = Intent(this, ProfilePage::class.java)
        intent.putExtra("EXTRA_SESSION_ID", FirebaseAuth.getInstance().uid);
        startActivity(intent)
    }
    private fun newEventClicked() {
        val intent = Intent(this, NewEvent::class.java)
        startActivity(intent)
    }
    private fun fetchCurUser(){

        // Picasso.get().load(SignupActivity?.currentUser?.url).into(profilePic)
        val curUser =
            FirebaseDatabase.getInstance().getReference("users/${FirebaseAuth.getInstance().uid}")
        val newUser =
            Firebase.database.reference.child("users").child(FirebaseAuth.getInstance().uid!!)
                .addValueEventListener(object :
                    ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val user1 = dataSnapshot.getValue(User::class.java)
                        if (user1 != null) {
                            //Picasso.get().load(user1?.url).into(profilePic)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        //Failed to read value
                    }
                })
    }
    private fun  fetchPosts(){
        val events : ArrayList<Event> = ArrayList()
        var mDatabase = FirebaseDatabase.getInstance().reference
        feed.layoutManager = LinearLayoutManager(this)
        mDatabase.child("/posts").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val event =
                        snapshot.getValue(Event::class.java)
                    event?.uid = snapshot.child("uid").value.toString()
                    event?.description = snapshot.child("description").value.toString()
                    if (event != null) {
                        events.add(event)
                    }

                }
                feed.adapter= postAdapter(events)

            }})
        /*
        for(i in 0..100){
            events.add(Event(null,null,null,null))
        }
*/

    }



}