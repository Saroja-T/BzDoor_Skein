package com.busydoor.app.model


import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class UserModel(
    @SerializedName("status_code")
    val statusCode: Int, // 200
    @SerializedName("message")
    val message: String, // Logged In Successfully
    @SerializedName("data")
    val `data`: Data
) {
    @Keep
    data class Data(
        @SerializedName("user_id")
        val userId: Int, // 2
        @SerializedName("first_name")
        val firstName: String, // Dhaval
        @SerializedName("last_name")
        val lastName: String, // Dhaval
        @SerializedName("phone_number")
        val phoneNumber: String, // 9979639481
        @SerializedName("token_id")
        val tokenId: String,
        @SerializedName("access_level")
        val accessLevel: String, // Manager
        @SerializedName("logged_in_status")
        val loggedInStatus: String, // Login
        @SerializedName("active")
        val active: String, // True
        @SerializedName("reserved")
        val reserved: String,
        @SerializedName("device_token")
        val deviceToken: String,
        @SerializedName("is_block")
        val isBlock: String, // 0
        @SerializedName("is_admin")
        val isAdmin: String, // 0
        @SerializedName("is_delete")
        val isDelete: String, // 0
        @SerializedName("device_type")
        val deviceType: String,
        @SerializedName("user_type")
        val userType: String, // User
        @SerializedName("login_count")
        val loginCount: Int, // 2
        @SerializedName("last_login_time")
        val lastLoginTime: String, // 2021-04-26 09:22:00
        @SerializedName("created_at")
        val createdAt: String, // 2021-04-26 07:03:18
        @SerializedName("updated_at")
        val updatedAt: String, // 2021-04-26 09:22:00
        @SerializedName("time_zone")
        val timeZone: String,
        @SerializedName("token")
        val token: String // eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImp0aSI6ImVhZmU3ZjI2ZTMzY2U4MjdkZWY4ZDUwY2JlZGU5ZmE3ZTFjZGNjMDE0ZjFlNmM1M2RlZDIzNWIyYmRiOWZkZDljMzc2ZTM4MWJlMDY2ODVmIn0.eyJhdWQiOiIxIiwianRpIjoiZWFmZTdmMjZlMzNjZTgyN2RlZjhkNTBjYmVkZTlmYTdlMWNkY2MwMTRmMWU2YzUzZGVkMjM1YjJiZGI5ZmRkOWMzNzZlMzgxYmUwNjY4NWYiLCJpYXQiOjE2MTk0Mjg5MTksIm5iZiI6MTYxOTQyODkxOSwiZXhwIjoxNjUwOTY0OTE5LCJzdWIiOiIyIiwic2NvcGVzIjpbXX0.ZMfDQCYEIcGgtNOVpyY_XvbYnDRJYLQpreL5R2jR2a7aqygILiECLWf-ag1GS1Z-34A63LJsaMJuDwLZsAuAUqvlNH6IoY20k1bcDRjm6O10OJ1Z8D4x1oNZc_NmB2mvi5C5f7RKsh7VehX7-EzCdumcixAHB3tpWOtEiQ1RPqmJunuIOfQDEQRiV5FCz4l0VLUfy3K_h8iKvgVOEY8DoWBbiAC7MS9RHAeSRJZk6DMaOk9oWVpGLnh8IpqdEPR1tbUbigPvJdwJa_qsuPZIG_fZ2mlksPgiU5x6_Jx6XkB-I_ol0EXkdEnSvwgfkMt92lOZ5iOaJ05AM2k_Rs5hBgDOb-FkH2p1_uKnvduJJomANXpI8w6uYBCH2UDo33A5plODSfGaOP0Hk8hZlty5gfio3yjgGOlupGLWKWrtVft6yjQ-L8QB_hoJL7PWV_ACMHmWFZY8SO4AId5lWVV3IJgzOkpbBWBdnOKhOaBB0oeEbeOiayoBf9v2iaNJbfLpHwO1_8t-NuioDwyjiZkJllvCf580Ar8SkcOWq4WLzsbpAH38Omtehyhj8uh-lekZR8vSl9bnzckm7zwXtXFhK329WCVZsaSk3eMT1Ql5EH52X2xgHWMtMNs29OruHYPyLD2arwW0uWtS-LePRTHjvf-0wZHDz47VDOprHXgJCJw
    )
}