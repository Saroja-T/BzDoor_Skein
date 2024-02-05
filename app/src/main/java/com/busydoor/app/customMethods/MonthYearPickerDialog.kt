import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.widget.NumberPicker
import com.busydoor.app.R
import java.text.SimpleDateFormat
import java.util.*

class MonthYearPickerDialog(
    context: Context,
    private val listener: OnDateSetListener,
    private val calendar: Calendar,
    private val initialDate: String,
) : AlertDialog(context) {

    private fun getMonthName(month: Int): String {
        val monthNames = arrayOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        return monthNames[month - 1]
    }

    init {
        val inflater = layoutInflater.inflate(R.layout.month_year_picker, null)
        setView(inflater)

        val monthPicker: NumberPicker = inflater.findViewById(R.id.monthPicker)
        val yearPicker: NumberPicker = inflater.findViewById(R.id.yearPicker)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = dateFormat.parse(initialDate)

        if (date != null) {
            val calendar1 = Calendar.getInstance()
            calendar1.time = date


            // Customize your pickers, set min and max values, etc.
            monthPicker.minValue = 1
            monthPicker.maxValue = 12
            monthPicker.value = calendar1.get(Calendar.MONTH) + 1 // Calendar.MONTH is zero-based
            monthPicker.displayedValues = arrayOf(
                "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
            )

            yearPicker.minValue = 2023
            yearPicker.maxValue = calendar.get(Calendar.YEAR)
            yearPicker.value = calendar1.get(Calendar.YEAR)
        }
        setButton(BUTTON_POSITIVE, "OK") { _, _ ->
            listener.onDateSet(getMonthName(monthPicker.value), yearPicker.value)
        }

        setButton(BUTTON_NEGATIVE, "Cancel") { _, _ -> }

        setTitle("Select Month and Year")

        // Add an OnValueChangedListener to yearPicker to dynamically update the max month value
        yearPicker.setOnValueChangedListener { _, _, newYear ->
            updateMaxMonth(monthPicker, newYear)
        }

        // Initial call to updateMaxMonth with the initial year value
        updateMaxMonth(monthPicker, yearPicker.value)
    }

    private fun updateMaxMonth(monthPicker: NumberPicker, selectedYear: Int) {
        // Set the max month value based on the selected year
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val maxMonth = if (selectedYear == currentYear) {
            Calendar.getInstance().get(Calendar.MONTH) + 1
        } else {
            12
        }
        monthPicker.maxValue = maxMonth
    }

    interface OnDateSetListener {
        fun onDateSet(month: String, year: Int)
    }
}

