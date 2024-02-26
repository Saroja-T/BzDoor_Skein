package com.busydoor.app.customMethods

import android.app.DatePickerDialog
import android.content.Context
import android.os.Build
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import com.busydoor.app.R
import java.text.SimpleDateFormat
import java.util.*

object DatePickerUtil {

    @RequiresApi(Build.VERSION_CODES.R)
    fun showDatePicker(
        context: Context,
        initialDate: String,
        onDateSelected: (String) -> Unit
    ) {
        val initialCalendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        try {
            val parsedDate = dateFormat.parse(initialDate)
            parsedDate?.let {
                initialCalendar.time = it
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val calendar = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            context,
            { _, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, monthOfYear, dayOfMonth)
                }
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val formattedDate = dateFormat.format(selectedDate.time)
                onDateSelected(formattedDate)
            },
            initialCalendar.get(Calendar.YEAR),
            initialCalendar.get(Calendar.MONTH),
            initialCalendar.get(Calendar.DAY_OF_MONTH)
        )

        // Set the maximum date to the current date within the current month
        datePickerDialog.datePicker.maxDate = calendar.timeInMillis
        // Find the background view of the DatePicker and set the color
        val ll = datePickerDialog.datePicker.getChildAt(0) as LinearLayout
        val ll2 = ll.getChildAt(0) as LinearLayout
        for (i in 0 until ll2.childCount) {
            val dpHeaderText = ll2.getChildAt(i) as View
            dpHeaderText.setBackgroundColor(context.resources.getColor(R.color.app_color))
        }

        datePickerDialog.show()
    }
}

