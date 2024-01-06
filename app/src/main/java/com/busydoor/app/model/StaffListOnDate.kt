package com.busydoor.app.model


import com.google.gson.annotations.SerializedName

data class StaffListOnDate (

    @SerializedName("status_code" ) var statusCode : Int?            = null,
    @SerializedName("message"     ) var message    : String?         = null,
    @SerializedName("data"        ) var data       : ArrayList<Data> = arrayListOf()

){

    data class Data (

        @SerializedName("status"     ) var status    : String? = null,
        @SerializedName("user_id"    ) var userId    : Int?    = null,
        @SerializedName("first_name" ) var firstName : String? = null,
        @SerializedName("last_name"  ) var lastName  : String? = null,
        @SerializedName("photo"      ) var photo     : String? = null,
        @SerializedName("user_type"  ) var userType  : String? = null

    )

}