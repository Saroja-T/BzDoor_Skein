package com.busydoor.app.apiService
import android.accounts.NetworkErrorException
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDialog
import com.busydoor.app.R
import com.busydoor.app.activity.LoginHomeActivity
import com.busydoor.app.customMethods.PrefUtils
import com.busydoor.app.customMethods.Utils.dismissDialog
import com.busydoor.app.customMethods.getProgressDialog
import com.busydoor.app.customMethods.toast
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
open class ApiRequest<T>(
    private val activity: Activity? = null,
    objectType: T,
    private val TYPE: Int,
    private val isShowProgressDialog: Boolean,
    private val apiResponseInterface: ApiResponseInterface
) : Callback<T>, Parcelable {

    private var mProgressDialog: AppCompatDialog? = null
    private var retryCount = 0
    private var call: Call<T>? = null

    protected lateinit var objSharedPref: PrefUtils
    /** create var for the instance in bd-application... **/
    private lateinit var bdApplication: BDApplication

    init {
        showProgress()
        call = objectType as Call<T>
        call!!.enqueue(this)
        /** assign the instance in bd-application... **/
        bdApplication = activity!!.application as BDApplication

    }

    private fun showProgress() {
        if (isShowProgressDialog) {
            if (mProgressDialog == null)
                mProgressDialog = activity?.let { getProgressDialog(it) }
        }
    }

    private fun dismissProgress() {
        if (isShowProgressDialog && activity!=null) {
            dismissDialog(activity, mProgressDialog!!)
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onResponse(call: Call<T>, response: Response<T>) {
        dismissProgress()
        Log.e("Api Request", "REQUEST_URL = " + call.request().url().toString())
        Log.e("onResponse", "RESPONSE_CODE = " + response.code().toString())
        Log.e("REQUEST", "RESPONSE = $response")
        Log.e("REQUEST", "RESPONSE = " + response.body())

        if (response.isSuccessful) {
            Log.e("REQUEST", "RESPONSE = " + response.body())
            apiResponseInterface.getApiResponse(ApiResponseManager(response.body(), TYPE))
        } else {
            if(response.code()==404){
                if(activity!=null) {
                    toast("Unable to connect Server", true, activity)
                }

            }else if(response.body()!=null){
                val error = ErrorUtils.parseError(response)
                if(activity!=null) {
                    toast(error.message(), true, activity)
                }
                Log.e("error.status()==>", "zzzzzzz"+error.status().toString())
                if (error.status() == 401 || error.status() == 405) {
                    Log.e("error.status()==>","mmmmmmm"+ response.code().toString())

                    if(activity!=null){
                        PrefUtils(activity!!).putBoolean(
                            activity.getString(R.string.isLogin), false
                        )
                        /*** login details here ***/
                        val intent = Intent(activity, LoginHomeActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        activity.startActivity(intent)
                        activity.finishAffinity()
                    }

                }
            }
            else if (response.code() == 401 || response.code()  == 405) {
                Log.e("error.status()==>","nnnnnn"+ response.code().toString())

                if(activity!=null){
                    PrefUtils(activity!!).putBoolean(
                        activity.getString(R.string.isLogin), false
                    )
                    /*** login details here ***/
                    val intent = Intent(activity, LoginHomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    bdApplication.stopForegroundService()
                    activity.startActivity(intent)
                    activity.finishAffinity()
                }

            }

        }
    }

    override fun onFailure(call: Call<T>, error: Throwable) {
        if (retryCount++ < TOTAL_RETRIES) {
            retry()
            return
        }

        dismissProgress()
        when (error) {
            is NetworkErrorException -> {
                if(activity!=null)
                    toast(
                        activity.resources.getString(R.string.networkError),
                        true,
                        activity
                    )
            }
            is TimeoutException -> {
                if(activity!=null)
                    toast(
                        activity.resources.getString(R.string.networkError),
                        false,
                        activity
                    )
            }
            is SocketTimeoutException -> {
                if(activity!=null)
                    toast(
                        ("Try after sometime"),
                        false,
                        activity
                    )
            }
            is ParseException -> {
                if(activity!=null)
                    toast(
                        "Something went wrong..",
                        false,
                        activity
                    )
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
        parcel.writeByte(if (isShowProgressDialog) 1 else 0)
        parcel.writeInt(retryCount)
    }

    override fun describeContents(): Int {
        return 0
    }
}