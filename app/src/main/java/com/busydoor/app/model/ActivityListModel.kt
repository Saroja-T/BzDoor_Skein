package com.busydoor.app.model

import com.google.gson.annotations.SerializedName

data class UserActivities (

    @SerializedName("status_code" ) var statusCode : Int?    = null,
    @SerializedName("message"     ) var message    : String? = null,
    @SerializedName("data"        ) var data       : Data?   = Data()

){

    data class Data (

        @SerializedName("userdetails"       ) var userdetails       : Userdetails?                 = Userdetails(),
        @SerializedName("premisedetails"    ) var premisedetails    : Premisedetails?              = Premisedetails(),
        @SerializedName("activitiesdetails" ) var activitiesdetails : ArrayList<Activitiesdetails> = arrayListOf(),
        @SerializedName("offsitedetails"    ) var offsitedetails    : ArrayList<Offsitedetails>    = arrayListOf()

    ){
        data class Activitiesdetails (

            @SerializedName("notification_id" ) var notificationId : Int?    = null,
            @SerializedName("premise_id"      ) var premiseId      : Int?    = null,
            @SerializedName("user_id"         ) var userId         : Int?    = null,
            @SerializedName("datetime"        ) var datetime       : String? = null,
            @SerializedName("type"            ) var type           : String? = null,
            @SerializedName("title"           ) var title          : String? = null,
            @SerializedName("file_name"       ) var fileName       : String? = null,
            @SerializedName("message"         ) var message        : String? = null,
            @SerializedName("image"           ) var image          : String? = null,
            @SerializedName("url"             ) var url            : String? = null,
            @SerializedName("created_at"      ) var createdAt      : String? = null,
            @SerializedName("updated_at"      ) var updatedAt      : String? = null,
            @SerializedName("is_send"         ) var isSend         : String? = null

        )
        data class Offsitedetails (

            @SerializedName("staff_time_permission_id" ) var staffTimePermissionId : Int?    = null,
            @SerializedName("requester_type"           ) var requesterType         : String? = null,
            @SerializedName("requester_first_name"     ) var requesterFirstName    : String? = null,
            @SerializedName("requester_last_name"      ) var requesterLastName     : String? = null,
            @SerializedName("requester_photo"          ) var requesterPhoto        : String? = null,
            @SerializedName("time_permission_status"   ) var timePermissionStatus  : String? = null,
            @SerializedName("time_permission_reason"   ) var timePermissionReason  : String? = null,
            @SerializedName("requested_date"           ) var requestedDate         : String? = null,
            @SerializedName("requested_time"           ) var requestedTime         : String? = null,
            @SerializedName("start_date"               ) var startDate             : String? = null,
            @SerializedName("start_time"               ) var startTime             : String? = null,
            @SerializedName("end_date"                 ) var endDate               : String? = null,
            @SerializedName("end_time"                 ) var endTime               : String? = null,
            @SerializedName("comments_by_requester"    ) var commentsByRequester   : String? = null,
            @SerializedName("approver_first_name"      ) var approverFirstName     : String? = null,
            @SerializedName("approver_last_name"       ) var approverLastName      : String? = null,
            @SerializedName("approver_photo"           ) var approverPhoto         : String? = null,
            @SerializedName("comments_by_approver"     ) var commentsByApprover    : String? = null,
            @SerializedName("approved_date"            ) var approvedDate          : String? = null,
            @SerializedName("approved_time"            ) var approvedTime          : String? = null

        )
        data class Premisedetails (

            @SerializedName("premise_id"   ) var premiseId   : Int?    = null,
            @SerializedName("premise_name" ) var premiseName : String? = null,
            @SerializedName("address"      ) var address     : String? = null,
            @SerializedName("city"         ) var city        : String? = null,
            @SerializedName("state"        ) var state       : String? = null,
            @SerializedName("country"      ) var country     : String? = null

        )
        data class Userdetails (

            @SerializedName("user_id"         ) var userId        : Int?    = null,
            @SerializedName("user_first_name" ) var userFirstName : String? = null,
            @SerializedName("user_last_name"  ) var userLastName  : String? = null,
            @SerializedName("user_image"      ) var userImage     : String? = null,
            @SerializedName("user_access_level") var userAccessLevel : String? = null,
            @SerializedName("user_status"     ) var userStatus    : String? = null

        )
    }
}