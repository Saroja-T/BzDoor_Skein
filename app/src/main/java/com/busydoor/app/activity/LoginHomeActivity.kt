package com.busydoor.app.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.busydoor.app.R
import com.busydoor.app.apiService.*
import com.busydoor.app.customMethods.*
import com.busydoor.app.databinding.ActivityLoginBinding
import com.google.firebase.auth.*
import okhttp3.ResponseBody
import org.json.JSONObject


class LoginHomeActivity : ActivityBase(),ApiResponseInterface {
    private var mAuth: FirebaseAuth? = null
    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }

    /** Main Function... **/
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupUI(binding.root)
        gContext = this@LoginHomeActivity
        activity = this@LoginHomeActivity

        /** firebase Initialize here... **/
        mAuth = FirebaseAuth.getInstance()

        binding.btnSignIn.setOnClickListener {
            if (binding.phoneNumber.text?.length==10){
                /** call loginApi here... **/
                loginApi()
            }else{
                binding.phoneNumber.error="Please enter Valid number"
            }
        }

        binding.textSignUp.setOnClickListener {
            val registerActivity = Intent(this@LoginHomeActivity, RegisterActivity::class.java)
            startActivity(registerActivity)
            finish()
        }
        objSharedPref.putString("deviceId",getAndroidId(this))
    }

    private fun getAndroidId(context: Context): String {
        Log.e("getAndroidId",
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID).toString())
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }
    /** loginApi function... **/
    @RequiresApi(Build.VERSION_CODES.R)
    private fun loginApi() {
        if (isOnline(this@LoginHomeActivity)) {
            ApiRequest(
                this,
                ApiInitialize.initialize(ApiInitialize.LOCAL_URL).userLoginFunction(
                    encrypt(binding.phoneNumber.text.toString()),
                    encrypt(DEVICE_TYPE),
                    objSharedPref.getString("FCM_TOKEN").toString(),
                    encrypt(timeZoneSet),
                    objSharedPref.getString("deviceId")!!.toString()
                ),
                LOGIN, true, this
            )
        } else {
            showSnackBar(
                binding.root,
                getString(R.string.noInternet),
                ACTIONSNACKBAR.DISMISS
            )
        }
    }

    /** sendVerificationCode fun... **/
    private fun sendVerificationCode(number: String) {
        //this method is used for getting OTP on user phone number.
        PhoneAuthUtil.sendVerificationCode(
            this,
            this@LoginHomeActivity,
            number, {
                // Success callback, navigate to the next activity or perform other actions
                forceResendingTokenGbl = PhoneAuthUtil.getForceResendingToken()
                val intent = Intent(this@LoginHomeActivity, OtpVerifyActivity::class.java)
                // Pass necessary data to the next activity
                intent.putExtra("verificationId", PhoneAuthUtil.getVerificationId())
                intent.putExtra("phone_number", binding.phoneNumber.text.toString())
                intent.putExtra("otp_type","Login")
                startActivity(intent)
                finish()
            },
            { errorMessage ->
                // Error callback, show a message or handle the error accordingly
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            }
        )
        dismissProgress()
    }


    @RequiresApi(Build.VERSION_CODES.R)
    private fun forceLogOutApi() {
        if (isOnline(this@LoginHomeActivity)) {
            ApiRequest(
                this,
                ApiInitialize.initialize(ApiInitialize.LOCAL_URL).forceLogout(
                    encrypt(binding.phoneNumber.text.toString()),
                ),
                FORCE_LOGOUT, true, this
            )
        } else {
            showSnackBar(
                binding.root,
                getString(R.string.noInternet),
                ACTIONSNACKBAR.DISMISS
            )
        }

    }

    /** getApiResponse function... **/
    @RequiresApi(Build.VERSION_CODES.R)
    override fun getApiResponse(apiResponseManager: ApiResponseManager<*>) {
        when (apiResponseManager.type) {
            LOGIN -> {
                val model = apiResponseManager.response as ResponseBody
                val responseValue = model.string()
                val response = JSONObject(responseValue)
                when (response.optInt("status_code")) {
                    SUCCESS_CODE -> {
                        Log.d(TAG, "onCreate: $response")
                        objSharedPref.putString(getString(R.string.userResponse), responseValue)
                        val phone = "+91" + binding.phoneNumber.text.toString()
                        showProgress()
                        sendVerificationCode(phone)
                    }
                    ERROR_CODE -> {
                        val data = response.getJSONObject("data")
                        showSnackBar(
                            binding.root,
                            data.optString("all"),
                            ACTIONSNACKBAR.DISMISS
                        )
                    }
                    403 -> {
                        showAlertBox("logout","Are sure want to logout from the other devices?")
                    }
                }
            }

            FORCE_LOGOUT -> {
                val model = apiResponseManager.response as ResponseBody
                val responseValue = model.string()
                val response = JSONObject(responseValue)
                when (response.optInt("status_code")) {
                    SUCCESS_CODE ->{
                        Toast.makeText(this, response.optString("message"), Toast.LENGTH_LONG).show()
                    }
                    ERROR_CODE ->{
                        Toast.makeText(this,response.optString("message"), Toast.LENGTH_LONG).show()
                    }
                }

            }


        }
    }


    /** Showing the error response ... **/
    @RequiresApi(Build.VERSION_CODES.R)
    private fun showAlertBox(type: String, reason: String){
        val dialog = AlertDialog.Builder(this)
        val view: View = layoutInflater.inflate(R.layout.custom_alert_dialog, null)
        dialog.setView(view)
        dialog.setCancelable(true)
        val alert = dialog.create()
        alert.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val content = view.findViewById<View>(R.id.dialog_text) as TextView
        val tittle = view.findViewById<View>(R.id.dialog_tittle_text) as TextView
        val cancel = view.findViewById<View>(R.id.cancel_action) as Button
        cancel.setOnClickListener { alert.dismiss() }
        val ok = view.findViewById<View>(R.id.ok_action) as Button
        cancel.visibility = View.VISIBLE
        content.textAlignment= View.TEXT_ALIGNMENT_CENTER
        content.text = reason
        ok.text = "Yes,Logout"
        when(type){
            "logout"->{
                tittle.text = "Alert!"
                if(reason!=null){content.text = reason}
                ok.setOnClickListener {
                    forceLogOutApi()
                    alert.dismiss();
                }
            }
            "success"->{
                tittle.text = "Success"
                if(reason!=null){content.text = reason}
                ok.setOnClickListener {
                    finish();
                    alert.dismiss();
                }
            }
        }
        alert.show()
    }
}