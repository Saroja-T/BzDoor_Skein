
package com.busydoor.app.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.busydoor.app.R
import com.busydoor.app.activity.CreateNewUserActivity
import com.busydoor.app.activity.CryptLib2
import com.busydoor.app.activity.EditProfileActivity
import com.busydoor.app.activity.OtpVerifyActivity
import com.busydoor.app.adapter.UserListAdapter
import com.busydoor.app.apiService.ApiInitialize
import com.busydoor.app.apiService.ApiRequest
import com.busydoor.app.apiService.ApiResponseInterface
import com.busydoor.app.apiService.ApiResponseManager
import com.busydoor.app.customMethods.ADD_USER_RESPONSE
import com.busydoor.app.customMethods.ADD_USER_TO_PREMISE
import com.busydoor.app.customMethods.ENCRYPTION_IV
import com.busydoor.app.customMethods.ERROR_CODE
import com.busydoor.app.customMethods.PrefUtils
import com.busydoor.app.customMethods.SUCCESS_CODE
import com.busydoor.app.customMethods.USER_ACTIVE_DEACTIVE
import com.busydoor.app.customMethods.USER_LIST_DATA
import com.busydoor.app.customMethods.encode
import com.busydoor.app.customMethods.forceResendingTokenGbl
import com.busydoor.app.customMethods.isOnline
import com.busydoor.app.customMethods.key
import com.busydoor.app.databinding.FragmentUserListBinding
import com.busydoor.app.interfaceD.HomeClick
import com.busydoor.app.model.AddUserToPremise
import com.busydoor.app.model.PremiseUserList
import com.busydoor.app.model.UserModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.gson.Gson
import okhttp3.ResponseBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit


class UserListFragment : Fragment(),ApiResponseInterface,HomeClick {
    private lateinit var binding: FragmentUserListBinding
    private var userListPremise: PremiseUserList? = null
    private var userActiveDeactiveRes: ResponseBody? = null
    private var userActiveDeactiveResponse: AddUserToPremise? = null
    open lateinit var objSharedPref: PrefUtils
    var cryptLib: CryptLib2? = null
    private var otpType = ""
    private lateinit var add_user_ll:LinearLayout
    private lateinit var progress_circular:ProgressBar
    private lateinit var errorMsg:TextView
    private var swStatus:String = ""
    private var userActiveStatus:String = ""
    private var userActiverName:String = ""
    private var premiseName:String = ""
    private var premiseId:String = ""
    private var verifyCode: String = ""
    private var verificationId: String = ""
    private var mobileNumberStr:String = ""
    lateinit var pd : ProgressDialog
    private var mAuth: FirebaseAuth? = null
    lateinit var dialog : AlertDialog.Builder






    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserListBinding.inflate(inflater, container, false)
        val root: View = binding.root
        objSharedPref = PrefUtils(requireContext())
        cryptLib = CryptLib2()
        swStatus="true"
        premiseId=activity?.intent?.getStringExtra("premiseId").toString()
        Log.e("original value userList== ",premiseId.toString())
        mAuth = FirebaseAuth.getInstance()
        pd = ProgressDialog(requireContext())
        pd.setMessage("Please wait...")

        binding.userProfileView.editProfile.setOnClickListener {
            startActivity(Intent(requireActivity(), EditProfileActivity::class.java))
        }
        binding.userProfileView.backPage.setOnClickListener {
            requireActivity().finish()
        }
        binding.first.setOnClickListener {
            showAlertBox("adduser","")
            Log.e("user_list","first")
        }
        binding.second.setOnClickListener {
            startActivity(Intent(requireContext(),CreateNewUserActivity::class.java).putExtra("premiseName",premiseName).putExtra("premise_id",premiseId))
        }

        getUserListRequest()
        // Inflate the layout for this fragment
        return root
    }
    @RequiresApi(Build.VERSION_CODES.R)
    override fun homePostionClick(postion: Int) {
        Log.e("original value dash== ",userListPremise!!.data!!.staffdetails!![postion].userActiveStatus.toString())
        userActiveStatus=userListPremise!!.data!!.staffdetails!![postion].userActiveStatus.toString()
        userActiverName=userListPremise!!.data!!.staffdetails!![postion].firstName.toString()
        when (userActiveStatus) {
            "true" -> {
                userActiveStatus="Inactive"
                showAlertBox("true",userListPremise!!.data!!.staffdetails!![postion].userId.toString())
            }
            "false" -> {
                userActiveStatus="Active"
                showAlertBox("false",userListPremise!!.data!!.staffdetails!![postion].userId.toString())
            }
            else -> {
                userActiveStatus=""
                showAlertBox("activate",userListPremise!!.data!!.staffdetails!![postion].userId.toString())
            }
        }


    }

    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.R)
    fun showAlertBox(type:String,response:String){
        dialog = AlertDialog.Builder(requireContext())
        val view: View = layoutInflater.inflate(com.busydoor.app.R.layout.custom_add_user_dialog, null)
        dialog.setView(view)
        dialog.setCancelable(true)
        val alert = dialog.create()
        alert.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val content = view.findViewById<View>(com.busydoor.app.R.id.dialog_text) as TextView
        val mobileNum = view.findViewById<View>(com.busydoor.app.R.id.phoneNumber) as TextView
        val tittle = view.findViewById<View>(com.busydoor.app.R.id.dialog_tittle_text) as TextView
        val cancel = view.findViewById<View>(com.busydoor.app.R.id.cancel_action) as Button
        val sw_status = view.findViewById<View>(com.busydoor.app.R.id.sw_status) as SwitchCompat
        sw_status.setOnCheckedChangeListener { buttonView, isChecked ->
            swStatus = if (isChecked){
                "true"
            }else{
                "false"
            }
        }
        errorMsg = view.findViewById<View>(R.id.errorMsg) as TextView
        add_user_ll = view.findViewById<View>(R.id.add_user_ll) as LinearLayout
        progress_circular = view.findViewById<View>(R.id.progress_circular) as ProgressBar
        cancel.setOnClickListener { alert.dismiss() }
        val ok = view.findViewById<View>(R.id.ok_action) as Button
        when(type){
            "adduser"->{
                ok.setOnClickListener{
                    if(mobileNum.text.length>=10){
                        val phone = "+91" + mobileNum.text.toString().trim()
                        Log.e("print123",phone)
                        otpType = "add_user_to_premise"
                        progress_circular.visibility =View.VISIBLE
                        mobileNumberStr=mobileNum.text.toString()
                        addUserRequest(mobileNum.text.toString(),swStatus)
                        alert.dismiss();
                    }else{
                        Log.e("print123",swStatus)
                        mobileNum.error = "Please enter valid mobile number"
                    }
                }
            }
            "false"->{
                content.visibility =View.VISIBLE
                add_user_ll.visibility =View.GONE
                sw_status.visibility =View.GONE
                cancel.visibility =View.GONE
                tittle.text = "Active/Inactive User"
                content.text="Are you sure want to $userActiveStatus ${userActiverName} User?"
                ok.setOnClickListener {
                    updateUserStatus(response,"true")
                    alert.dismiss()
                }
            }
            "true"->{
                content.visibility =View.VISIBLE
                add_user_ll.visibility =View.GONE
                sw_status.visibility =View.GONE
                cancel.visibility =View.GONE
                tittle.text = "Active/Inactive User"
                content.text="Are you sure want to $userActiveStatus ${userActiverName} User?"
                ok.setOnClickListener {
                    updateUserStatus(response,"false")
                    alert.dismiss()
                }
            }

            "api-success"->{
                content.visibility =View.VISIBLE
                add_user_ll.visibility =View.GONE
                sw_status.visibility =View.GONE
                cancel.visibility =View.GONE
                tittle.text = "Success"
                content.text=response
                ok.text= "Done"
                ok.setOnClickListener {
                    alert.dismiss()
                }
            }
            "api-failure"->{
                tittle.text="something went wrong"
                content.text=userListPremise!!.message.toString();
                ok.text= "Ok"
                add_user_ll.visibility =View.GONE
                sw_status.visibility =View.GONE
                content.visibility =View.VISIBLE
                cancel.visibility= View.GONE
                ok.setOnClickListener {
                    alert.dismiss()
                }
            }
        }
        alert.show()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun getUserListRequest() {
        try {
            if (isOnline(requireContext())) {

                Log.e("apiCalled", " yes")
                Log.d("BDApplication", "the p ${"requestType".toString()}")
                ApiRequest(
                    requireActivity(), ApiInitialize.initialize(ApiInitialize.LOCAL_URL).getStaffListBypremise(
                        "Bearer ${getUserModel()!!.data.token}",
                        encrypt(premiseId),
                    ), USER_LIST_DATA, true, this
                )

            }else {
                Log.e("Application", "offline")
            }
        } catch (e: Exception) {
            Log.e("APIEXceptions", e.toString())
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun addUserRequest(phone:String,status:String) {
        try {
            if (isOnline(requireContext())) {

                Log.e("apiCalled", " yes")
                Log.d("apiCalled", "values==>> ${"mnumber $phone status $status id $premiseId"}")
                ApiRequest(
                    requireActivity(), ApiInitialize.initialize(ApiInitialize.LOCAL_URL).userPremiseLink(
                        "Bearer ${getUserModel()!!.data.token}",
                        encrypt(phone),
                        encrypt(premiseId),
                        encrypt(status),
                        "check",
                    ), ADD_USER_TO_PREMISE, true, this
                )
            }else {
                Log.e("Application", "offline")
            }
        } catch (e: Exception) {
            Log.e("APIEXceptions", e.toString())
        }
    }
    @RequiresApi(Build.VERSION_CODES.R)
    fun updateUserStatus(userid:String,status:String) {
        try {
            if (isOnline(requireContext())) {
                Snackbar.make(binding.root, "Switch state checked ", Snackbar.LENGTH_LONG)
                    .setAction("ACTION",null).show();
                Log.e("apiCalled", " yes")
                Log.d("BDApplication", "the p ${"requestType".toString()}")
                ApiRequest(
                    requireActivity(), ApiInitialize.initialize(ApiInitialize.LOCAL_URL).updateUserStatus(
                        "Bearer ${getUserModel()!!.data.token}",
                        encrypt(status),
                        encrypt(premiseId),
                        encrypt(userid)
                    ), USER_ACTIVE_DEACTIVE, true, this
                )

            }else {
                Log.e("Application", "offline")
            }
        } catch (e: Exception) {
            Log.e("APIEXceptions", e.toString())
        }
    }

    private fun setUserAdapter(data: ArrayList<PremiseUserList.Data.Staffdetails>) {
        if(data.size>0){
            binding.rvUserList.setHasFixedSize(true)
            val userListLayout = GridLayoutManager(requireContext(),2)
            binding.rvUserList.layoutManager =userListLayout
            val userListAdapter=UserListAdapter(
                requireContext(),data,getUserModel()!!.data!!.userId.toString(), this
            )
            // create a adapter
            binding.rvUserList.adapter=userListAdapter
            binding.rvUserList.visibility=View.VISIBLE
            binding.userNodataView.visibility=View.GONE
        }else{
            binding.userNodataView.visibility =View.VISIBLE
            binding.rvUserList.visibility =View.GONE
        }
    }
    @SuppressLint("NewApi")
    override fun getApiResponse(apiResponseManager: ApiResponseManager<*>) {
        when (apiResponseManager.type) {
            USER_LIST_DATA ->{
                userListPremise = apiResponseManager.response as PremiseUserList
                if(userListPremise!!.statusCode== SUCCESS_CODE){
                    val circularProgressDrawable = CircularProgressDrawable(requireContext())
                    circularProgressDrawable.strokeWidth = 5f
                    circularProgressDrawable.centerRadius = 30f
                    circularProgressDrawable.start()

                    binding.userProfileView.userName.text=userListPremise!!.data!!.userdetails!!.userFirstName+" "+userListPremise!!.data!!.userdetails!!.userLastName
                    binding.userProfileView.userNumber.text=userListPremise!!.data!!.premisedetails!!.premiseName+", "+userListPremise!!.data!!.premisedetails!!.city+", "+userListPremise!!.data!!.premisedetails!!.state
                    if(userListPremise!!.data!!.userdetails!! !=null) {
                        Glide.with(requireContext())
                            .load(userListPremise!!.data!!.userdetails!!.userImage)
                            .placeholder(circularProgressDrawable)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(binding.userProfileView!!.PremiseStaffImage)
                    }else{
                        Glide.with(requireContext())
                            .load(R.drawable.icon_users)
                            .placeholder(circularProgressDrawable)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(binding.userProfileView!!.PremiseStaffImage)
                    }
                    when (userListPremise!!.data!!.userdetails!!.userStatus) {
                        "in" -> {
                            binding.userProfileView.staffStatus.setImageResource(R.drawable.icon_staff_profile_in)
                        }
                        "inout" -> {
                            binding.userProfileView.staffStatus.setImageResource(R.drawable.icon_profile_status_inout)
                        }
                        "out" -> {
                            binding.userProfileView.staffStatus.setImageResource(R.drawable.icon_profile_status_out)
                        }
                        "out" -> {
                            binding.userProfileView.staffStatus.setImageResource(R.drawable.icon_profile_status_offline)
                        }
                        else -> {
                            binding.userProfileView.staffStatus.setImageResource(R.drawable.icon_profile_status_offline)

                        }
                    }

                    setUserAdapter(userListPremise!!.data!!.staffdetails)
                    premiseName=userListPremise!!.data!!.premisedetails!!.premiseName.toString()

                }else{
                    showAlertBox("api-failure",userListPremise!!.message.toString())
                }
            }
            ADD_USER_TO_PREMISE ->{
                val model = apiResponseManager.response as ResponseBody
                val responseValue = model.string()
                Log.e("TAG", "response LOGIN:-$responseValue")
                val response = JSONObject(responseValue)
                when (response.optInt("status_code")) {
                    SUCCESS_CODE -> {
                        ADD_USER_RESPONSE = 1
                        val phone = "+91$mobileNumberStr"
                        sendVerificationCode(phone, response)

                    }
                    ERROR_CODE -> {
                        val data = response.optString("message")
                        showAlertBox("api-failure",data)
                    }
                }
            }
            USER_ACTIVE_DEACTIVE ->{
                userActiveDeactiveResponse = apiResponseManager.response as AddUserToPremise
                if(userActiveDeactiveResponse!!.statusCode== SUCCESS_CODE){
                    getUserListRequest()
                    showAlertBox("api-success",userActiveDeactiveResponse!!.message.toString())
                }else{
                    showAlertBox("api-failure",userActiveDeactiveResponse!!.message.toString())}
            }
        }

    }

    private fun sendVerificationCode(number: String, response: JSONObject) {
        //this method is used for getting OTP on user phone number.
        pd.show()
        val options = mAuth?.let {
            PhoneAuthOptions.newBuilder(it)
                .setPhoneNumber(number)       // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(requireActivity())                 // Activity (for callback binding)
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
    private val   //initializing our callbacks for on verification callback method.
            mCallBack: PhoneAuthProvider.OnVerificationStateChangedCallbacks =
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            //below method is used when OTP is sent from Firebase
            @RequiresApi(Build.VERSION_CODES.R)
            override fun onCodeSent(
                s: String,
                forceResendingToken: PhoneAuthProvider.ForceResendingToken
            ) {
                super.onCodeSent(s, forceResendingToken)
                forceResendingTokenGbl = forceResendingToken
                pd.dismiss()
                // customDismissDialog(this@LoginActivity, getProgressDialog(this@LoginActivity))
                //when we recieve the OTP it contains a unique id wich we are storing in our string which we have already created.
                verificationId = s

                    val getStartedIntent = Intent(requireContext(), OtpVerifyActivity::class.java)
                    getStartedIntent.putExtra("verificationId", verificationId)
                    getStartedIntent.putExtra("phone_number", mobileNumberStr)
                    getStartedIntent.putExtra("otp_type",otpType)
                    getStartedIntent.putExtra("premiseID",premiseId)
                    getStartedIntent.putExtra("status",swStatus)
                    getStartedIntent.putExtra("premise_id",premiseId)
                    getStartedIntent.putExtra("status",swStatus)
                    requireActivity().startActivityForResult(getStartedIntent,111)
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

            //this method is called when firebase does not sends our OTP code due to any error or issue.
            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
                pd.dismiss()
            }
        }

    open fun encrypt(value: String): String {
        return if (TextUtils.isEmpty(value)) {
            value.replace('\"', ' ', ignoreCase = false)
        } else {
            val values = cryptLib!!.encrypt(value, key, ENCRYPTION_IV)
            Log.e(
                "TAG",
                "original value == $value " +
                        "encrypted message ==> ${
                            values!!.trimEnd().encode()
                        }"
            )
            values.trimEnd().encode()
        }
    }
    fun getUserModel(): UserModel? {
        val gson = Gson()
        return gson.fromJson(
            objSharedPref.getString(getString(com.busydoor.app.R.string.userResponse))!!, UserModel::class.java
        )
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e("onactivityresult","result added")
        getUserListRequest()
    }
}