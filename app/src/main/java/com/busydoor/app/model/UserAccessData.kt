package com.busydoor.app.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class UserAccessData(
        @SerializedName("status_code") val status_code : Int,
        @SerializedName("message") val message : String,
        @SerializedName("data") val data : Data
)
{
    @Keep
    data class Data (
            @SerializedName("useraccess") val useraccess : Boolean)
}