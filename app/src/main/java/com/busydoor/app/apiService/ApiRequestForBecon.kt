package com.busydoor.app.apiService


import android.accounts.NetworkErrorException
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Intent
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.annotation.RequiresApi
import com.busydoor.app.activity.LoginHomeActivity
import com.busydoor.app.customMethods.PrefUtils
import com.busydoor.app.service.BDApplication
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.SocketTimeoutException
import java.text.ParseException
import java.util.concurrent.TimeoutException

/**
 * Created by Admin on 30-11-2023.
 */
@SuppressLint("ParcelCreator")
open class ApiRequestForBecon<T>(
    private val activity: Application? = null,
    objectType: T,
    private val TYPE: Int,
    private val apiResponseInterface: BDApplication
) : Callback<T>, Parcelable {
    private var retryCount = 0
    private var call: Call<T>? = null
    /** create var for the instance in bd-application... **/
    private lateinit var bdApplication: BDApplication
    protected lateinit var objSharedPref: PrefUtils

    init {
        call = objectType as Call<T>
        call!!.enqueue(this)
        /** assign the instance in bd-application... **/
        bdApplication = activity as BDApplication
    }



    @RequiresApi(Build.VERSION_CODES.R)
    override fun onResponse(call: Call<T>, response: Response<T>) {
        Log.e("Api Request", "REQUEST_URL = " + call.request().url().toString())
        Log.e("onResponse", "RESPONSE_CODE = " + response.code().toString())
        Log.e("REQUEST", "RESPONSE = $response")
        Log.e("REQUEST", "RESPONSE = " + response.body())

        if (response.isSuccessful) {
            Log.e("REQUEST", "RESPONSE = " + response.body())
            apiResponseInterface.getApiResponse(ApiResponseManager(response.body(), TYPE))
        } else {
            if(response.code()==404){
                Log.e("ApiRequestForBeacon", "Unable to connect Server")

            }else if(response.body()!=null){
                val error = ErrorUtils.parseError(response)
                Log.e("ApiRequestForBeacon==>", error.status().toString())
                if (error.status() == 401 || error.status() == 405) {
                    Log.e("ApiRequestForBeacon==>", error.status().toString())
                }
            }
            else if (response.code() == 401 || response.code()  == 405) {
                Log.e("error.status()==>", response.code().toString())

                if(activity!=null){
                    /*** login details here ***/
                    bdApplication.stopForegroundService()
                }
            }

        }
    }

    override fun onFailure(call: Call<T>, error: Throwable) {
        if (retryCount++ < TOTAL_RETRIES) {
            retry()
            return
        }

        when (error) {
            is NetworkErrorException -> {
                Log.e("ApiRequestForBeacon==>", error.toString())
            }
            is TimeoutException -> {
                Log.e("ApiRequestForBeacon==>", error.toString())
            }
            is SocketTimeoutException -> {
                Log.e("ApiRequestForBeacon==>", error.toString())
            }
            is ParseException -> {
                Log.e("ApiRequestForBeacon==>", error.toString())
            }
        }
    }

    private fun retry() {
        call!!.clone().enqueue(this)
    }

    companion object {
        private const val TAG = "ApiRequest"
        private const val TOTAL_RETRIES = 5
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(TYPE)
        parcel.writeInt(retryCount)
    }

    override fun describeContents(): Int {
        return 0
    }
}