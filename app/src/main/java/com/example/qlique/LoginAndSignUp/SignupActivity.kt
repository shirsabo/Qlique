package com.example.qlique.LoginAndSignUp

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.qlique.Profile.User
import com.example.qlique.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.scwang.wave.MultiWaveHeader
import java.util.*

/**
 * SignupActivity
 * This activity is responsible for registering
 */
class SignupActivity : AppCompatActivity(), AdapterView.OnItemClickListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var emailEt: EditText
    private lateinit var passwordEt: EditText
    private lateinit var cityEt: EditText
    private lateinit var signUpBtn: Button
    private lateinit var loginBtn: Button
    private lateinit var btnUpload: com.mikhaellopez.circularimageview.CircularImageView
    private lateinit var firstNameEt: EditText
    private lateinit var lastNameEt: EditText
    private lateinit var genderbtn: RadioGroup
    private lateinit var maleBtn: RadioButton
    private lateinit var femaleBtn: RadioButton
    private lateinit var profilPicture: com.mikhaellopez.circularimageview.CircularImageView

    companion object {
        var currentUser: User? = null
    }

    var url: String? = ""
    var hobbiesList: MutableList<String> = mutableListOf<String>()
    var selectedPhotoUri: Uri? = null
    private var launchSomeActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedPhotoUri = result.data?.data
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
                BitmapDrawable(bitmap)
                profilPicture.setImageBitmap(bitmap)
            }
        }

    /**
     * initializes the buttons and the text view, gets the information the user entered.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        val waveHeader = findViewById<MultiWaveHeader>(R.id.waveHeader)
        waveHeader.velocity = 3.0f
        waveHeader.progress = 1.0F
        waveHeader.isRunning()
        waveHeader.gradientAngle = 70
        waveHeader.waveHeight = 60
        waveHeader.closeColor = Color.rgb(47, 122, 255)
        waveHeader.startColor = Color.rgb(47, 122, 160)
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
        btnUpload = findViewById(R.id.img_plus)
        profilPicture = findViewById(R.id.img_profile)
        femaleBtn = findViewById(R.id.radioF)
        loginBtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        signUpBtn.setOnClickListener {
            val email: String = emailEt.text.toString()
            val password: String = passwordEt.text.toString()
            val city: String = cityEt.text.toString()
            val fname: String = firstNameEt.text.toString()
            val lname: String = lastNameEt.text.toString()
            var gender: String
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(city)
                || TextUtils.isEmpty(fname) || TextUtils.isEmpty(lname) || selectedPhotoUri == null
                || genderbtn.checkedRadioButtonId == -1
            ) {
                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_LONG).show()
            } else {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, OnCompleteListener { task ->
                        if (task.isSuccessful) {
                            gender = if (maleBtn.isChecked) {
                                "Male"
                            } else {
                                "Female"
                            }
                            uploadImage()
                            auth.currentUser?.let { it1 ->
                                if (url != null) {
                                    writeNewUser(it1.uid, fname, lname, city, email, gender, url!!)
                                }
                            }
                        } else {
                            Toast.makeText(this, "Registration Failed", Toast.LENGTH_LONG).show()
                        }
                    })
            }
        }
        btnUpload.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            launchSomeActivity.launch(intent)
        }
    }

    /**
     * creates a new user and moves to the intent of the hobbies selection.
     */
    private fun writeNewUser(
        userId: String, fName: String, lName: String, city: String,
        email: String, gender: String, url: String
    ) {
        val uid: String? = FirebaseAuth.getInstance().uid
        val user = User(fName, lName, city, email, gender, uid, url)
        SetCurDeviceToken(user)
        // Creating a new user and saving it in the real time database.
        database.child("users").child(userId).setValue(user)
        // Send a verification email to the new user.
        FirebaseAuth.getInstance().currentUser?.sendEmailVerification()
        // Creating a new intent if selecting hobbies and passing the uid to it.
        val myIntent = Intent(this, HobbiesSelection::class.java)
        myIntent.putExtra("StringVariableName", uid)
        startActivity(myIntent)
        finish()
    }

    /**
     * sets the device token of the user.
     */
    private fun SetCurDeviceToken(user: User) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            val deviceToken = task.result!!
            user.tokenFCM = deviceToken
            updateFcm(user)
        }
    }

    /**
     * updates the user and saves it in the firebase.
     */
    private fun updateFcm(user: User) {
        if (user.uid == null) {
            return
        }
        val ref = FirebaseDatabase.getInstance().getReference("/users/${user.uid}")
        ref.setValue(user)
    }

    /**
     * adds or removes hobbies,  when clicking one of the hobbies we
     * will add or remove them from the hobbies list.
     */
    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        // when clicking one of the hobbies we will add or remove them from the hobbies list
        val items: String = parent?.getItemAtPosition(position) as String
        if (items in hobbiesList) {
            hobbiesList.remove(items)
        } else {
            hobbiesList.add(items)
        }
    }

    /**
     * the user chooses a profile picture and it sets to be his profile picture.
     */
    private fun uploadImage() {
        if (selectedPhotoUri == null) {
            return
        } else {
            val filename = UUID.randomUUID().toString()
            val ref = FirebaseStorage.getInstance().getReference("images/$filename")
            ref.putFile(selectedPhotoUri!!).addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener {
                    url = it.toString()
                    val uid = FirebaseAuth.getInstance().uid
                    val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
                    Firebase.database.reference.child("/users/$uid").addValueEventListener(
                        object :
                            ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                val user1 = dataSnapshot.getValue(User::class.java)
                                if (user1 != null) {
                                    user1.url = url
                                    currentUser = user1
                                    ref.setValue(user1)
                                    Log.d("finish", url)
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                //Failed to read value
                            }
                        })
                }
            }
        }
    }
}