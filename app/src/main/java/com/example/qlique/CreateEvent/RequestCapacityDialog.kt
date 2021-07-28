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

/**
 * Class RequestCapacityDialog.
 * This activity is responsible for the Dialog which allows the
 * user to choose the members capacity of his/hers event.
 */
class RequestCapacityDialog : DialogFragment() {
    var layout: LinearLayout? = null
    lateinit var button: Button //submitting the capacity
    lateinit var seekBar: SeekBar //the seekBar which the user can choose capacity
    lateinit var text: TextView // the text of the capacity
    var heightPrecent = 0.9
    interface OnCompleteListener {
        fun onComplete(r: String)
    }
    private var mListener: OnCompleteListener? = null
    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        try {
            mListener = activity as OnCompleteListener //sets complete listener
        } catch (e: ClassCastException) {
            throw ClassCastException("$activity must implement OnCompleteListener")
        }
    }
    /**
     *@return the configured inflater
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.request_capacity, container, false)
    }
    /**
     *@return the configured inflater
     */
    override fun onStart() {
        super.onStart()
        capacityImage.setImageDrawable(resources.getDrawable(R.drawable.ic_youth));
        //sets the size of the dialog
        val width = (resources.displayMetrics.widthPixels)
        val height = (resources.displayMetrics.heightPixels*heightPrecent).toInt()
        //gets all the elements
        button = dialog?.findViewById(R.id.dialog_button_cap) as Button
        seekBar = dialog?.findViewById(R.id.dialog_seekbar_cap) as SeekBar
        text = dialog?.findViewById(R.id.seekbarValCap) as TextView
        //sets OnClickListener, when clicked calls to onComplete with the chosen capacity
        button.setOnClickListener {
            val capacity = text.text
            this.mListener?.onComplete(capacity as String)
            dialog!!.dismiss() // cancels dialog
        }
        // adds OnSeekBarChangeListener
        seekBar .setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            /**
             * updates the [seekbarValCap] to the current [progressCustom] in string
             *@params seek: SeekBar, progress: Int, fromUser: Boolea
             */
            override fun onProgressChanged(seek: SeekBar,
                                           progress: Int, fromUser: Boolean) {
                val progressCustom = ( progress )
                seekbarValCap.text = progressCustom.toString()
            }

            override fun onStartTrackingTouch(seek: SeekBar) {
            }

            override fun onStopTrackingTouch(seek: SeekBar) {
            }
        })
        dialog!!.window?.setLayout(width, height)// sets the size of the dialog
    }

}