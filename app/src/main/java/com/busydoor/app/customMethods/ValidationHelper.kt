package com.busydoor.app.customMethods

import android.content.Context
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.busydoor.app.R

object ValidationHelper {
    fun isRegistrationDataValid(
        etFirstName: EditText,
        etLastName: EditText,
        edMobileNumber: EditText,
        tvSpinnerError: TextView,
        accessLevelName: String,
        context: Context
    ): Boolean {
        if (etLastName.text.toString().isEmpty() ||
            etFirstName.text.toString().isEmpty() ||
            accessLevelName.isEmpty() ||
            edMobileNumber.text.toString().isEmpty()
        ) {
            // Set error messages for individual fields
            etFirstName.error = context.getString(R.string.error_empty_first_name)
            etLastName.error = context.getString(R.string.error_last_name_required)
            edMobileNumber.error = context.getString(R.string.validMobileNumber)

            // Show error for the spinner
            tvSpinnerError.visibility = View.VISIBLE
            return false
        }

        if (edMobileNumber.text.toString().length != 10) {
            edMobileNumber.error = context.getString(R.string.validMobileNumber)
            return false
        }

        if (etFirstName.text.toString().length < 3) {
            etFirstName.error = context.getString(R.string.error_short_first_name)
            return false
        }

        if (etLastName.text.toString().isEmpty()) {
            etLastName.error = context.getString(R.string.error_last_name_required)
            return false
        }

        if (accessLevelName.isEmpty()) {
            tvSpinnerError.visibility = View.VISIBLE
            false
        }

        // Additional validations or checks can be added here

        return true
    }
}
