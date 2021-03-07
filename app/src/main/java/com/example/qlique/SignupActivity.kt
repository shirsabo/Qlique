package com.example.qlique

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var emailEt: EditText
    private lateinit var passwordEt: EditText
    private lateinit var cityEt: EditText
    private lateinit var signUpBtn: Button
    private lateinit var loginBtn: Button
    private lateinit var firstNameEt : EditText
    private lateinit var lastNameEt : EditText
    private lateinit var genderbtn : RadioGroup
    private lateinit var maleBtn : RadioButton
    private lateinit var femaleBtn : RadioButton

    private lateinit var btnChoose: Button
    private lateinit var btnUpload: Button
    private lateinit var imageView: ImageView
    private var filePath: Uri? = null
    private val PICK_IMAGE_REQUEST = 71

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference
        firstNameEt = findViewById(R.id.fName_edt_text)
        lastNameEt = findViewById(R.id.lName_edt_text)
        emailEt = findViewById(R.id.email_edt_text)
        passwordEt = findViewById(R.id.pass_edt_text)
        cityEt = findViewById(R.id.city_edt_text)
        loginBtn = findViewById(R.id.login_btn)
        signUpBtn = findViewById(R.id.signup_btn)
        genderbtn = findViewById(R.id.Gender)
        maleBtn = findViewById(R.id.radioM)
        femaleBtn = findViewById(R.id.radioF)
        signUpBtn.setOnClickListener {
            val email: String = emailEt.text.toString()
            val password: String = passwordEt.text.toString()
            val city: String = cityEt.text.toString()
            val fname : String = firstNameEt.text.toString()
            val lname : String = lastNameEt.text.toString()
            var gender : String
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(city)
              || TextUtils.isEmpty(fname) || TextUtils.isEmpty(lname) || genderbtn.checkedRadioButtonId == -1) {
                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_LONG).show()
            } else {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, OnCompleteListener { task ->
                        if (task.isSuccessful) {
                            gender = if(maleBtn.isChecked) {
                                "Male"
                            } else {
                                "Female"
                            }
                            auth.currentUser?.let { it1 -> writeNewUser(it1.uid, fname, lname, city, email, gender) }
                            Toast.makeText(this,"Successfully Registered", Toast.LENGTH_LONG).show()
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Registration Failed", Toast.LENGTH_LONG).show()
                        }
                    })
            }
        }

        loginBtn.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


    private fun writeNewUser(userId: String, fName: String, lName: String, city: String, email: String, gender: String) {
        val user = User(fName, lName, city, email, gender)
        database.child("users").child(userId).setValue(user)
        database.child("users").child(userId).get().addOnSuccessListener {
            Log.i("firebase", "Got value ${it.value}")
        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
        }
    }
}