package com.busydoor.app.model

import com.google.gson.annotations.SerializedName


data class RequestAllOffsiteResponse (
    @SerializedName("status_code" ) var statusCode : Int?            = null,
    @SerializedName("message"     ) var message    : String?         = null,
    @SerializedName("data"        ) var data       : List<Data>

) {
    data class Data (

        @SerializedName("requester_first_name"   ) var requesterFirstName   : String? = null,
        @SerializedName("requester_last_name"    ) var requesterLastName    : String? = null,
        @SerializedName("time_permission_status" ) var timePermissionStatus : String? = null,
        @SerializedName("time_permission_reason" ) var timePermissionReason : String? = null,
        @SerializedName("requested_date"         ) var requestedDate        : String? = null,
        @SerializedName("requested_time"         ) var requestedTime        : String? = null,
        @SerializedName("start_date"             ) var startDate            : String? = null,
        @SerializedName("start_time"             ) var startTime            : String? = null,
        @SerializedName("end_date"               ) var endDate              : String? = null,
        @SerializedName("end_time"               ) var endTime              : String? = null,
        @SerializedName("comments_by_requester"  ) var commentsByRequester  : String? = null,
        @SerializedName("approver_first_name"      ) var approverFirstName     : String? = null,
        @SerializedName("approver_last_name"       ) var approverLastName      : String? = null,
        @SerializedName("comments_by_approver"   ) var commentsByApprover   : String? = null,
        @SerializedName("approved_date"          ) var approvedDate         : String? = null,
        @SerializedName("approved_time"          ) var approvedTime         : String? = null

    )
}