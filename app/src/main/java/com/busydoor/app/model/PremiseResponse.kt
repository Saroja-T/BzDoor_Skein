package com.busydoor.app.model


import com.google.gson.annotations.SerializedName

import androidx.annotation.Keep

@Keep
data class PremiseResponse(
    @SerializedName("status_code")
    val statusCode: Int, // 200
    @SerializedName("message")
    val message: String, // get home page data
    @SerializedName("data")
    val `data`: List<Data>
) {
    @Keep
    data class Data(
        @SerializedName("premise_id")
        val premiseId: Int, // 1
        @SerializedName("data_availabel")
        val data_availabel: String, // 1
        @SerializedName("token_id")
        val tokenId: String, // 45bcc085-7403-45ff-a1c4-8a3170c07067
        @SerializedName("date_time_recorded")
        val dateTimeRecorded: String, // 2021-04-01 00:00:00
        @SerializedName("title")
        val title: String, // Showroom
        @SerializedName("name")
        val name: String, // ad
        @SerializedName("sub_name")
        val subName: String, // asd
        @SerializedName("address")
        val address: String, // asd
        @SerializedName("country")
        val country: String, // asd
        @SerializedName("state")
        val state: String, // das
        @SerializedName("city")
        val city: String, // sadasd
        @SerializedName("photo")
        val photo: String, // asdasdad
        @SerializedName("reserved")
        val reserved: String, // asdasd
        @SerializedName("created_at")
        val createdAt: String,
        @SerializedName("updated_at")
        val updatedAt: String,
        @SerializedName("staff_status")
        val premiseStatus: String,
        @SerializedName("getpremisecurrentstatus")
        val getpremisecurrentstatus: Getpremisecurrentstatus
    ) {
        @Keep
        data class Getpremisecurrentstatus(
            @SerializedName("premise_current_status_id")
            val premiseCurrentStatusId: Int, // 1
            @SerializedName("premise_id")
            val premiseId: Int, // 1
            @SerializedName("date_time_recorded")
            val dateTimeRecorded: String, // 2021-04-12 12:12:12
            @SerializedName("status")
            val status: String, // Open
            @SerializedName("last_updated")
            val lastUpdated: String, // 2021-04-14 12:00:18
            @SerializedName("image_filename")
            val imageFilename: String, // F:\archieve\45bcc085-7403-45ff-a1c4-8a3170c07067\CurrentStatus\OpenClose_.png
            @SerializedName("created_at")
            val createdAt: String, // 2021-04-14 12:00:18
            @SerializedName("updated_at")
            val updatedAt: String // 2021-04-14 12:00:18
        )
    }
}