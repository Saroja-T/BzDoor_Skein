package com.busydoor.app.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class UserDetails (

    @SerializedName("status_code" ) var statusCode : Int?    = null,
    @SerializedName("message"     ) var message    : String? = null,
    @SerializedName("data"        ) var data       : Data?   = Data()

) {
    @Keep
    data class Data (

        @SerializedName("user_id"             ) var userId            : Int?    = null,
        @SerializedName("first_name"          ) var firstName         : String? = null,
        @SerializedName("last_name"           ) var lastName          : String? = null,
        @SerializedName("phone_number"        ) var phoneNumber       : String? = null,
        @SerializedName("photo"               ) var photo             : String? = null,
        @SerializedName("original_image_name" ) var originalImageName : String? = null,
        @SerializedName("access_level"        ) var accessLevel       : String? = null

    )
}