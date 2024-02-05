package com.busydoor.app.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.busydoor.app.R
import com.busydoor.app.apiService.*
import com.busydoor.app.customMethods.*
import com.busydoor.app.databinding.ActivityLoginBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import okhttp3.ResponseBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit


class LoginHomeActivity : ActivityBase(),ApiResponseInterface {
    private var mAuth: FirebaseAuth? = null
    private var verifyCode: String = ""
    private var verificationId: String = ""
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
                    encrypt(timeZoneSet)
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
    }


    /** getApiResponse function... **/
    override fun getApiResponse(apiResponseManager: ApiResponseManager<*>) {
        when (apiResponseManager.type) {
            LOGIN -> {
                val model = apiResponseManager.response as ResponseBody
                val responseValue = model.string()
                val response = JSONObject(responseValue)
                when (response.optInt("status_code")) {
                    SUCCESS_CODE -> {
                        objSharedPref.putString(getString(R.string.userResponse), responseValue)
                        val phone = "+91" + binding.phoneNumber.text.toString()
                        sendVerificationCode(phone)
                        showProgress()
                    }
                    ERROR_CODE -> {
                        val data = response.getJSONObject("data")
                        showSnackBar(
                            binding.root,
                            data.optString("all"),
                            ACTIONSNACKBAR.DISMISS
                        )
                    }
                }
            }
        }
    }
}