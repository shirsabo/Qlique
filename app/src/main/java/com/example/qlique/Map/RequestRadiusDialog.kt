package com.example.qlique.Map

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.qlique.R
import kotlinx.android.synthetic.main.request_radius.*

/**
 * RequestRadiusDialog
 * when the user enters to the map we ask him to choose the radius of the events
 * we will present him.
 */
class RequestRadiusDialog : DialogFragment() {
    var layout: LinearLayout? = null
    lateinit var button: Button
    lateinit var seekBar: SeekBar
    lateinit var text: TextView

    interface OnCompleteListener {
        fun onComplete(r: String)
    }

    private var mListener: OnCompleteListener? = null

    /**
     * make sure the Activity implemented it, setts a listener
     */
    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        try {
            mListener = activity as OnCompleteListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$activity must implement OnCompleteListener")
        }
    }

    /**
     * calls when the view in being created.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.request_radius, container, false)
    }

    /**
     * displays the button and the seek bar so the user could choose rhe radius.
     */
    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels).toInt()
        val height = (resources.displayMetrics.heightPixels * 1.0).toInt()
        button = dialog?.findViewById(R.id.dialog_button) as Button
        seekBar = dialog?.findViewById(R.id.dialog_seekbar) as SeekBar
        text = dialog?.findViewById(R.id.seekbarVal) as TextView
        button.setOnClickListener {
            val radius = text.text
            this.mListener?.onComplete(radius as String)
            dialog!!.dismiss()
        }
        seekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar,
                                           progress: Int, fromUser: Boolean) {
                val progressCustom = ( progress )
                seekbarVal.text = progressCustom.toString()
            }

            override fun onStartTrackingTouch(seek: SeekBar) {
                // write custom code for progress is started
            }

            override fun onStopTrackingTouch(seek: SeekBar) {
                // write custom code for progress is stopped
            }
        })
        dialog!!.window?.setLayout(width, height)
    }

}