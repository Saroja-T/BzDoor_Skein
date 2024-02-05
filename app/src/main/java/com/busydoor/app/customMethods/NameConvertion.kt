package com.busydoor.app.customMethods

class NameConvertion {
    fun truncateText(text: String): String {
        val maxLength= 20
        return if (text.length > maxLength) {
            // Trim the string to 'maxLength' characters and add "..."
            text.substring(0, maxLength - 3) + "..."
        } else {
            // Return the original text if it's within 'maxLength' characters
            text
        }
    }
}