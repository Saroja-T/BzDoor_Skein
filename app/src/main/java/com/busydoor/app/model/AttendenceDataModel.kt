package com.busydoor.app.model

import com.google.gson.annotations.SerializedName


data class AttendanceResponse (
    @SerializedName("status_code" ) var statusCode : Int?              = null,
    @SerializedName("message"     ) var message    : String?           = null,
    @SerializedName("data"        ) var data       : ArrayList<String> = arrayListOf()
)