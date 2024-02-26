package com.busydoor.app.model

import com.google.gson.annotations.SerializedName


data class StaffCountResponse (

    @SerializedName("status_code" ) var statusCode : Int?    = null,
    @SerializedName("message"     ) var message    : String? = null,
    @SerializedName("data"        ) var data       : Data?   = Data()

){
    data class Data (

        @SerializedName("userdetails"    ) var userDetails    : Userdetails?    = Userdetails(),
        @SerializedName("premisedetails" ) var premiseDetails : Premisedetails? = Premisedetails(),
        @SerializedName("count"          ) var count          : Count?          = Count(),
    ) {

        data class Count(

            @SerializedName("total_count") var totalCount: Int? = null,
            @SerializedName("total_present_count") var totalPresentCount: Int? = null,
            @SerializedName("checked_in_count") var checkedInCount: Int? = null,
            @SerializedName("checked_out_count") var checkedOutCount: Int? = null,
            @SerializedName("offline_count") var offlineCount: Int? = null

        )

        data class Userdetails(

            @SerializedName("user_id") var userId: Int? = null,
            @SerializedName("user_first_name") var userFirstName: String? = null,
            @SerializedName("user_last_name") var userLastName: String? = null,
            @SerializedName("user_image") var userImage: String? = null,
            @SerializedName("user_access_level") var userAccessLevel: String? = null,
            @SerializedName("user_status") var userStatus: String? = null

        )

        data class Premisedetails(

            @SerializedName("premise_id") var premiseId: Int? = null,
            @SerializedName("premise_name") var premiseName: String? = null,
            @SerializedName("address") var address: String? = null,
            @SerializedName("city") var city: String? = null,
            @SerializedName("state") var state: String? = null,
            @SerializedName("country") var country: String? = null

        )
    }
}