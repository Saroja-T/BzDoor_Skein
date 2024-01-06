package com.busydoor.app.customCalender

import java.text.SimpleDateFormat
import java.util.*

data class CalendarDateModel(var data: Date, var isSelected: Boolean = false) {

    val calendarDay: String
        get() = SimpleDateFormat("EE", Locale.getDefault()).format(data)

    val calendarDate: String
        get() {
            val cal = Calendar.getInstance()
            cal.time = data
            return cal[Calendar.DAY_OF_MONTH].toString()
        }
    val calendarDateStr: String
        get() = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(data)

}