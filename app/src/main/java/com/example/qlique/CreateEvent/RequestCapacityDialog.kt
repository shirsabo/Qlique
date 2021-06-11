package com.example.qlique.CreateEvent
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
import kotlinx.android.synthetic.main.request_capacity.*


class RequestCapacityDialog : DialogFragment() {
    var layout: LinearLayout? = null
    lateinit var button: Button
    lateinit var seekBar: SeekBar
    lateinit var text: TextView

    interface OnCompleteListener {
        fun onComplete(r: String)
    }

    private var mListener: OnCompleteListener? = null

    // make sure the Activity implemented it
    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        try {
            mListener = activity as OnCompleteListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$activity must implement OnCompleteListener")
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //dialog!!.window?.setBackgroundDrawableResource(R.drawable.round_corner)
        return inflater.inflate(R.layout.request_capacity, container, false)
    }

    override fun onStart() {
        super.onStart()
        capacityImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_youth));
        val width = (resources.displayMetrics.widthPixels).toInt()
        val height = (resources.displayMetrics.heightPixels*0.9).toInt()
        button = dialog?.findViewById(R.id.dialog_button_cap) as Button
        seekBar = dialog?.findViewById(R.id.dialog_seekbar_cap) as SeekBar
        text = dialog?.findViewById(R.id.seekbarValCap) as TextView
        button.setOnClickListener {
            val capacity = text.text
            this.mListener?.onComplete(capacity as String)
            dialog!!.dismiss()
        }
        seekBar .setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar,
                                           progress: Int, fromUser: Boolean) {
                val progressCustom = ( progress )
                seekbarValCap.text = progressCustom.toString()
            }

            override fun onStartTrackingTouch(seek: SeekBar) {
                // write custom code for progress is started
            }

            override fun onStopTrackingTouch(seek: SeekBar) {
                // write custom code for progress is stopped
            }
        })
        dialog!!.window?.setLayout(width, height)//ViewGroup.LayoutParams.WRAP_CONTENT, width)
    }

}