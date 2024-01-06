package com.busydoor.app.customMethods

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*


class HourYAxisValueFormatter : ValueFormatter() {
    companion object {
        private val TAG = HourYAxisValueFormatter::class.java.simpleName
    }

    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        val date = Date(value.toLong())
        val dateFormatExpression = SimpleDateFormat("hh:mm a")
        return dateFormatExpression.format(date)
    }
}