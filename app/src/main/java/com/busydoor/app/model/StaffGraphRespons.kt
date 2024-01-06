package com.busydoor.app.model

import com.google.gson.annotations.SerializedName



data class StaffGraphcount (

    @SerializedName("status_code" ) var statusCode : Int?    = null,
    @SerializedName("message"     ) var message    : String? = null,
    @SerializedName("data"        ) var data       : Data?   = Data()

){
    data class Data (

        @SerializedName("total_count"                ) var totalCount              : Int?                               = null,
        @SerializedName("total_present_count_bydate" ) var totalPresentCountBydate : ArrayList<TotalPresentCountBydate> = arrayListOf(),
        @SerializedName("staff_details"              ) var staffDetails            : ArrayList<StaffDetails>            = arrayListOf()

    ) {

        data class TotalPresentCountBydate (

            @SerializedName("date"                ) var date              : String? = null,
            @SerializedName("total_present_count" ) var totalPresentCount : Int?    = null

        )

        data class StaffDetails (

            @SerializedName("status"     ) var status    : String? = null,
            @SerializedName("user_id"    ) var userId    : Int?    = null,
            @SerializedName("first_name" ) var firstName : String? = null,
            @SerializedName("last_name"  ) var lastName  : String? = null,
            @SerializedName("photo"      ) var photo     : String? = null,
            @SerializedName("user_type"  ) var userType  : String? = null

        )
    }
}