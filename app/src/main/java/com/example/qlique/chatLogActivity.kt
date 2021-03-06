package com.example.qlique

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class chatLogActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)
        supportActionBar?.title="Chat"
    }
}