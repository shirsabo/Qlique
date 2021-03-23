package com.example.qlique

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.example.qlique.Chat.ChatListActivity
import com.example.qlique.LoginAndSignUp.LoginActivity
import com.example.qlique.LoginAndSignUp.UpdatePassword
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val lay : View = findViewById(R.id.app_bar_main_layout)
        val toolbar: Toolbar = lay.findViewById(R.id.toolbar_main)
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
        if(auth.currentUser == null){
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }else{
            Toast.makeText(this, "Already logged in", Toast.LENGTH_LONG).show()
        }

        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_item_one -> profileClicked()
                R.id.nav_item_two -> chatClicked()
                //R.id.nav_item_three -> Toast.makeText(this, "Clicked item three", Toast.LENGTH_SHORT)
                //  .show()
                R.id.nav_item_four -> changePasswordClicked()
                R.id.nav_item_five -> logoutClicked()
            }
            return@setNavigationItemSelectedListener true
        }
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
        Toast.makeText(this, "chat", Toast.LENGTH_LONG)
            .show()

        startActivity(intent)
        finish()
    }

    private fun profileClicked() {
        val intent = Intent(this, ProfilePage::class.java)
        intent.putExtra("EXTRA_SESSION_ID", FirebaseAuth.getInstance().uid);
        startActivity(intent)
    }



}