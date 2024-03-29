package com.busydoor.app.model


import com.google.gson.annotations.SerializedName


data class PremiseUserList (

    @SerializedName("status_code" ) var statusCode : Int?    = null,
    @SerializedName("message"     ) var message    : String? = null,
    @SerializedName("data"        ) var data       : Data?   = Data()

){

    data class Data (

        @SerializedName("userdetails"    ) var userDetails    : Userdetails?            = Userdetails(),
        @SerializedName("premisedetails" ) var premiseDetails : Premisedetails?         = Premisedetails(),
        @SerializedName("staffdetails"   ) var staffdetails   : ArrayList<Staffdetails> = arrayListOf()

    ) {
        data class Premisedetails (

            @SerializedName("premise_id"   ) var premiseId   : Int?    = null,
            @SerializedName("premise_name" ) var premiseName : String? = null,
            @SerializedName("address"      ) var address     : String? = null,
            @SerializedName("city"         ) var city        : String? = null,
            @SerializedName("state"        ) var state       : String? = null,
            @SerializedName("country"      ) var country     : String? = null

        )
        data class Staffdetails (

            @SerializedName("user_id"            ) var userId           : Int?    = null,
            @SerializedName("first_name"         ) var firstName        : String? = null,
            @SerializedName("last_name"          ) var lastName         : String? = null,
            @SerializedName("user_active_status" ) var userActiveStatus : String? = null,
            @SerializedName("photo"              ) var photo            : String? = null,
            @SerializedName("access_level"       ) var accessLevel      : String? = null,
            @SerializedName("user_status"        ) var userStatus       : String? = null

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