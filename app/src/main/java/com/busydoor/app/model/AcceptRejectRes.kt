package com.busydoor.app.model

import com.google.gson.annotations.SerializedName

data class AcceptOffsiteRes (

    @SerializedName("status_code" ) var statusCode : Int?    = null,
    @SerializedName("message"     ) var message    : String? = null,
    @SerializedName("data"        ) var data       : Data?   = Data()

){
    data class Data (

        @SerializedName("staff_time_permission_id" ) var staffTimePermissionId : Int?    = null,
        @SerializedName("premise_id"               ) var premiseId             : Int?    = null,
        @SerializedName("staff_id"                 ) var staffId               : Int?    = null,
        @SerializedName("user_id"                  ) var userId                : Int?    = null,
        @SerializedName("start_date_time"          ) var startDateTime         : String? = null,
        @SerializedName("end_date_time"            ) var endDateTime           : String? = null,
        @SerializedName("totminutes"               ) var totminutes            : Int?    = null,
        @SerializedName("time_permission_reason"   ) var timePermissionReason  : String? = null,
        @SerializedName("time_permission_status"   ) var timePermissionStatus  : String? = null,
        @SerializedName("approved_by"              ) var approvedBy            : Int?    = null,
        @SerializedName("approved_date_time"       ) var approvedDateTime      : String? = null,
        @SerializedName("comments_by_requester"    ) var commentsByRequester   : String? = null,
        @SerializedName("comments_by_approver"     ) var commentsByApprover    : String? = null,
        @SerializedName("created_at"               ) var createdAt             : String? = null,
        @SerializedName("updated_at"               ) var updatedAt             : String? = null

    )
}