package com.example.qlique.LoginAndSignUp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.qlique.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth

/**
 * UpdatePassword
 * This activity is responsible for updating the password of the user in case he wants to switch it.
 */
class UpdatePassword : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var passwordEt: EditText
    private lateinit var changePasswordBtn: Button
    private lateinit var back: Button

    /**
     * initializes the buttons and the text views.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_password)
        auth = FirebaseAuth.getInstance()
        passwordEt = findViewById(R.id.password_edt_text)
        changePasswordBtn = findViewById(R.id.reset_pass_btn)
        back = findViewById(R.id.back_btn_update_pass)
        back.setOnClickListener {
            finish()
        }
        changePasswordBtn.setOnClickListener {
            val password: String = passwordEt.text.toString()
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please enter password", Toast.LENGTH_LONG).show()
            } else {
                auth.currentUser?.updatePassword(password)
                    ?.addOnCompleteListener(this, OnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Password changes successfully", Toast.LENGTH_LONG)
                                .show()
                            finish()
                        } else {
                            Toast.makeText(this, "password not changed", Toast.LENGTH_LONG)
                                .show()
                        }
                    })
            }
        }
    }
}