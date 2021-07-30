package com.example.qlique.CreateEvent
import android.annotation.SuppressLint
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*

/**
 * TimePickerFragment
 * This activity is responsible for the Dialog which allows the
 * user to choose the time and the date of his/hers event.
 */
class TimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {
    var time:String=""
    var hourOfDay:Int = 0
    var minute:Int = 0

    /**
     * Use the current time as the default values for the picker
     * Create a new instance of TimePickerDialog and return it
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)
        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(activity, this, hour, minute, is24HourFormat(activity))
    }

    /**
     * saves the time that was chosen.
     */
    @SuppressLint("SetTextI18n")
    override fun onTimeSet(view: TimePicker, hourOfDayIn: Int, minuteIn: Int) {
        var m=minuteIn.toString()
        if (minuteIn<10){
            m="0$m"
        }
        hourOfDay =hourOfDayIn
        // minute = minuteIn
        if ( NewEvent.savedtime!=null){
            NewEvent.savedtime!!.text  = "$hourOfDay:$m"
        }
    }
}