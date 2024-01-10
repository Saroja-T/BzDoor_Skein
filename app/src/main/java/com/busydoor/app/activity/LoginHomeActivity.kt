package com.busydoor.app.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.busydoor.app.R
import com.busydoor.app.apiService.ApiInitialize
import com.busydoor.app.apiService.ApiRequest
import com.busydoor.app.apiService.ApiResponseInterface
import com.busydoor.app.apiService.ApiResponseManager
import com.busydoor.app.customMethods.DEVICE_TYPE
import com.busydoor.app.customMethods.ERROR_CODE
import com.busydoor.app.customMethods.LOGIN
import com.busydoor.app.customMethods.SUCCESS_CODE
import com.busydoor.app.customMethods.activity
import com.busydoor.app.customMethods.forceResendingTokenGbl
import com.busydoor.app.customMethods.gContext
import com.busydoor.app.customMethods.isOnline
import com.busydoor.app.databinding.ActivityLoginBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
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
//                showSnackBar(
//                    binding.root,
//                    "Please enter Valid number",
//                    ACTIONSNACKBAR.DISMISS
//                )
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
        val options = mAuth?.let {
            PhoneAuthOptions.newBuilder(it)
                .setPhoneNumber(number)       // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this)                 // Activity (for callback binding)
                .setCallbacks(mCallBack)          // OnVerificationStateChangedCallbacks
                .build()
        }
        if (options != null) {
            PhoneAuthProvider.verifyPhoneNumber(options)
        }
    }


    /** initializing our callbacks for on verification callback method.... **/
    private val mCallBack: PhoneAuthProvider.OnVerificationStateChangedCallbacks =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            //below method is used when OTP is sent from Firebase
            override fun onCodeSent(
                s: String,
                forceResendingToken: PhoneAuthProvider.ForceResendingToken
            ) {
                super.onCodeSent(s, forceResendingToken)
                dismissProgress()
                forceResendingTokenGbl = forceResendingToken
                // customDismissDialog(this@LoginActivity, getProgressDialog(this@LoginActivity))
                //when we receive the OTP it contains a unique id which we are storing in our string which we have already created.
                verificationId = s
                Log.e("send code","test")
                val getStartedIntent = Intent(this@LoginHomeActivity, OtpVerifyActivity::class.java)
                getStartedIntent.putExtra("verificationId", verificationId)
                getStartedIntent.putExtra("phone_number", binding.phoneNumber.text.toString())
                getStartedIntent.putExtra("otp_type","Login")
                startActivity(getStartedIntent)
                finish()
            }
            //this method is called when user receive OTP from Firebase.
            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                //below line is used for getting OTP code which is sent in phone auth credentials.
                val code = phoneAuthCredential.smsCode
                //checking if the code is null or not.
                if (code != null) {
                    //if the code is not null then we are setting that code to our OTP edittext field.
                    //   edtOTP!!.setText(code)
                    //after setting this code to OTP edittext field we are calling our verification code method.
                    //  verifyCode(code)
                    verifyCode = code
                }
            }

            //This method is called when firebase does not sends our OTP code due to any error or issue.
            override fun onVerificationFailed(e: FirebaseException) {
                //displaying error message with firebase exception.
                Toast.makeText(this@LoginHomeActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }


    /** getApiResponse function... **/
    override fun getApiResponse(apiResponseManager: ApiResponseManager<*>) {
        when (apiResponseManager.type) {
            LOGIN -> {
                val model = apiResponseManager.response as ResponseBody
                val responseValue = model.string()
                Log.e(TAG, "response LOGIN:-$responseValue")
                val response = JSONObject(responseValue)
                Log.e(TAG, "response LOGINxx:-${response.optInt("status_code")}")
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