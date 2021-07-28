package com.example.qlique.CreateEvent
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.text.SimpleDateFormat
import java.util.*
/**
 * Class  DatePickerFragment.
 * This activity responsible of the [DatePickerDialog].
 */
class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {
    var date :String="" //the chosen date
    var isEventToday = true
    @SuppressLint("SimpleDateFormat")
    /**
     * Configures the [Dialog] with min date,
     * @return datePicker - the Dialog
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current date as the default date in the picker
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        // Create a new instance of DatePickerDialog and return it
        val datePicker =DatePickerDialog(requireActivity(), this, year, month, day)
        val dateFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd");
        val date: Date? = dateFormat.parse(dateFormat.format(c.time))
        datePicker.datePicker.minDate= date?.time!!
        return datePicker
    }

    @SuppressLint("SetTextI18n")
    /**
     * Called when data is set.
     * Sets the date of the future event to be the date chosen by the dialog.
     * @return datePicker - the Dialog
     */
    override fun onDateSet(view: DatePicker, yearIn: Int, monthIn: Int, dayIn: Int) {
        val c = Calendar.getInstance()
        val curYear = c.get(Calendar.YEAR)
        val curMonth = c.get(Calendar.MONTH)
        val curDay = c.get(Calendar.DAY_OF_MONTH)
        isEventToday = yearIn == curYear && monthIn==curMonth&&dayIn==curDay
        if ( NewEvent.savedDate!=null){
            NewEvent.savedDate!!.text  = dayIn.toString()+"."+(monthIn+1)+"."+yearIn
        }
    }
}