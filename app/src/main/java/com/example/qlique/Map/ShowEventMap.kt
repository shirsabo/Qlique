package com.example.qlique.Map

import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.qlique.CreateEvent.Event
import com.example.qlique.R
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetDialog


class ShowEventMap : EventsMap() {
    lateinit var event:Event
    override fun onMapReady(googleMap: GoogleMap) {
        super.onMapReady(googleMap)
        val event :Event= intent.getSerializableExtra("event") as Event
        this.event = event
        // Move the map's center to be the event chosen.
        val latLng = CameraUpdateFactory.newLatLng(LatLng(event.latitude, event.longitude)) //= LatLng(event.latitude, event.longitude)
        val zoom = CameraUpdateFactory.zoomTo(DEFAULT_ZOOM)//: Float = DEFAULT_ZOOM
       // mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
        // Add a marker to the map.
        addMarkerToMap(event)
        mMap!!.moveCamera(latLng);
        mMap!!.animateCamera(zoom);
        // Hide the information image view.
        val imgView: ImageView = findViewById<View>(R.id.ic_information) as ImageView
        imgView.visibility = View.GONE
        val gps: ImageView = findViewById<View>(R.id.ic_gps) as ImageView
        gps.visibility = View.GONE
        displayInfo(event)
    }
    private fun openJoinDialog(applicationContext: Context, eventUid: String) {
        val view = View.inflate(applicationContext, R.layout.join_event_dialog, null)
        val builder = AlertDialog.Builder(applicationContext)
        builder.setView(view)
        val dialog: Dialog = builder.create()
        val width = (DisplayMetrics().widthPixels)
        val height = (DisplayMetrics().heightPixels * 0.4).toInt()
        dialog.window?.setLayout(width, height)
        dialog.show() /*
        view.leave_btn.setOnClickListener {
            joinEvent(eventUid, holder)
            dialog.cancel()
        }
        view.cancle_leave_btn.setOnClickListener {
            dialog.cancel()

        }
        */
    }

    private fun displayInfo(event: Event) {
        // Display the information of the event.
        val bottomSheetDialogIn = BottomSheetDialog(
            this@ShowEventMap,
            R.style.BottomSheetDialogTheme
        )
        val bottomSheetView:View =
            LayoutInflater.from(this@ShowEventMap).inflate(
                R.layout.layout_bottom_sheet_map, findViewById(
                    R.id.bottomContainer
                )
            )
        updateViewOfBottomDialog(bottomSheetView, event)
        bottomSheetView.findViewById<TextView>(R.id.description_post_info_bottom).text =
            "hey"
        //val image:ImageView = bottomSheetView.findViewById(R.id.post_image_join_btn)
        updateViewOfBottomDialog(bottomSheetView, event)
        bottomSheetDialogIn.setContentView(bottomSheetView)
        bottomSheetDialogIn.show()
        /*
        image.setOnClickListener {
            openJoinDialog(applicationContext, event.eventUid);
        }
         */
    }

    private fun addMarkerToMap(event: Event){
        val hobby = if (event.hobbiesRelated != null && event.hobbiesRelated.size > 0){
            event.hobbiesRelated[0]
        } else {
            ""
        }
        val marker = mMap?.addMarker(
            MarkerOptions()
                .position(LatLng(event.latitude, event.longitude))
                .anchor(0.5f, 0.5f)
                .icon(
                    getBitmapDescriptorFromVector(
                        applicationContext, getImageByHobby(hobby)
                    )
                )
        )
        marker?.tag = event.uid
        mMap?.setOnMarkerClickListener(OnMarkerClickListener { marker ->
            displayInfo(this.event)
            true
        })
    }

    override fun init() {
        Log.d(ContentValues.TAG, "init: initializing")

        mGoogleApiClient = GoogleApiClient.Builder(this)
            .addApi(Places.GEO_DATA_API)
            .addApi(Places.PLACE_DETECTION_API)
            .enableAutoManage(this, this)
            .build()

        mGps!!.setOnClickListener {
            Log.d(ContentValues.TAG, "onClick: clicked gps icon")
        }
    }
    override fun getDeviceLocation() {

    }
}