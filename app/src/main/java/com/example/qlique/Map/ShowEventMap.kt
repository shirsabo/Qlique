package com.example.qlique.Map

import com.example.qlique.CreateEvent.Event
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.example.qlique.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class ShowEventMap : BasicMapActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_event_map)
    }
    override fun onMapReady(googleMap: GoogleMap) {
        super.onMapReady(googleMap)
        // Hide the information image view.
        val imgView: ImageView = findViewById<View>(R.id.ic_information) as ImageView
        imgView.visibility = View.GONE
        // Display the wanted event.
        val event = intent.getStringExtra("event")
        /*val hobby = if (event.hobbiesRelated != null && event.hobbiesRelated.size > 0){
            event.hobbiesRelated[0]
        } else {
            ""
        }
        val marker = mMap?.addMarker(
            MarkerOptions()
                .position(LatLng(event.latitude, event.longitude))
                .anchor(0.5f, 0.5f)
                .icon(
                    DisplayEventsMapActivity().getBitmapDescriptorFromVector(
                        applicationContext, DisplayEventsMapActivity().getImageByHobby(hobby)
                    )
                )
        )
        marker?.tag = event.uid*/
        /*mMap!!.setOnMarkerClickListener { marker ->
            Firebase.database.reference.child("posts").child(marker.tag.toString())
                .addValueEventListener(object :
                    ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val event = dataSnapshot.getValue(Event::class.java)
                        if (event != null) {
                            event.uid = dataSnapshot.child("uid").value.toString()
                            event.description = dataSnapshot.child("description").value.toString()
                            val bottomSheetDialogIn: BottomSheetDialog = BottomSheetDialog(
                                this@ShowEventMap,
                                R.style.BottomSheetDialogTheme
                            )
                            val bottom_sheet_view:View =
                                LayoutInflater.from(this@ShowEventMap).inflate(
                                    R.layout.layout_bottom_sheet_map, findViewById(
                                        R.id.bottomContainer
                                    )
                                )
                            updateViewOfBottomDialog(bottom_sheet_view,event)
                            bottom_sheet_view.findViewById<TextView>(R.id.description_post_info_bottom).text =
                                "hey"
                            updateViewOfBottomDialog(bottom_sheet_view,event)
                            bottomSheetDialogIn.setContentView(bottom_sheet_view)
                            bottomSheetDialogIn.show()

                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        //Failed to read value
                    }
                })
            false
        }*/
    }

}