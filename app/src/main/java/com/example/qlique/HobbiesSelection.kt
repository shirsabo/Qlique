package com.example.qlique

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class HobbiesSelection : AppCompatActivity() , AdapterView.OnItemClickListener{
    var hobbiesList:MutableList<String> = mutableListOf<String>()
    private lateinit var continueBtn: Button
    var listView: ListView? = null
    private lateinit var rowItemHobby : ArrayList<RowItemHobby>
    // var arrayAdapter:ArrayAdapter<String> ? = null
    private val hobbies = arrayListOf(
        "Sport", "Initiative", "Business", "Fashion", "Social",
        "Entertainment", "Study", "Beauty and style", "Comedy", "Drama", "Food", "Animals",
        "Talent", "Cars", "Love and dating", "Fitness and health",
        "Dance", "Outdoor activities", "Home and garden"
    )
    private val images = intArrayOf(
        R.drawable.ic_facebook,
        R.drawable.ic_instagram,
        R.drawable.ic_like,
        R.drawable.ic_google_plus,
        R.drawable.ic_id_card,
        R.drawable.ic_facebook,
        R.drawable.ic_instagram,
        R.drawable.ic_like,
        R.drawable.ic_google_plus,
        R.drawable.ic_id_card,
        R.drawable.ic_facebook,
        R.drawable.ic_instagram,
        R.drawable.ic_like,
        R.drawable.ic_google_plus,
        R.drawable.ic_id_card,
        R.drawable.ic_facebook,
        R.drawable.ic_instagram,
        R.drawable.ic_like,
        R.drawable.ic_google_plus
    )


        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_hobbies_selection)
            continueBtn = findViewById(R.id.continueBtn)
            listView = findViewById(R.id.multiple_list_view)

            rowItemHobby = ArrayList<RowItemHobby>()
            for (i in 0 until hobbies.size){
                val item  =
                    RowItemHobby(images[i] as Int, hobbies[i])
                rowItemHobby.add(item)
            }
            listView = findViewById(R.id.multiple_list_view)
            val adapter = CustomViewAdapter(this, R.layout.list_item, rowItemHobby)
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

        override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            // when clicking one of the hobbies we will add or remove them from the hobbies list
            val items: RowItemHobby = parent?.getItemAtPosition(position) as RowItemHobby
            if (items.hobby in hobbiesList){
                hobbiesList.remove(items.hobby)
            } else {
                hobbiesList.add(items.hobby)
            }
        }
    }

