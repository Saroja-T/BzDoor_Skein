package com.busydoor.app.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.busydoor.app.R
import com.busydoor.app.apiService.ApiInitialize
import com.busydoor.app.apiService.ApiRequest
import com.busydoor.app.apiService.ApiResponseInterface
import com.busydoor.app.apiService.ApiResponseManager
import com.busydoor.app.customMethods.DEVICE_TYPE
import com.busydoor.app.customMethods.ERROR_CODE
import com.busydoor.app.customMethods.PrefUtils
import com.busydoor.app.customMethods.REGISTER
import com.busydoor.app.customMethods.SUCCESS_CODE
import com.busydoor.app.customMethods.activity
import com.busydoor.app.customMethods.encode
import com.busydoor.app.customMethods.forceResendingTokenGbl
import com.busydoor.app.customMethods.gContext
import com.busydoor.app.customMethods.isOnline
import com.busydoor.app.databinding.ActivityRegisterBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import okhttp3.ResponseBody
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit

class RegisterActivity : ActivityBase(), ApiResponseInterface, AdapterView.OnItemSelectedListener {
    private var accessLevelList: ArrayList<String>? = null
    private var accessLevelName: String = ""
    private var mAuth: FirebaseAuth? = null
    private var verifyCode: String = ""
    private var verificationId: String = ""
    private var premiseID: String = ""
    private var premiseName: String = ""
    private val binding by lazy { ActivityRegisterBinding.inflate(layoutInflater) }

    /** Initially set all UI data set here... **/
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        gContext = this@RegisterActivity
        activity = this@RegisterActivity
        objSharedPref = PrefUtils(this@RegisterActivity)
        mAuth = FirebaseAuth.getInstance()
        accessLevelList = ArrayList()
        accessLevelList!!.add("")
        accessLevelList!!.add("Manager")
        accessLevelList!!.add("Staff")

        premiseID = intent?.getStringExtra("premise_id").toString()

        setSpinner1(binding.spAccessLevel,accessLevelList!!)

        /** verify button fun here ... **/
        binding.btnRegister.setOnClickListener {
            if(binding.edLastName.text!!.isNotEmpty()&& binding.etFirstName.text!!.isNotEmpty()&& accessLevelName.isNotEmpty()){
                if(binding.edMobileNumber.text!!.length==10){
                    if(binding.etFirstName.text!!.length>=3){
                        registerApi()
                    }else{
                        binding.etFirstName.error="Please enter valid first name"
                    }
                }else{
                    binding.edMobileNumber.error="Please enter valid mobile number"
                }

            }else{
                binding.edMobileNumber.error="Please enter valid MobileNumber"
                binding.etFirstName.error="Please enter valid FirstName"
                binding.edLastName.error="Please enter valid LastName"
            }

        }

        /** if u want to login just click here ... **/
        binding.tvTermAndCondition.setOnClickListener {
            val loginActivity =Intent(this@RegisterActivity,LoginHomeActivity::class.java)
            startActivity(loginActivity)
            finish()
        }
    }

    /** onResume fun... **/
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onResume() {
        super.onResume()
        gContext = this@RegisterActivity
    }

    private fun setSpinner1(spinner: Spinner, list: java.util.ArrayList<String>) {

        spinner.onItemSelectedListener = this
        val adapter = object : ArrayAdapter<String>(
            this@RegisterActivity,
            R.layout.item_detail__second_row,
            list
        ) {

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup?
            ): View {
                val view = super.getDropDownView(position, convertView, parent!!)
                view as TextView
                return view
            }
        }
        adapter.setDropDownViewResource(R.layout.item_detail__second_row)
        spinner.adapter = adapter
    }





    /** If user was a new to u need to register so call this fun... **/
    @RequiresApi(Build.VERSION_CODES.R)
    private fun registerApi() {
        Log.d("TAG", "Register# " + objSharedPref.getString("FCM_TOKEN")!!)
        if (isOnline(this@RegisterActivity)) {
            Log.e("Register#em", encrypt(binding.edMobileNumber.text.toString()))
            Log.e("Register#ef", encrypt(binding.etFirstName.text.toString()))
            Log.e("Register#el", encrypt(binding.edLastName.text.toString()))
            Log.e("Register#al", accessLevelName)
            Log.e("Register#dt", DEVICE_TYPE.toString())
            Log.e("Register#ft", objSharedPref.getString("FCM_TOKEN")!!)
            Log.e("Register#ck", "check")
            ApiRequest(
                this,
                ApiInitialize.initialize(ApiInitialize.LOCAL_URL).userRegisterFunction(
                    encrypt(binding.edMobileNumber.text.toString()),
                    encrypt(binding.etFirstName.text.toString()),
                    encrypt(binding.edLastName.text.toString()),
                    encrypt(accessLevelName.lowercase(Locale.ROOT)),
                    encrypt(DEVICE_TYPE),
                    objSharedPref.getString("FCM_TOKEN")!!,
                    encrypt(timeZoneSet),
                    "check"
                ),
                REGISTER, true, this
            )
        } else {
            showSnackBar(
                binding.llRegisterParentView,
                getString(R.string.noInternet),
                ACTIONSNACKBAR.DISMISS
            )
        }
    }


      override fun encrypt(value: String): String {
        return if (TextUtils.isEmpty(value)) {
            value.replace('\"', ' ', ignoreCase = false)
        } else {

            val values = cryptLib!!.encrypt(value,
                com.busydoor.app.customMethods.key,
                com.busydoor.app.customMethods.ENCRYPTION_IV
            )
            Log.e(
                "BDApplication.TAG",
                "original value == $value " +
                        "encrypted message ==> ${
                            values!!.trimEnd().encode()
                        }"
            )
            values!!.trimEnd().encode()
        }
    }

    /** apiResponse here ... **/
    override fun getApiResponse(apiResponseManager: ApiResponseManager<*>) {
        when (apiResponseManager.type) {

            REGISTER -> {
                val model = apiResponseManager.response as ResponseBody
                val responseValue = model.string()
                Log.e(TAG, "response LOGIN:-$responseValue")
                val response = JSONObject(responseValue)
                when (response.optInt("status_code")) {
                    SUCCESS_CODE -> {
                        val phone = "+91" + binding.edMobileNumber.text.toString()
                        sendVerificationCode(phone)
                    }
                    ERROR_CODE -> {
                        val data = response.getJSONObject("data")
                        showSnackBar(
                            binding.llRegisterParent,
                            data.optString("all"),
                            ACTIONSNACKBAR.DISMISS
                        )
                    }
                }
            }
        }
    }

    /** sendNotification here ... **/
    private fun sendVerificationCode(number: String) {
        //this method is used for getting OTP on user phone number.
        Log.e("login","jio")
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
        /*PhoneAuthProvider.getInstance().verifyPhoneNumber(
            number,  //first parameter is user's mobile number
            60,  //second parameter is time limit for OTP verification which is 60 seconds in our case.
            TimeUnit.SECONDS,  // third parameter is for initializing units for time period which is in seconds in our case.
            TaskExecutors.MAIN_THREAD,  //this task will be excuted on Main thread.
            mCallBack //we are calling callback method when we recieve OTP for auto verification of user.
        )*/
    }

    /** callback function here ... **/
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
                dismissProgress()
                // customDismissDialog(this@LoginActivity, getProgressDialog(this@LoginActivity))
                //when we recieve the OTP it contains a unique id wich we are storing in our string which we have already created.
                verificationId = s
                Log.e("login","jio")
                val getStartedIntent = Intent(this@RegisterActivity, OtpVerifyActivity::class.java)
                getStartedIntent.putExtra("verificationId", verificationId)
                getStartedIntent.putExtra("phone_number", binding.edMobileNumber.text.toString())
                getStartedIntent.putExtra("otp_type","Register")
                getStartedIntent.putExtra("first_name", binding.etFirstName.text.toString())
                getStartedIntent.putExtra("last_name",binding.edLastName.text.toString())
                getStartedIntent.putExtra("accessLevelName",accessLevelName)
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
                startActivity(getStartedIntent)
                finish()
            }

            //this method is called when user recieve OTP from Firebase.
            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                //below line is used for getting OTP code which is sent in phone auth credentials.
                val code = phoneAuthCredential.smsCode
                //checking if the code is null or not.
                if (code != null) {
                    //if the code is not null then we are setting that code to our OTP edittext field.
                    //   edtOTP!!.setText(code)
                    //after setting this code to OTP edittext field we are calling our verifycode method.
                    //  verifyCode(code)
                    verifyCode = code
                }
            }

            //this method is called when firebase doesnot sends our OTP code due to any error or issue.
            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(this@RegisterActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent) {
            binding.spAccessLevel -> {
                accessLevelName = accessLevelList!![position]
                Log.e("accessLevelName",accessLevelName)
            }
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        Log.e("accessLevelName1",accessLevelName)
    }
}