package com.busydoor.app.model

import com.google.gson.annotations.SerializedName

data class StaffGraphDetails (

    @SerializedName("status_code" ) var statusCode : Int?    = null,
    @SerializedName("message"     ) var message    : String? = null,
    @SerializedName("data"        ) var data       : Data?   = Data()

){
    data class Data (

        @SerializedName("user_details"    ) var userDetails    : UserDetails?            = UserDetails(),
        @SerializedName("premise_details" ) var premiseDetails : PremiseDetails?         = PremiseDetails(),
        @SerializedName("graph_details"   ) var graphDetails   : ArrayList<GraphDetails> = arrayListOf(),
        @SerializedName("pdfdownload"    ) var pdfDownload    : String?                = null // Add this line


    ){
        data class GraphDetails (

            @SerializedName("date"                             ) var date                          : String? = null,
            @SerializedName("total_in_hours"                   ) var totalInHours                  : String? = null,
            @SerializedName("total_in_percentage"              ) var totalInPercentage             : Int?    = null,
            @SerializedName("online_timings"                   ) var onlineTimings                 : String? = null,
            @SerializedName("total_offline_hours"              ) var totalOfflineHours             : String? = null,
            @SerializedName("total_offline_percentage"         ) var totalOfflinePercentage        : Int?    = null,
            @SerializedName("offline_timings"                  ) var offlineTimings           : String? = null,
            @SerializedName("total_request_offsite_hours"      ) var totalRequestOffsiteHours      : String? = null,
            @SerializedName("total_request_offsite_percentage" ) var totalRequestOffsitePercentage : Int?    = null,
            @SerializedName("offsite_timings"                  ) var offsiteTimings                : String? = null,
            @SerializedName("total_remaining_hours"            ) var totalRemainingHours           : String? = null,
            @SerializedName("total_remaining_percentage"       ) var totalRemainingPercentage      : Int?    = null,
            @SerializedName("first_in_time"                    ) var firstInTime                   : String? = null,
            @SerializedName("last_out_time"                    ) var lastOutTime                   : String? = null,
            @SerializedName("request_offsite"                  ) var requestOffsite                : String? = null

        )

        data class PremiseDetails (

            @SerializedName("premise_id"   ) var premiseId   : Int?    = null,
            @SerializedName("premise_name" ) var premiseName : String? = null,
            @SerializedName("address"      ) var address     : String? = null,
            @SerializedName("city"         ) var city        : String? = null,
            @SerializedName("state"        ) var state       : String? = null,
            @SerializedName("country"      ) var country     : String? = null

        )

        data class UserDetails (

            @SerializedName("user_id"           ) var userId          : Int?    = null,
            @SerializedName("user_first_name"   ) var userFirstName   : String? = null,
            @SerializedName("user_last_name"    ) var userLastName    : String? = null,
            @SerializedName("user_image"        ) var userImage       : String? = null,
            @SerializedName("user_access_level" ) var userAccessLevel : String? = null,
            @SerializedName("user_status"       ) var userStatus      : String? = null

        )
    }
}