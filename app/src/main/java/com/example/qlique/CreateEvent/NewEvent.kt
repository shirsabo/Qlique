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

/**
 * Class NewEvent
 * responsible for collecting the future event's data from user and posting it to the Firebase DB.
 */
class NewEvent : AppCompatActivity(), RequestCapacityDialog.OnCompleteListener,DatePickerDialog.OnDateSetListener,TimePickerDialog.OnTimeSetListener {

    companion object {
        var savedtime: TextView? = null //the time of the event
        var savedDate: TextView? = null //the date of the event
        var chosenLat by Delegates.notNull<Double>() //the latitude of the event
        var chosenLon by Delegates.notNull<Double>() //the longitude of the event
        var textViewCapacity: TextView? = null
        var capacityMembers  by Delegates.observable(-1){ property, oldValue, newValue ->
            if (newValue>=0){
                textViewCapacity?.text = newValue.toString()
            }
        }// the members capacity of the event
        const val chooseAddress = "Please Choose Address"
        var textViewAddress: TextView? = null

        var addressChosen by Delegates.observable("Please Choose Address") { property, oldValue, newValue ->
            textViewAddress?.text = newValue
        }
    }
    var urLImage: Uri? = null
    var authorUid: String? = null
    var categories: ArrayList<String> = ArrayList(0)
    var firstTime = true
    var timePickerFragment = TimePickerFragment()
    private var datePickerFragment = DatePickerFragment()
    private var chooseImageActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                urLImage = result.data?.data // updates the url of the image
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, result.data?.data)
                photo_event_new.setImageBitmap(bitmap) // shows the image in the activity
            }
        }
    /**
     * Opens the Dialog which responsible of choosing number of members
     */
    private fun openCapacityDialog(){
        RequestCapacityDialog().show(supportFragmentManager, "MyCustomFragment")
    }
    /**
     * checks if a header was set by the user.
     * @return True if header exists, else False
     */
    private fun isHeaderExists(): Boolean {
        val textHeader = findViewById<TextView>(R.id.headerNewEvent)
        return textHeader.length()!=0
    }
    /**
     * checks if a desc was set by the user.
     * @return True if desc exists,, else False
     */
    private fun isDescExists(): Boolean {
        val textDesc = findViewById<TextView>(R.id.descNewEvent)
        return textDesc.length()!=0
    }
    /**
     * checks if all the values were set by the user before submitting
     * @return True if everything exist, else  False
     */
    private fun checkIfAllValuesExist(): Boolean {
        if(!isHeaderExists()){
            Toast.makeText(this, "Please write header", Toast.LENGTH_LONG).show()
            return false
        }
        if(!isDescExists()){
            Toast.makeText(this, "Please write description", Toast.LENGTH_LONG).show()
            return false
        }
        if(urLImage == null){
            Toast.makeText(this, "Please choose photo", Toast.LENGTH_LONG).show()
            return false
        }
        if(authorUid == null){
            return false
        }
        if(savedDate == null || savedDate?.text == ""){
            Toast.makeText(this, "Please choose date", Toast.LENGTH_LONG).show()
            return false
        }
        if(savedtime == null|| savedtime?.text == ""){
            Toast.makeText(this, "Please choose hour", Toast.LENGTH_LONG).show()
            return false
        }
        if(categories.size == 0){
            Toast.makeText(this, "Please choose hobbies related", Toast.LENGTH_LONG).show()
            return false
        }
        if(capacityMembers < 0){
            Toast.makeText(this, "Please choose valid number of members", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }
    @SuppressLint("CutPasteId")
    /**
     * Sets contentView, sets onclick listeners, sets default variables.
     * @param savedInstanceState -  param for super.onCreate
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_event)
        savedDate = DateNewEvent
        savedtime = hourNewEvent
        val addressTextView = findViewById<TextView>(R.id.Address)
        textViewAddress = addressTextView
        val capTextView = findViewById<TextView>(R.id.membersCapacity)
        textViewCapacity = capTextView
        addressTextView.text = chooseAddress
        capacityMembers = -1 // by that we can know if no member number is submitted
        authorUid = FirebaseAuth.getInstance().currentUser?.uid
        // sets Click Listener for uploading image
        photo_event_new.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            chooseImageActivity.launch(intent)
        }
        //// sets Click Listener for choosing event's hobbies
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
        //sets onClick event for posting the new event to Firebase
        next_first_step.setOnClickListener {
            if (checkIfAllValuesExist()&&checkIfTimeNotPassed(timePickerFragment.hourOfDay,timePickerFragment.minute)){
                val event =
                    Event(
                        urLImage.toString(),
                        authorUid,
                        textInputDesc.editText?.text.toString(),
                        categories
                    )
                createEvent(event) //posts it on Firebase
                finish()
            } else{
                Toast.makeText(this, "Please check if all fields are correct", Toast.LENGTH_LONG).show()
            }
        }
        setCurrentTime()
        setCurrentDate()
    }
    /**
     * Checks if the event is not in the past.
     * @params - hourOfDayIn: Int,minuteIn: Int
     * @return true if time has not passed, else False
     */
    private fun checkIfTimeNotPassed(hourOfDayIn: Int,minuteIn: Int): Boolean {
        //cannot chose event from the past in the dialog picker
        if (!datePickerFragment.isEventToday){
            return true;
        }
        val c = Calendar.getInstance()
        if(hourOfDayIn<c.get(Calendar.HOUR_OF_DAY)){ // if the hour is in the future , return false
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
    /**
     * creates Event object according to the data collected from user, posting the object to Firebase.
     * @params -Event
     */
    private fun createEvent(event: Event) {
        val filename = UUID.randomUUID().toString()
        var mDatabase = FirebaseDatabase.getInstance().reference
        val ref = FirebaseStorage.getInstance().getReference("images/$filename")
        ref.putFile(urLImage!!).addOnSuccessListener { //uploads the photo to Firebase
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
                event.members.add(authorUid)
                dbRef.setValue(event) //saves the event in Firebase in "/posts"
                writeEventLocation(event, postKey)
                PostAdapter.addEventToUser(event.eventUid) //add the event to the author's events
            }
        }
    }
    /**
     * Saves the event's location in "/geoFire" which enables to fetch events by radius very easily.
     * @params - event: Event, postKey: String
     */
    private fun writeEventLocation(event: Event, postKey: String) {
        val firebaseDatabase = FirebaseDatabase.getInstance()
        val ref: DatabaseReference = firebaseDatabase.getReference("geoFire")
        val geoFire = GeoFire(ref)
        //sets the location with the coordinates the user chose
        geoFire.setLocation(
            postKey,
            GeoLocation(event.latitude, event.longitude),
            object : GeoFire.CompletionListener {
                override fun onComplete(key: String?, error: DatabaseError?) {
                    if (error != null) {
                        System.err.println("There was an error saving the location to GeoFire: $error")
                    } else {
                        println("Location saved on server successfully!")
                    }
                }
            })
    }
    /**
     * shows the TimePickerDialog
     */
    fun showTimePickerDialog(v: View) {
        timePickerFragment.show(supportFragmentManager, "timePicker")
        hourNewEvent.text = timePickerFragment.time //updates the picked time
    }
    /**
     * shows the DatePickerDialog
     */

    fun showDatePickerDialog(v: View) {
        datePickerFragment .show(supportFragmentManager, "datePicker")
        DateNewEvent.text = datePickerFragment.date //updates the picked date
    }
    /**
     * Starts the [CreateEventMapActivity].
     * in this activity the user can choose the events location, sees how to get to his/hers location
     */
    fun openCreateEventMapActivity(v: View) {
        val intent = Intent(this, CreateEventMapActivity::class.java)
        startActivityForResult(intent, 19)
        // passes a flag if its the first time opening this activity
        intent.putExtra("FirstTime", firstTime)
        startActivity(intent)
        firstTime = false
        sleep(100)
        firstTime = true

    }
    @SuppressLint("SetTextI18n")
    /**
     * saves the date of the event in [DateNewEvent]
     * @params view: DatePicker?, year: Int, month: Int, dayOfMonth: Int
     */
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        DateNewEvent.text = "$dayOfMonth.$month.$year"
    }
    /**
     * saves the time of the event in [ hourNewEvent]
     * @params view: TimePicker?, hourOfDay: Int, minute: Int
     */
    @SuppressLint("SetTextI18n")
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        hourNewEvent.text = "$hourOfDay:$minute"
    }
    @SuppressLint("SimpleDateFormat")
    /**
     * Returns the current time
     * @return curTime - the current time
     */
    private fun getCurrentTime(): String {
        val timeFormat: DateFormat = SimpleDateFormat("HH:mm")
        timeFormat.timeZone = TimeZone.getTimeZone("Asia/Jerusalem")
        val curTime: String = timeFormat.format(Date())
        return curTime
    }
    /**
     * Sets the current time in [hourNewEvent]
     */
    private fun setCurrentTime() {
        hourNewEvent.text = getCurrentTime()
    }
    /**
     *Gets the current date
     * @return c.time - current time
     */
    private fun getCurrentDate(): Date {
        val c = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jerusalem"));
        return c.time
    }
    @SuppressLint("SimpleDateFormat")
    /**
     *Sets the current date in [DateNewEvent]
     */
    private fun setCurrentDate() {
        val dateFormat: SimpleDateFormat? = SimpleDateFormat("MMMM dd, yyyy");
        DateNewEvent.text = dateFormat?.format(getCurrentDate())
    }
    /**
     * Sets the [capacityMembers] when complete
     *@param r - members capacity
     */
    override fun onComplete(r: String) {
        capacityMembers = r.toInt()
    }
    /**
     * Opens Capacity Dialog
     */
    fun capacityOnClick(view: View) {
        openCapacityDialog()
    }
}
