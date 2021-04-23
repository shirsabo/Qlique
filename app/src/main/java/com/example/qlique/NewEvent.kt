package com.example.qlique

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.PendingIntent.getActivity
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.DatePicker
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.qlique.LoginAndSignUp.SignupActivity
import com.example.qlique.Map.CreateEventMapActivity
import com.example.qlique.Profile.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_new_event.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class NewEvent : AppCompatActivity(),DatePickerDialog.OnDateSetListener,TimePickerDialog.OnTimeSetListener {

    companion object {
        var savedtime: TextView? = null
        var savedDate: TextView? = null
    }

    var urLImage: String? = null
    var authorUid: String? = null
    var categories: ArrayList<String> = ArrayList(0)
    private var launchSomeActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                urLImage = result.data?.data.toString()
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, result.data?.data)
                val bitmapDrawble = BitmapDrawable(bitmap)
                photo_event_new.setImageBitmap(bitmap)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_event)
        savedDate = DateNewEvent
        savedtime = hourNewEvent
        authorUid = FirebaseAuth.getInstance().currentUser.uid
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
                "Sport", "Initiative", "Business", "Fashion", "Social",
                "Entertainment", "Study", "Beauty and style", "Comedy", "Food", "Animals",
                "Talent", "Cars", "Love and dating", "Fitness and health",
                "Dance", "Outdoor activities", "Home and garden", "Gaming"
            )
            // Boolean array for initial selected items
            val checkedCategoriesArray = booleanArrayOf(
                false, false, false, false, false,
                false, false, false, false, false, false,
                false, false, false, false,
                false, false, false, false
            )
            // Convert the color array to list
            val categoriesList = listOf(*categoriesArray)
            //setTitle
            builder.setTitle("Select categories")
            //set multichoice
            builder.setMultiChoiceItems(
                categoriesArray,
                checkedCategoriesArray
            ) { dialog, which, isChecked ->
                // Update the current focused item's checked status
                checkedCategoriesArray[which] = isChecked
                // Get the current focused item
                val currentItem = categoriesList[which]
                // Notify the current action
                Toast.makeText(
                    applicationContext,
                    currentItem + " " + isChecked,
                    Toast.LENGTH_SHORT
                ).show()
            }
            builder.setPositiveButton("OK") { dialog, which ->
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

        }
        next_first_step.setOnClickListener {
            if (urLImage != null && authorUid != null && textInputDesc.getEditText()?.getText()
                    .toString() != null
            ) {
                val event: Event = Event(
                    urLImage,
                    authorUid,
                    textInputDesc.getEditText()?.getText().toString(),
                    categories
                )
                createEvent(event)
            }

        }
        setCurrentTime()
        setCurrentDate()
        //createEvent()
    }

    private fun createEvent(event: Event) {
        //uploadImage(event.photoUrl)
        var mDatabase = FirebaseDatabase.getInstance().reference
        mDatabase.child("/posts").push().setValue(event)
    }

    fun showTimePickerDialog(v: View) {
        val newFragment = TimePickerFragment()
        newFragment.show(supportFragmentManager, "timePicker")
        hourNewEvent.text = newFragment.time
    }

    fun showDatePickerDialog(v: View) {
        val newFragment = DatePickerFragment()
        newFragment.show(supportFragmentManager, "datePicker")
        DateNewEvent.text = newFragment.date
    }

    fun openCreateEventMapActivity(v: View) {
        val intent = Intent(this, CreateEventMapActivity::class.java)
        startActivity(intent)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        DateNewEvent.text = dayOfMonth.toString() + "." + month + "." + year
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        hourNewEvent.text = hourOfDay.toString() + ":" + minute.toString()
    }

    fun setCurrentTime() {
        val timeFormat: DateFormat = SimpleDateFormat("HH:mm")
        timeFormat.setTimeZone(TimeZone.getTimeZone("Asia/Jerusalem"))
        val curTime: String = timeFormat.format(Date())
        hourNewEvent.text = curTime
    }

    private fun setCurrentDate() {
        val c = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jerusalem"));
        var dateFormat: SimpleDateFormat? = SimpleDateFormat("MM/dd/yyyy");
        DateNewEvent.text = dateFormat?.format(c.getTime())
    }
}
