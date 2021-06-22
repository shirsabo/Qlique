package com.example.qlique.CreateEvent

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.DatePicker
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.qlique.Feed.PostAdapter
import com.example.qlique.Map.CreateEventMapActivity
import com.example.qlique.R
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.firebase.FirebaseError
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_new_event.*
import java.lang.Thread.sleep
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.Delegates


class NewEvent : AppCompatActivity(), RequestCapacityDialog.OnCompleteListener,DatePickerDialog.OnDateSetListener,TimePickerDialog.OnTimeSetListener {

    companion object {
        var savedtime: TextView? = null
        var savedDate: TextView? = null
        var chosenLat by Delegates.notNull<Double>()
        var chosenLon by Delegates.notNull<Double>()
        var capacityMembers by Delegates.notNull<Int>()
    }
    var urLImage: Uri? = null
    var authorUid: String? = null
    var categories: ArrayList<String> = ArrayList(0)
    var latlng: DoubleArray? = null
    var firstTime = true
    var timePickerFragment = TimePickerFragment()
    private var datePickerFragment = DatePickerFragment()
    private var launchSomeActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                urLImage = result.data?.data
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, result.data?.data)
                photo_event_new.setImageBitmap(bitmap)
            }
        }
    private fun openCapacityDialog(){
        RequestCapacityDialog().show(supportFragmentManager, "MyCustomFragment")
    }
    private fun isHeaderExists(): Boolean {
        val textHeader = findViewById<TextView>(R.id.headerNewEvent)
        return textHeader.length()!=0
    }
    private fun isDescExists(): Boolean {
        val textDesc = findViewById<TextView>(R.id.descNewEvent)
        return textDesc.length()!=0
    }
    private fun checkIfAllValuesExist(): Boolean {
        if(!isHeaderExists()){
            Toast.makeText(this, "Please write header", Toast.LENGTH_LONG).show()
            return false
        }
        if(!isDescExists()){
            Toast.makeText(this, "Please write description", Toast.LENGTH_LONG).show()
            return false
        }
        if(urLImage==null){
            Toast.makeText(this, "Please choose photo", Toast.LENGTH_LONG).show()
            return false
        }
        if(authorUid == null){
            return false
        }
        if(savedDate ==null||savedDate?.text==""){
            Toast.makeText(this, "Please choose date", Toast.LENGTH_LONG).show()
            return false
        }
        if(savedtime==null|| savedtime?.text==""){
            Toast.makeText(this, "Please choose hour", Toast.LENGTH_LONG).show()
            return false
        }
        if(categories.size == 0){
            Toast.makeText(this, "Please choose hobbies related", Toast.LENGTH_LONG).show()
            return false
        }
        return  true
    }

    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_event)

        savedDate = DateNewEvent
        savedtime = hourNewEvent
        authorUid = FirebaseAuth.getInstance().currentUser?.uid
        photo_event_new.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            launchSomeActivity.launch(intent)
        }
        btnCatergories.setOnClickListener {
            categories = ArrayList(0)
            val builder = AlertDialog.Builder(this@NewEvent)
            // String array for alert dialog multi choice items
            val categoriesArray = arrayOf(
                "Sport",
                "Ball Game",
                "Biking",
                "Initiative",
                "Business",
                "Fashion",
                "Social",
                "Entertainment",
                "Cooking",
                "Study",
                "Art",
                "Beauty and style",
                "Comedy",
                "Food",
                "Animals",
                "Talent",
                "Cars",
                "Love and dating",
                "Fitness and health",
                "Dance",
                "Outdoor activities",
                "Home and garden",
                "Gaming"
            )
            // Boolean array for initial selected items
            val checkedCategoriesArray = booleanArrayOf(
                false, false, false, false, false, false, false,
                false, false, false, false, false, false, false, false,
                false, false, false, false,
                false, false, false, false
            )
            // Convert the color array to list
            val categoriesList = listOf(*categoriesArray)
            //setTitle
            builder.setTitle("Select categories")
            //set multi choice
            builder.setMultiChoiceItems(
                categoriesArray,
                checkedCategoriesArray
            ) { _, which, isChecked ->
                // Update the current focused item's checked status
                checkedCategoriesArray[which] = isChecked
                // Get the current focused item
                val currentItem = categoriesList[which]
                // Notify the current action
                Toast.makeText(
                    applicationContext,
                    "$currentItem $isChecked",
                    Toast.LENGTH_SHORT
                ).show()
            }
            builder.setPositiveButton("OK") { _, _ ->
                // Do something when click positive button
                for (i in checkedCategoriesArray.indices) {
                    val checked = checkedCategoriesArray[i]
                    if (checked) {
                        categories.add(categoriesList[i])
                    }
                }
            }
            // Set the neutral/cancel button click listener
            builder.setNeutralButton("Cancel") { dialog, which ->
                categories = ArrayList(0)
            }
            val dialog = builder.create()
            // Display the alert dialog on interface
            dialog.show()
            val headerText: TextView = findViewById(R.id.headerNewEvent)
            headerText.movementMethod = ScrollingMovementMethod()
            val description: TextView = findViewById(R.id.descNewEvent)
            description.movementMethod = ScrollingMovementMethod()
        }
        next_first_step.setOnClickListener {
            val textHeader = findViewById<TextView>(R.id.headerNewEvent)
            //val textDesc = findViewById<TextView>(R.id.textInputDesc)
            if (checkIfAllValuesExist()&&checkIfTimeNotPassed(timePickerFragment.hourOfDay,timePickerFragment.minute)){
                val event =
                    Event(
                        urLImage.toString(),
                        authorUid,
                        textInputDesc.editText?.text.toString(),
                        categories
                    )
                createEvent(event)
                finish()
            } else{
                Toast.makeText(this, "Please check if all fields are correct", Toast.LENGTH_LONG).show()
            }
        }
        setCurrentTime()
        setCurrentDate()
    }

    private fun checkIfTimeNotPassed(hourOfDayIn: Int,minuteIn: Int): Boolean {
        if (!datePickerFragment.isEventToday){
            return true;
        }
        val c = Calendar.getInstance()
        if(hourOfDayIn<c.get(Calendar.HOUR_OF_DAY)){
            Toast.makeText(
                this, "Wrong hour",
                Toast.LENGTH_SHORT).show()
            return false
        }
        return if((hourOfDayIn == (c.get(Calendar.HOUR_OF_DAY)))&& //may be the same hour but wrong minutes
            (minuteIn <= (c.get(Calendar.MINUTE)))){
            Toast.makeText(
                this, "Wrong hour",
                Toast.LENGTH_SHORT).show()
            false
        } else{
            true
        }
    }

    private fun createEvent(event: Event) {
        val filename = UUID.randomUUID().toString()
        var mDatabase = FirebaseDatabase.getInstance().reference
        val ref = FirebaseStorage.getInstance().getReference("images/$filename")
        ref.putFile(urLImage!!).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener {
                event.photoUrl = it.toString()
                val headerText: TextView = findViewById(R.id.headerNewEvent)
                event.header = headerText.text.toString()
                event.latitude = chosenLat
                event.longitude = chosenLon
                event.setMembersCapacity(capacityMembers)
                event.setDate(savedDate?.text.toString())
                event.setHour(savedtime?.text.toString())
                val dbRef: DatabaseReference =
                    mDatabase.child("/posts").push() //creates blank record in db
                val postKey = dbRef.key.toString() //the UniqueID/key you seek
                event.eventUid = postKey
                dbRef.setValue(event)
                writeEventLocation(event, postKey)
                PostAdapter.addEventToUser(event.eventUid)
            }
        }
    }

    private fun writeEventLocation(event: Event, postKey: String) {
        val firebaseDatabase = FirebaseDatabase.getInstance()
        val ref: DatabaseReference = firebaseDatabase.getReference("geoFire")
        val geoFire = GeoFire(ref)
        geoFire.setLocation(
            postKey,
            GeoLocation(event.latitude, event.longitude),
            object : GeoFire.CompletionListener {
                fun onComplete(key: String?, error: FirebaseError?) {
                    if (error != null) {
                        System.err.println("There was an error saving the location to GeoFire: $error")
                    } else {
                        println("Location saved on server successfully!")
                    }
                }

                override fun onComplete(key: String?, error: DatabaseError?) {
                    if (error != null) {
                        System.err.println("There was an error saving the location to GeoFire: $error")
                    } else {
                        println("Location saved on server successfully!")
                    }
                }
            })
    }

    fun showTimePickerDialog(v: View) {
        timePickerFragment.show(supportFragmentManager, "timePicker")
        hourNewEvent.text = timePickerFragment.time
    }

    fun showDatePickerDialog(v: View) {
        datePickerFragment .show(supportFragmentManager, "datePicker")
        DateNewEvent.text = datePickerFragment.date
    }

    fun openCreateEventMapActivity(v: View) {
        val intent = Intent(this, CreateEventMapActivity::class.java)
        startActivityForResult(intent, 19)
        intent.putExtra("FirstTime", firstTime)
        startActivity(intent)
        firstTime = false
        sleep(100)
        firstTime = true

    }

    @SuppressLint("SetTextI18n")
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        DateNewEvent.text = "$dayOfMonth.$month.$year"
    }

    @SuppressLint("SetTextI18n")
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        hourNewEvent.text = "$hourOfDay:$minute"
    }
    @SuppressLint("SimpleDateFormat")
    private fun getCurrentTime(): String {
        val timeFormat: DateFormat = SimpleDateFormat("HH:mm")
        timeFormat.timeZone = TimeZone.getTimeZone("Asia/Jerusalem")
        val curTime: String = timeFormat.format(Date())
        return curTime
    }

    private fun setCurrentTime() {
        hourNewEvent.text = getCurrentTime()
    }
    private fun getCurrentDate(): Date {
        val c = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jerusalem"));
        return c.getTime()
    }
    @SuppressLint("SimpleDateFormat")
    private fun setCurrentDate() {
        val dateFormat: SimpleDateFormat? = SimpleDateFormat("MMMM dd, yyyy");
        DateNewEvent.text = dateFormat?.format(getCurrentDate())
    }

    override fun onComplete(r: String) {
        capacityMembers = r.toInt()
    }

    fun capacityOnClick(view: View) {
        openCapacityDialog()
    }
}
