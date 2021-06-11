package com.example.qlique.CreateEvent

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.text.SimpleDateFormat
import java.util.*

class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {
    var date :String=""
    var iseventToday = true;

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current date as the default date in the picker
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        // Create a new instance of DatePickerDialog and return it
        val datePicker =DatePickerDialog(requireActivity(), this, year, month, day)
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Jerusalem"));
        var dateFormat: SimpleDateFormat? = SimpleDateFormat("yyyy-MM-dd");
        val date: Date? = dateFormat?.parse(dateFormat?.format(c.getTime()))
        datePicker.datePicker.minDate= date?.getTime()!!

        return datePicker
    }

    override fun onDateSet(view: DatePicker, yearIn: Int, monthIn: Int, dayIn: Int) {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        iseventToday = yearIn == year && monthIn==month&&dayIn==day
        if ( NewEvent.savedDate!=null){
            NewEvent.savedDate!!.text  = day.toString()+"."+(month+1)+"."+year
        }
    }
}