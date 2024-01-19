package com.busydoor.app.model
import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class HomeDataResponse(
    @SerializedName("status_code" ) var statusCode : Int?    = null,
    @SerializedName("message"     ) var message    : String? = null,
    @SerializedName("data"        ) var data       : Data?   = Data()
) {
    data class Data (

        @SerializedName("user_details"    ) var userDetails    : UserDetails?    = UserDetails(),
        @SerializedName("premise_details" ) var premiseDetails : PremiseDetails? = PremiseDetails(),
        @SerializedName("donut_details"   ) var donutDetails   : DonutDetails?   = DonutDetails()

    ){
        data class UserDetails(

            @SerializedName("staff_id") var staffId: Int? = null,
            @SerializedName("staff_first_name") var staffFirstName: String? = null,
            @SerializedName("staff_last_name") var staffLastName: String? = null,
            @SerializedName("staff_image") var staffImage: String? = null,
            @SerializedName("staff_status") var staffStatus: String? = null

        )

        data class PremiseDetails (

            @SerializedName("premise_id"   ) var premiseId   : Int?    = null,
            @SerializedName("premise_name" ) var premiseName : String? = null,
            @SerializedName("address"      ) var address     : String? = null,
            @SerializedName("country"      ) var country     : String? = null,
            @SerializedName("state"        ) var state       : String? = null,
            @SerializedName("city"         ) var city        : String? = null

        )

        data class DonutDetails (

            @SerializedName("date"                       ) var date                     : String? = null,
            @SerializedName("total_in_hours"             ) var totalInHours             : String? = null,
            @SerializedName("total_in_percentage"        ) var totalInPercentage        : Int?    = null,
            @SerializedName("total_offline_hours"        ) var totalOfflineHours        : String? = null,
            @SerializedName("total_offline_percentage"   ) var totalOfflinePercentage   : Int?    = null,
            @SerializedName("total_remaining_percentage" ) var totalRemainingPercentage : Int?    = null,
            @SerializedName("total_request_offsite_hours") var totalRequestOffsiteHours : String?    = null,
            @SerializedName("total_request_offsite_percentage") var totalRequestOffsitePercentage : Int?    = null,
            @SerializedName("first_in_time"              ) var firstInTime              : String? = null,
            @SerializedName("last_out_time"              ) var lastOutTime              : String? = null,
            @SerializedName("request_offsite"            ) var isShowRequestOffsite     : Boolean? = null
        )
    }
}