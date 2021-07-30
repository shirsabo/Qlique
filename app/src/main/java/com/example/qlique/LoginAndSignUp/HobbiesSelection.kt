package com.example.qlique.LoginAndSignUp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.qlique.Feed.MainActivity
import com.example.qlique.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

/**
 * HobbiesSelection
 * This activity is responsible for the hobbies selection, and the icon of each hobby.
 */
class HobbiesSelection : AppCompatActivity(), AdapterView.OnItemClickListener {
    var hobbiesList: MutableList<String> = mutableListOf<String>()
    private lateinit var continueBtn: Button
    var listView: ListView? = null
    private lateinit var rowItemHobby: ArrayList<RowItemHobby>
    private val hobbies = arrayListOf(
        "Sport", "Initiative", "Business", "Fashion", "Social",
        "Entertainment", "Study", "Beauty and style", "Comedy", "Food", "Animals",
        "Talent", "Cars", "Love and dating", "Fitness and health",
        "Dance", "Outdoor activities", "Home and garden", "Gaming"
    )
    private val images = intArrayOf(
        R.drawable.ic_sports,
        R.drawable.ic_initiative,
        R.drawable.ic_business,
        R.drawable.ic_dress,
        R.drawable.ic_social,
        R.drawable.ic_entertainment,
        R.drawable.ic_study,
        R.drawable.ic_beauty_and_style,
        R.drawable.ic_comedy,
        R.drawable.ic_food,
        R.drawable.ic_animals,
        R.drawable.ic_microphone,
        R.drawable.ic_car,
        R.drawable.ic_love_and_dating,
        R.drawable.ic_fitness_and_health,
        R.drawable.ic_dance,
        R.drawable.ic_outdoor_activities,
        R.drawable.ic_home_and_garden,
        R.drawable.ic_gaming
    )

    /**
     * initializes the buttons and the text view,
     * updating the hobbies list of this user in the real time database.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hobbies_selection)
        continueBtn = findViewById(R.id.continueBtn)
        listView = findViewById(R.id.multiple_list_view)
        rowItemHobby = ArrayList<RowItemHobby>()
        for (i in 0 until hobbies.size) {
            val item =
                RowItemHobby(
                    images[i] as Int,
                    hobbies[i]
                )
            rowItemHobby.add(item)
        }
        listView = findViewById(R.id.multiple_list_view)
        val adapter = CustomViewAdapter(
            this,
            R.layout.list_item,
            rowItemHobby
        )
        listView?.adapter = adapter
        listView?.choiceMode = ListView.CHOICE_MODE_MULTIPLE
        listView?.onItemClickListener = this
        continueBtn.setOnClickListener {
            if (hobbiesList.size == 0) {
                Toast.makeText(this, "Please choose at least one item", Toast.LENGTH_LONG).show()
            } else {
                val ref = Firebase.database.reference
                // Getting the user id from the previous intent.
                val uid: String? = FirebaseAuth.getInstance().uid
                // updating the hobbies list of this user in the real time database.
                ref.child("users/$uid/hobbies").setValue(hobbiesList)
                Toast.makeText(this, "Successfully Registered", Toast.LENGTH_LONG)
                    .show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    /**
     * adds or removes hobbies,  when clicking one of the hobbies we
     * will add or remove them from the hobbies list.
     */
    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        // when clicking one of the hobbies we will add or remove them from the hobbies list
        val items: RowItemHobby = parent?.getItemAtPosition(position) as RowItemHobby
        if (items.hobby in hobbiesList) {
            hobbiesList.remove(items.hobby)
        } else {
            hobbiesList.add(items.hobby)
        }
    }
}

