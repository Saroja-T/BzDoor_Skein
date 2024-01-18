package com.busydoor.app.apiService

import com.busydoor.app.model.AcceptOffsiteRes
import com.busydoor.app.model.AddUserToPremise
import com.busydoor.app.model.AttendanceResponse
import com.busydoor.app.model.EmitterDetailsRespons
import com.busydoor.app.model.HomeDataResponse
import com.busydoor.app.model.PremiseResponse
import com.busydoor.app.model.PremiseUserList
import com.busydoor.app.model.SendRequestOffsiteResponse
import com.busydoor.app.model.RequestAllOffsiteResponse
import com.busydoor.app.model.StaffCountResponse
import com.busydoor.app.model.StaffGraphcount
import com.busydoor.app.model.StaffListOnDate
import com.busydoor.app.model.UserActivities
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

/**
 * Created by Admin on 30-11-2023.
 */
interface ApiInterface {

    // Function for user login in the application
    @FormUrlEncoded
    @Headers("Accept:application/json")
    @POST("user/login")
    fun userLoginFunction(
        @Field("phone_number") first_name: String,
        @Field("device_type") device_type: String,
        @Field("fcm_token") device_token: String,
        @Field("time_zone") time_zone: String,
    ): Call<ResponseBody>
    @FormUrlEncoded
    @Headers("Accept:application/json")
    @PUT("user/updateuserstatus")
    fun updateUserStatus(
        @Header("Authorization") inToken: String,
        @Field("status") status: String,
        @Field("premise_id") premiseId: String,
        @Field("user_id") userId: String
    ): Call<AddUserToPremise>

    // Function for registering a user in the application
    @FormUrlEncoded
    @Headers("Accept:application.json")
    @POST("user/register")
    fun userRegisterFunction(
        @Field("phone_number") phone_number: String,
        @Field("first_name") first_name: String,
        @Field("last_name") last_name: String,
        @Field("access_level") access_level: String,
        @Field("device_type") device_type: String,
        @Field("fcm_token") device_token: String,
        @Field("time_zone") time_zone: String,
        @Field("type") type: String,
    ): Call<ResponseBody>

    // Function to get home data
    @Headers("Accept:application/json")
    @GET("user/home/data")
    fun homeDataGet(
        @Header("Authorization") inToken: String,
    ): Call<PremiseResponse>

//    // Function to retrieve user activity data
//    @FormUrlEncoded
//    @Headers("Accept:application/json")
//    @POST("user/activities")
//    fun myActivitiesData(
//        @Header("Authorization") inToken: String?= null,
//        @Field("date") date: String? =null,
//        @Field("premise_id") premiseId: String? =null,
//    ): Call<ActivityListModel>
//
//     Function to get user premise data
    @Headers("Accept:application/json")
    @GET("user/getDashboardData")
    fun userPremiseData(
        @Header("Authorization") inToken: String,
        @Query("premise_id") premise_id: String,
        @Query("date") date: String
    ): Call<HomeDataResponse>

//
//    // Function to get bar graph data details
//    @FormUrlEncoded
//    @Headers("Accept:application/json")
//    @POST("user/premise/bardetails")
//    fun statusGraphDetails(
//        @Header("Authorization") inToken: String,
//        @Field("startdate") startdate: String,
//        @Field("enddate") enddate: String,
//        @Field("id") id: String,
//    ): Call<StatusGraphData>
//
    // Function to contact customer support
    @FormUrlEncoded
    @Headers("Accept:application/json")
    @POST("contact/us")
    fun contactUs(
        @Header("Authorization") inToken: String,
        @Field("name") name: String,
        @Field("phone") phone: String,
        @Field("email") email: String,
        @Field("category") category: String,
        @Field("message") message: String,
    ): Call<ResponseBody>
//
//    // Function to get user staff details data
//    @FormUrlEncoded
//    @Headers("Accept:application/json")
//    @POST("user/staff/details")
//    fun userStaffDetailsData(
//        @Header("Authorization") inToken: String,
//        @Field("staff_id") id: String,
//        @Field("premise_id") premiseId: String,
//        @Field("startdate") startdate: String,
//        @Field("enddate") enddate: String,
//    ): Call<StaffDetailsGraphData>
//
//    // Function to get user configuration data
//    @FormUrlEncoded
//    @Headers("Accept:application/json")
//    @POST("user/userconfig")
//    fun userConfigData(
//        @Header("Authorization") inToken: String,
//        @Field("user_id") id: String,
//        @Field("premise_id") premiseId: String,
//        @Field("date") date: String,
//        @Field("toggle") toggle: String,
//        @Field("targetstaff") targetStaff: String,
//        @Field("targetcust") targetCustomer: String,
//        @Field("targetknown") targetKnown: String,
//    ): Call<UserConfigData>
//
//
//
//
//    // Function to get user access data
//    @FormUrlEncoded
//    @Headers("Accept:application/json")
//    @POST("user/useraccess")
//    fun userAccess(
//        // @Header("Authorization") inToken: String,
//        @Field("user_id") id: String,
//    ): Call<UserAccessData>
//
//    // Function to get user lists
//    @Headers("Accept:application/json")
//    @GET("user/userPremiseLinkData")
//    fun getUserLists(
//        @Header("Authorization") inToken: String,
//    ): Call<UserListData>
//
//    // Function to get user lists by premise ID
//    @FormUrlEncoded
//    @Headers("Accept:application/json")
//    @POST("user/getuserPremiseLink")
//    fun getUserListsByPremiseId(
//        @Header("Authorization") inToken: String,
//        @Field("premise_id") id: String,
//    ): Call<UserListData>
//
    // Function to create a user
    @FormUrlEncoded
    @Headers("Accept:application/json")
    @POST("user/register")
    fun createUser(
        @Header("Authorization") inToken: String,
        @Field("phone_number") phone_number: String,
        @Field("first_name") first_name: String,
        @Field("last_name") last_name: String,
        @Field("access_level") access_level: String,
        @Field("type") type: String,
        @Field("premise_id") id: String,
        @Field("status") userStatus: String,
    ): Call<ResponseBody>
//
//    // Function to update a user
//    @FormUrlEncoded
//    @Headers("Accept:application/json")
//    @PUT("user/updateUser")
//    fun updateUser(
//        @Header("Authorization") inToken: String,
//        @Field("user_id") id: String,
//        @Field("status") status: String,
//        @Field("premise_id") premiseId: String,
//    ): Call<ResponseBody>
//
    // Function to link a user to a premise
    @FormUrlEncoded
    @Headers("Accept:application/json")
    @POST("user/userPremiseLink")
    fun userPremiseLink(
        @Header("Authorization") inToken: String,
        @Field("phone_number") phone_number: String,
        @Field("premise_id") premise_id: String,
        @Field("status") status: String,
        @Field("type") type: String,
    ): Call<ResponseBody>
//
    // Function to add attendance
    @FormUrlEncoded
    @Headers("Accept:application/json")
    @POST("user/postRequestOffsite")
    fun sendOffsiteRequest(
        @Header("Authorization") inToken: String,
        @Field("premise_id") premiseId: String,
        @Field("date") date: String,
        @Field("start_time") startTime: String,
        @Field("end_time") endTime: String,
        @Field("comments") comments: String,
        @Field("time_permission_reason") requestType: String,
    ): Call<SendRequestOffsiteResponse>

    @Headers("Accept:application/json")
    @GET("user/getRequestOffsite")
    fun getRequestOffsiteDetails(
        @Header("Authorization") inToken: String,
        @Query("premise_id") premiseId: String,
        @Query("date") date: String,
    ): Call<RequestAllOffsiteResponse>

    @Headers("Accept:application/json")
    @GET("user/getuseractivitiesoffsitelist")
    fun getYourActivitiesList(
        @Header("Authorization") inToken: String,
        @Query("premise_id") premiseId: String,
        @Query("date") date: String,
    ): Call<UserActivities>

    @FormUrlEncoded
    @Headers("Accept:application/json")
    @PUT("user/approverejectoffsite")
    fun setPermissionoffsite(
        @Header("Authorization") inToken: String,
        @Field("staff_time_permission_id") permissionId: String,
        @Field("time_permission_status") permissionStatus: String,
        @Field("comments") comments: String,
    ): Call<AcceptOffsiteRes>

    @Headers("Accept:application/json")
    @GET("user/getstaffcount")
    fun getStaffCount(
        @Header("Authorization") inToken: String,
        @Query("premise_id") premiseId: String,
        @Query("date") date: String,
    ): Call<StaffCountResponse>
    @Headers("Accept:application/json")
    @GET("user/getstaffdayspresentcount")
    fun getStaffGraphpresentCount(
        @Header("Authorization") inToken: String,
        @Query("premise_id") premiseId: String,
        @Query("enddate") date: String,
    ): Call<StaffGraphcount>
    @Headers("Accept:application/json")
    @GET("user/getstaffdetailondate")
    fun getStaffDetailOnDate(
        @Header("Authorization") inToken: String,
        @Query("premise_id") premiseId: String,
        @Query("date") date: String,
    ): Call<StaffListOnDate>

    @Headers("Accept:application/json")
    @GET("user/getstaffdetailsbypremise")
    fun getStaffListBypremise(
        @Header("Authorization") inToken: String,
        @Query("premise_id") premiseId: String,
    ): Call<PremiseUserList>

    // Function to get beacon details
    @Headers("Accept:application/json")
    @GET("user/getemittersdetails")
    fun getEmitterDetails(
        @Header("Authorization") inToken: String,
    ): Call<EmitterDetailsRespons>

    // Function to log staff Bluetooth data
    @FormUrlEncoded
    @Headers("Accept:application/json")
    @POST("user/staffbluetoothlog")
    fun staffBluetoothLog(
    @Header("Authorization") inToken: String,
    @Field("emitters_device_id") beacon_device_id: String,
    @Field("distance") distance: String,
    @Field("reserved") reserved: String,
    @Field("status") status: String,
    @Field("device_type") deviceType: String
    ): Call<AttendanceResponse>

    // Function to update static out data
    @FormUrlEncoded
    @Headers("Accept:application/json")
    @PUT("user/Updatestaticout")
    fun updatestaticout(
        @Header("Authorization") inToken: String,
        @Field("status") status: String,
        @Field("device_type") deviceType: String
    ): Call<AttendanceResponse>


}
