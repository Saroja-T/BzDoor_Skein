package com.busydoor.app.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

data class UpdateUserStatus (

    @SerializedName("status_code" ) var statusCode : Int?    = null,
    @SerializedName("message"     ) var message    : String? = null,
    @SerializedName("data"        ) var data       : Data?   = Data()

){
    @Keep
    data class Data(
        @SerializedName("message"     ) var message    : String? = null)
}