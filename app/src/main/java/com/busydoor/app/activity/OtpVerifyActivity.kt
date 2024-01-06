package com.busydoor.app.activity

import android.app.Dialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.busydoor.app.R
import com.busydoor.app.apiService.ApiInitialize
import com.busydoor.app.apiService.ApiRequest
import com.busydoor.app.apiService.ApiResponseInterface
import com.busydoor.app.apiService.ApiResponseManager
import com.busydoor.app.customMethods.ADD_USER
import com.busydoor.app.customMethods.ADD_USER_PREMISE
import com.busydoor.app.customMethods.ADD_USER_RESPONSE
import com.busydoor.app.customMethods.DEVICE_TYPE
import com.busydoor.app.customMethods.ERROR_CODE
import com.busydoor.app.customMethods.REGISTER
import com.busydoor.app.customMethods.SUCCESS_CODE
import com.busydoor.app.customMethods.activity
import com.busydoor.app.customMethods.forceResendingTokenGbl
import com.busydoor.app.customMethods.gContext
import com.busydoor.app.customMethods.isOnline
import com.busydoor.app.customMethods.showView
import com.busydoor.app.databinding.ActivityOtpVerifyBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import okhttp3.ResponseBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class OtpVerifyActivity : ActivityBase(),ApiResponseInterface{
    private var mAuth: FirebaseAuth? = null
    var resendOtpFirstValue: String = ""
    var resendOtpSecondValue: String = ""
    var resendOtpTIme: String = ""
    private var phone_number: String = ""
    private var verifyCode: String = ""
    private var verificationId: String = ""
    private val binding by lazy { ActivityOtpVerifyBinding.inflate(layoutInflater) }
    private var register_type: String = ""

    /** initialize UI and functionality here ... **/
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        gContext = this@OtpVerifyActivity
        activity = this@OtpVerifyActivity
        mAuth = FirebaseAuth.getInstance()

        verificationId = intent.getStringExtra("verificationId").toString()
        phone_number = intent.getStringExtra("phone_number").toString()
        register_type = intent.getStringExtra("otp_type").toString()

        binding.btnOtpVerify.setOnClickListener {
            if (TextUtils.isEmpty(binding.OTP.text.toString())) {
                //if the OTP text field is empty display a message to user to enter OTP
                binding.OTP.error="Please enter OTP"
            } else {
                //if OTP field is not empty calling method to verify the OTP.
                verifyCode(binding.OTP.text.toString())
            }
        }

        binding.resendCode.setOnClickListener {
            if (TextUtils.isEmpty(phone_number)) {
                //when mobile number text field is empty displaying a toast message.
                Toast.makeText(
                    this@OtpVerifyActivity,
                    "Please enter a valid phone number.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                //if the text field is not empty we are calling our send OTP method for getting OTP from Firebase.
                val phone = "+91$phone_number"
                Log.e("resend-data",phone)
                Log.e("token", forceResendingTokenGbl.toString())
                forceResendingTokenGbl?.let { resendVerificationCode(phone, it) }
                binding.timerChange.visibility = View.VISIBLE
                binding.resendCodeView.visibility = View.GONE
                showProgress()

            }

        }


        val spannable = SpannableString("Passcode will expire in 60 sec.")
        spannable.setSpan(
            ContextCompat.getColor(this@OtpVerifyActivity, R.color.app_color),
            24, 26,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        resendOtpFirstValue = getColoredSpanned("Passcode will expire in ", "#818382")

        resendOtpSecondValue = getColoredSpanned(" sec.", "#818382")

        setPhoneCountDownTime()
    }

    /** Show the counter time here ... **/
    private fun setPhoneCountDownTime() {
        object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                resendOtpTIme = getColoredLong(millisUntilFinished / 1000, "#00A2E7")
                //set_timer.text = "You can Resend in " + millisUntilFinished / 1000 + " seconds"
                //here you can have your logic to set text to edittext
                binding.timerChange.visibility = View.VISIBLE
                // set_resend.visibility=View.GONE

                binding.timerChange.text =
                    HtmlCompat.fromHtml(
                        "Resend Code $resendOtpTIme $resendOtpSecondValue",
                        HtmlCompat.FROM_HTML_MODE_COMPACT
                    )
            }

            override fun onFinish() {
                // set_timer.setText("Resend code")
                resendOtpTIme = getColoredSpanned("00", "#00A2E7")
                // tv_timer_change.visibility= View.GONE

                binding.timerChange.visibility = View.GONE
                binding.resendCodeView.visibility = View.VISIBLE
                binding.timerChange.text =
                    HtmlCompat.fromHtml(
                        resendOtpFirstValue + resendOtpTIme + resendOtpSecondValue,
                        HtmlCompat.FROM_HTML_MODE_COMPACT
                    )
            }
        }.start()
    }

    /** VerifyCode function here ... **/
    @RequiresApi(Build.VERSION_CODES.R)
    private fun verifyCode(code: String) {
//          showDialog("response")
        //below line is used for getting getting credentials from our verification id and code.
        val credential = PhoneAuthProvider.getCredential(verificationId, code)

        Log.e("TAG", "Verify cation id is$verificationId")
        //after getting credential we are calling sign in method.
        signInWithCredential(credential)
    }

    /** updateUser to Premise Api fun here ... **/
    @RequiresApi(Build.VERSION_CODES.R)
    private fun updateUserToPremise() {
        Log.d(TAG, "onCreate: " + objSharedPref.getString("FCM_TOKEN")!!)

        if (isOnline(this@OtpVerifyActivity)) {
            ApiRequest(
                this,
                ApiInitialize.initialize(ApiInitialize.LOCAL_URL).userPremiseLink(
                    "Bearer " + getUserModel()!!.data.token,
                    encrypt(phone_number),
                    encrypt(intent.getStringExtra("premiseID").toString()),
                    encrypt(intent.getStringExtra("status").toString()),
                    "linkedtopremise"
                ),
                ADD_USER_PREMISE, true, this
            )
        } else {
            showSnackBar(
                binding.root,
                getString(R.string.noInternet),
                ACTIONSNACKBAR.DISMISS
            )
        }
    }

    /** registerNewApi if call to create new user in admin ... **/
    @RequiresApi(Build.VERSION_CODES.R)
    private fun registerNewApi() {
        Log.d(TAG, "onCreate: " + objSharedPref.getString("FCM_TOKEN")!!)

        if (isOnline(this@OtpVerifyActivity)) {
            ApiRequest(
                this,
                ApiInitialize.initialize(ApiInitialize.LOCAL_URL).createUser(
                    "Bearer " + getUserModel()!!.data.token,
                    encrypt(phone_number),
                    encrypt(intent.getStringExtra("first_name").toString()),
                    encrypt(intent.getStringExtra("last_name").toString()),
                    encrypt(intent.getStringExtra("accessLevelName").toString()),
                    "register",
                    encrypt(intent.getStringExtra("premiseID").toString()),
                    encrypt(intent.getStringExtra("userStatus").toString())
                ),
                ADD_USER, true, this
            )
        } else {
            showSnackBar(
                binding.root,
                getString(R.string.noInternet),
                ACTIONSNACKBAR.DISMISS
            )
        }
    }

    /** normal register user functionality fun here ... **/
    @RequiresApi(Build.VERSION_CODES.R)
    private fun registerApi() {
        if (isOnline(this@OtpVerifyActivity)) {
            ApiRequest(
                this,
                ApiInitialize.initialize(ApiInitialize.LOCAL_URL).userRegisterFunction(
                    encrypt(phone_number),
                    encrypt(intent.getStringExtra("first_name").toString()),
                    encrypt(intent.getStringExtra("last_name").toString()),
                    encrypt(intent.getStringExtra("accessLevelName").toString()),
                    encrypt(DEVICE_TYPE),
                    objSharedPref.getString("FCM_TOKEN")!!,
                    encrypt(timeZoneSet),
                    "register"
                ),
                REGISTER, true, this
            )
        } else {
            showSnackBar(
                binding.root,
                getString(R.string.noInternet),
                ACTIONSNACKBAR.DISMISS
            )
        }
    }

    /** OtpVerify with credentials here ... **/
    @RequiresApi(Build.VERSION_CODES.R)
    private fun signInWithCredential(credential: PhoneAuthCredential) {
        //inside this method we are checking if the code entered is correct or not.
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    when (register_type) {
                        "add_user_to_premise" -> {
                            updateUserToPremise()
                        }
                        "add_user" -> {
                            registerNewApi()
                        }
                        "Register" -> {
                            registerApi()
                        }
                        else -> {
                            objSharedPref.putBoolean(getString(R.string.isLogin), true)
                            val getStartedIntent = Intent(this@OtpVerifyActivity, DashboardActivity::class.java)
                            overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
                            getStartedIntent.putExtra("user_response",objSharedPref.getString(getString(R.string.userResponse)))
                            startActivity(getStartedIntent)
                            finishAffinity()
                        }
                    }
                } else {
                    //if the code is not correct then we are displaying an error message to the user.
                    Toast.makeText(
                        this@OtpVerifyActivity,
                        task.exception!!.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    /** resendNotification here ... **/
    private fun resendVerificationCode(
        phoneNumber: String,
        token: PhoneAuthProvider.ForceResendingToken
    ) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,  // Phone number to verify
            60,  // Timeout duration
            TimeUnit.SECONDS,  // Unit of timeout
            this,  // Activity (for callback binding)
            mCallBack,  // OnVerificationStateChangedCallbacks
            token
        ) // ForceResendingToken from callbacks
    }
    /** callback send OTP fun here ... **/
    private val   //initializing our callbacks for on verification callback method.
            mCallBack: PhoneAuthProvider.OnVerificationStateChangedCallbacks =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            //below method is used when OTP is sent from Firebase
            override fun onCodeSent(
                s: String,
                forceResendingToken: PhoneAuthProvider.ForceResendingToken
            ) {
                super.onCodeSent(s, forceResendingToken)
                forceResendingTokenGbl = forceResendingToken
                Log.e("token-code", forceResendingToken.toString())
                // customDismissDialog(this@LoginActivity, getProgressDialog(this@LoginActivity))
                //when we receive the OTP it contains a unique id which we are storing in our string which we have already created.
                verificationId = s
                dismissProgress()
                setPhoneCountDownTime()
            }

            //this method is called when user receive OTP from Firebase.
            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                //below line is used for getting OTP code which is sent in phone auth credentials.
                val code = phoneAuthCredential.smsCode
                //checking if the code is null or not.
                if (code != null) {
                    verifyCode = code
                }
            }

            //this method is called when firebase does not sends our OTP code due to any error or issue.
            override fun onVerificationFailed(e: FirebaseException) {
                //displaying error message with firebase exception.
                Toast.makeText(this@OtpVerifyActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }

    /** timer text color here ... **/
    private fun getColoredLong(text: Long, color: String): String {
        return "<font color=$color>$text</font>"
    }

    /** timer seconds text color here ... **/
    private fun getColoredSpanned(text: String, color: String): String {
        return "<font color=$color>$text</font>"
    }

    /** apiResponse here ... **/
    override fun getApiResponse(apiResponseManager: ApiResponseManager<*>) {
        when (apiResponseManager.type) {
            REGISTER -> {
                val model = apiResponseManager.response as ResponseBody
                val responseValue = model.string()
                Log.e(TAG, "response LOGIN:-$responseValue")
                val response = JSONObject(responseValue)
                Log.e(TAG, "response LOGIN:-$response")
                when (response.optInt("status_code")) {
                    SUCCESS_CODE -> {
                        objSharedPref.putString(getString(R.string.userResponse), responseValue)
                        objSharedPref.putBoolean(getString(R.string.isLogin), true)
                        val getStartedIntent = Intent(this@OtpVerifyActivity, DashboardActivity::class.java)
                        overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
                        getStartedIntent.putExtra("user_response",objSharedPref.getString(getString(R.string.userResponse)))
                        showSnackBar(
                            binding.root,
                            response.optString("message"),
                            ACTIONSNACKBAR.DISMISS
                        )
                        startActivity(getStartedIntent)
                        finishAffinity()
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
            ADD_USER_PREMISE -> {
                val model = apiResponseManager.response as ResponseBody

                val responseValue = model.string()
                Log.e(TAG, "response LOGIN:-$responseValue")
                val response = JSONObject(responseValue)
                when (response.optInt("status_code")) {
                    SUCCESS_CODE -> {
                        ADD_USER_RESPONSE = 1
                        showDialog(response.optString("message"))
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
            ADD_USER -> {
                val model = apiResponseManager.response as ResponseBody
                val responseValue = model.string()
                val response = JSONObject(responseValue)
                when (response.optInt("status_code")) {
                    SUCCESS_CODE -> {
                        showSnackBar(
                            binding.root,
                            response.optString("message"),
                            ACTIONSNACKBAR.DISMISS
                        )
                        showView = false
                        ADD_USER_RESPONSE = 1
                        finish()
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

    /** Showing the error response ... **/
    private fun showDialog(title: String) {
        val dialog = Dialog(this)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.response_dialog_layout)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window?.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog.show()
        dialog.window?.attributes = lp
        val body = dialog.findViewById(R.id.body) as TextView
        body.text = title
        val yesBtn = dialog.findViewById(R.id.okBtn) as Button
        yesBtn.setOnClickListener {
            dialog.dismiss()
            val intent = Intent()
            setResult(RESULT_OK, intent)
            finish()
        }
    }

}