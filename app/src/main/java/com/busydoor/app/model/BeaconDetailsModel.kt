package com.busydoor.app.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName


@Keep
data class EmitterDetailsRespons (
    @SerializedName("status_code" ) var statusCode : Int?            = null,
    @SerializedName("message"     ) var message    : String?         = null,
    @SerializedName("data"        ) var data       : ArrayList<Data> = arrayListOf()
){

    @Keep
    data class Data (
        @SerializedName("emitters_id"             ) var beaconId            : Int?    = null,
        @SerializedName("emitters_device_id"      ) var beaconDeviceId      : String? = null,
        @SerializedName("emitters_device_address" ) var beaconDeviceAddress : String? = null,
        @SerializedName("namespace_id"          ) var nameSpaceId         : String? = null,
        @SerializedName("instance_id"           ) var instanceId          : String? = null,
        @SerializedName("premise_id"            ) var premiseId           : Int?    = null,
        @SerializedName("floor_number"          ) var floorNumber         : String? = null,
        @SerializedName("floor_name"            ) var floorName           : String? = null,
        @SerializedName("section_id"            ) var sectionId           : String? = null,
        @SerializedName("section_name"          ) var sectionName         : String? = null,
        @SerializedName("created_date"          ) var createdDate         : String? = null,
        @SerializedName("updated_date"          ) var updatedDate         : String? = null
    )

}
