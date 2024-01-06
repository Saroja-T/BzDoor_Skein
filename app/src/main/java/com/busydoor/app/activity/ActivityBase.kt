package com.busydoor.app.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatTextView
import com.busydoor.app.R
import com.busydoor.app.customMethods.gContext
import com.busydoor.app.customMethods.PrefUtils
import com.busydoor.app.customMethods.dismissProgressDialog
import com.busydoor.app.customMethods.encode
import com.busydoor.app.customMethods.getProgressDialog
import com.busydoor.app.model.UserModel
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import java.util.*


abstract class
ActivityBase : AppCompatActivity() {
    var timeZoneSet: String = ""
    private lateinit var mActivity: Activity
    open lateinit var objSharedPref: PrefUtils
    val TAG: String = javaClass.simpleName
    private var auth_token = ""
    private var user_id = ""
    private var mProgressDialog: AppCompatDialog? = null

    /*Encryption variables*/
    var cryptLib: CryptLib2? = null
    val key = "12345678901234561234567890123456"
    private val ENCRYPTION_IV = "12345678901234561234567890123456"


    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var globalActivity: Activity
    }

    private fun initBaseComponants(activityBase: ActivityBase) {
        mActivity = this@ActivityBase
        globalActivity = this@ActivityBase
        cryptLib = CryptLib2()
        getFirebaseMessagingToken()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        Fresco.initialize(this@ActivityBase)
        objSharedPref = PrefUtils(this@ActivityBase)
        initBaseComponants(this)
        initBaseData()
        try {
            val tz: TimeZone = TimeZone.getDefault()
            Log.e(
                "TAG", "TIME ZONE " + "TimeZone   " + tz.getDisplayName(false, TimeZone.SHORT)
                    .toString() + " Timezon id :: " + tz.id
            )
            println(
                "TimeZone   " + tz.getDisplayName(false, TimeZone.SHORT)
                    .toString() + " Timezon id :: " + tz.id
            )
            timeZoneSet = tz.id
        } catch (_: Exception) {

        }
    }

    fun appClose() {
        AlertDialog.Builder(this@ActivityBase)
            .setTitle("Exit")
            .setMessage("Are you sure you want to exit this app ?") // Specifying a listener allows you to take an action before dismissing the dialog.
            // The dialog is automatically dismissed when a dialog button is clicked.
            .setPositiveButton("Yes"
            ) { dialog, which ->
                finishAffinity()
                dialog.dismiss()
            } // A null listener allows the button to dismiss the dialog and take no further action.
            .setNegativeButton("No"
            ) { dialog, which ->
                // Continue with delete operation
                dialog.dismiss()
            }
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun logout(title:String,content:String) {
        val builder = android.app.AlertDialog.Builder(this)
        //set title for alert dialog
        builder.setTitle(title)
        //set message for alert dialog
        builder.setMessage(content)
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        //performing positive action
        builder.setPositiveButton("Yes") { _, _ ->
            //performing negative action
        }
        builder.setNegativeButton("No"){ dialog, _ ->
            dialog!!.dismiss()
        }
        // Create the AlertDialog
        val alertDialog: android.app.AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    open fun getFirebaseMessagingToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task: Task<String?> ->
                if (!task.isSuccessful) {
                    //Could not get FirebaseMessagingToken
                    return@addOnCompleteListener
                }
                if (null != task.result) {
                    //Got FirebaseMessagingToken
                    val token = task.result
                    Log.e(TAG, "FCM_TOKEN token===> $token")
                    Log.e(TAG, "FCM_TOKEN ===> $token")
                    objSharedPref.putString("FCM_TOKEN", token!!)
                    //   val firebaseMessagingToken: String = Objects.requireNonNull(task.result).toString()
                    //   Use firebaseMessagingToken further
                }
            }
    }

    /*encrypt editext value*/
    open fun encrypt(value: String): String {
        return if (TextUtils.isEmpty(value)) {
            value.replace('\"', ' ', ignoreCase = false)
        } else {
            val values = cryptLib!!.encrypt(value, key, ENCRYPTION_IV)
            Log.e(
                TAG,
                "original value == $value " +
                        "encrypted message ==> ${
                            values!!.trimEnd().encode()
                        }"
            )
            values.trimEnd().encode()
        }
    }


    private fun initBaseData() {

        //auth_token = "Bearer " + objSharedPref.getString(resources.getString(R.string.key_token))
        // user_id = objSharedPref.getString(resources.getString(R.string.key_user_id))!!
        Log.e("auth_token", "auth_token = $auth_token")
        Log.e("user_id", "user_id = $user_id")

        try {

            Log.e(TAG,"Token is"+getUserModel()!!.data.token)
        } catch (_: Exception) {

        }

    }

    fun showProgress() {
        if (mProgressDialog == null)
            mProgressDialog = getProgressDialog(gContext)
    }

    fun dismissProgress() {
        if (mProgressDialog != null) {
            dismissProgressDialog(mProgressDialog!!)
        }
    }

    protected fun showMessage(message: String) {
        Toast.makeText(this@ActivityBase, message, Toast.LENGTH_LONG).show()
    }

    protected fun showSnackBar(view: View, message: String, action: ACTIONSNACKBAR) {
        val snackBar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
        snackBar.view.setBackgroundColor(Color.DKGRAY)


        val tv = snackBar.view.findViewById(com.google.android.material.R.id.snackbar_text) as AppCompatTextView
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13f)
        tv.setTextColor(Color.WHITE)
        tv.maxLines = 25

        val snackBaraction = snackBar.view.findViewById(com.google.android.material.R.id.snackbar_action) as AppCompatButton
        snackBaraction.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13f)


        snackBar.setAction(action.actionMessage) {
            when (action.actionMessage) {
                ACTIONSNACKBAR.DISMISS.actionMessage -> {
                    snackBar.dismiss()
                }

                ACTIONSNACKBAR.FINISH_ACTIVITY.actionMessage -> {
                    finish()
                }
            }
        }
        snackBar.setActionTextColor(Color.WHITE)
        snackBar.show()
    }

    protected fun setCheckBoxPadding(checkBox: AppCompatCheckBox) {
        val scale = resources.displayMetrics.density
        checkBox.setPadding(
            checkBox.paddingLeft + (6.0f * scale + 0.5f).toInt(),
            checkBox.paddingTop,
            checkBox.paddingRight,
            checkBox.paddingBottom
        )
    }

    fun getUserModel(): UserModel? {
        val gson = Gson()
        return gson.fromJson(
            objSharedPref.getString(getString(R.string.userResponse))!!, UserModel::class.java
        )
    }

    enum class ACTIONSNACKBAR(val actionMessage: String) {
        DISMISS(globalActivity.getString(R.string.dismiss)),
        FINISH_ACTIVITY(globalActivity.getString(R.string.done)),
        NONE(""),

    }

    protected fun goScreen(activityName: Activity) {
        startActivity(Intent(this@ActivityBase, activityName::class.java))
    }


    fun printDebugLogs(tagClass: AppCompatActivity, printValue: String) {
        /*  if (BuildConfig.DEBUG)*/
        Log.e(tagClass.localClassName, printValue)
    }
}