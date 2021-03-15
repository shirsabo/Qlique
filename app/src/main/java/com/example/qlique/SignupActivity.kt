package com.example.qlique

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media.getBitmap
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import java.util.*
import kotlin.concurrent.thread


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */class SignupActivity : AppCompatActivity(), AdapterView.OnItemClickListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var emailEt: EditText
    private lateinit var passwordEt: EditText
    private lateinit var cityEt: EditText
    private lateinit var signUpBtn: Button
    private lateinit var loginBtn: Button
    private lateinit var btnUpload: com.mikhaellopez.circularimageview.CircularImageView
    private lateinit var firstNameEt : EditText
    private lateinit var lastNameEt : EditText
    private lateinit var genderbtn : RadioGroup
    private lateinit var maleBtn : RadioButton
    private lateinit var femaleBtn : RadioButton
    private lateinit var instagram : ImageView
    private lateinit var profilPicture : com.mikhaellopez.circularimageview.CircularImageView
    private lateinit var btnChoose: Button
    var url : String? = ""
    ////

    private lateinit var imageView: ImageView
    ////
    private var filePath: Uri? = null
    private val REQUEST_IMAGE_CAPTURE = 100
    private val TAKE_IMAGE_CODE = 10001

    var listView: ListView? = null
    var arrayAdapter:ArrayAdapter<String> ? = null
    var hobbiesList:MutableList<String> = mutableListOf<String>()
    var selectedPhotoUri:Uri?=null
    var launchSomeActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedPhotoUri = result.data?.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,selectedPhotoUri)
            val bitmapDrawble= BitmapDrawable(bitmap)
            profilPicture.setImageBitmap(bitmap)
        }
    }
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
        btnUpload = findViewById(R.id.img_plus)
        profilPicture = findViewById(R.id.img_profile)
        femaleBtn = findViewById(R.id.radioF)
        instagram =  findViewById(R.id.imp_instagram)
        listView = findViewById(R.id.multiple_list_view)
        arrayAdapter = ArrayAdapter(
            applicationContext,
            android.R.layout.simple_list_item_multiple_choice,
            resources.getStringArray(R.array.hobbies_item)
        )
        
        listView?.adapter = arrayAdapter
        listView?.choiceMode = ListView.CHOICE_MODE_MULTIPLE
        listView?.onItemClickListener = this


        loginBtn.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        instagram.setOnClickListener{
            val uri = Uri.parse("http://instagram.com/_u/nikolbabai")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.instagram.android")
            try {
                startActivity(intent)
            } catch (e : ActivityNotFoundException){
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://instagram.com/_u/nikolbabai")))
            }
        }
        signUpBtn.setOnClickListener {
            val email: String = emailEt.text.toString()
            val password: String = passwordEt.text.toString()
            val city: String = cityEt.text.toString()
            val fname : String = firstNameEt.text.toString()
            val lname : String = lastNameEt.text.toString()
            var gender : String
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(city)
                || TextUtils.isEmpty(fname) || TextUtils.isEmpty(lname) ||
                genderbtn.checkedRadioButtonId == -1 || hobbiesList.size == 0) {
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
                                    writeNewUser(
                                        it1.uid,
                                        fname,
                                        lname,
                                        city,
                                        email,
                                        gender,
                                        hobbiesList,
                                        url!!
                                    )
                                }
                            }
                            Toast.makeText(this, "Successfully Registered", Toast.LENGTH_LONG)
                                .show()
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Registration Failed", Toast.LENGTH_LONG).show()
                        }
                    })
            }


        }
        btnUpload.setOnClickListener{
            val intent = Intent(Intent.ACTION_PICK)
            intent.type="image/*"
            launchSomeActivity.launch(intent)
        }


    }

    private fun writeNewUser(
        userId: String,
        fName: String,
        lName: String,
        city: String,
        email: String,
        gender: String,
        hobbies: List<String>,
        url: String
    ) {
        val uid: String? = FirebaseAuth.getInstance().uid
        val user = User(fName, lName, city, email, gender, uid, hobbies,url)
        database.child("users").child(userId).setValue(user)
        database.child("users").child(userId).get().addOnSuccessListener {
            Log.i("firebase", "Got value ${it.value}")
        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        // when clicking one of the hobbies we will add or remove them from the hobbies list
        val items:String = parent?.getItemAtPosition(position) as String
        if (items in hobbiesList){
            hobbiesList.remove(items)
        } else {
            hobbiesList.add(items)
        }
    }



    fun handleImageClick(view: View) {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, 1)
    }
    fun uploadImage(){
        if(selectedPhotoUri==null){
            return
        }else{
            val filename = UUID.randomUUID().toString()
            val ref = FirebaseStorage.getInstance().getReference("images/$filename")
            ref.putFile(selectedPhotoUri!!).addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener {
                    url = it.toString()
                    val uid = FirebaseAuth.getInstance().uid
                    val ref =FirebaseDatabase.getInstance().getReference("/users/$uid")
                    val newUser =Firebase.database.reference.child("/users/$uid").addValueEventListener(object :
                        ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot){
                            val user1=dataSnapshot.getValue(User::class.java)
                            if (user1!=null){
                                user1.url=url
                                ref.setValue(user1)
                                Log.d("finish",url)
                            }
                        }
                        override fun onCancelled(error: DatabaseError){
                            //Failed to read value
                        }
                    })
                }

            }



        }

    }

}