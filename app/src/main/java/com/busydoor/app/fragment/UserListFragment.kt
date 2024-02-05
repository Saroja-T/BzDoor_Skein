
package com.busydoor.app.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.Handler
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.busydoor.app.R
import com.busydoor.app.activity.CreateNewUserActivity
import com.busydoor.app.activity.CryptLib2
import com.busydoor.app.activity.OtpVerifyActivity
import com.busydoor.app.adapter.UserListAdapter
import com.busydoor.app.apiService.ApiInitialize
import com.busydoor.app.apiService.ApiRequest
import com.busydoor.app.apiService.ApiResponseInterface
import com.busydoor.app.apiService.ApiResponseManager
import com.busydoor.app.customMethods.ADD_USER_RESPONSE
import com.busydoor.app.customMethods.ADD_USER_TO_PREMISE
import com.busydoor.app.customMethods.Activate
import com.busydoor.app.customMethods.ActiveInActivate
import com.busydoor.app.customMethods.ENCRYPTION_IV
import com.busydoor.app.customMethods.ERROR_CODE
import com.busydoor.app.customMethods.InActivate
import com.busydoor.app.customMethods.PhoneAuthUtil
import com.busydoor.app.customMethods.PrefUtils
import com.busydoor.app.customMethods.SUCCESS_CODE
import com.busydoor.app.customMethods.USER_ACTIVE_DEACTIVE
import com.busydoor.app.customMethods.USER_LIST_DATA
import com.busydoor.app.customMethods.encode
import com.busydoor.app.customMethods.forceResendingTokenGbl
import com.busydoor.app.customMethods.isOnline
import com.busydoor.app.customMethods.isRefresh
import com.busydoor.app.customMethods.key
import com.busydoor.app.databinding.FragmentUserListBinding
import com.busydoor.app.interfaceD.HomeClick
import com.busydoor.app.interfaceD.OnOtpVerifiedListener
import com.busydoor.app.interfaceD.OtpVerifiedListenerHolder
import com.busydoor.app.model.PremiseUserList
import com.busydoor.app.model.UpdateUserStatus
import com.busydoor.app.model.UserModel
import com.busydoor.app.viewmodel.OTPViewModel
import com.busydoor.app.viewmodel.ProfileViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.gson.Gson
import okhttp3.ResponseBody
import org.json.JSONObject
import java.io.Serializable
import java.util.concurrent.TimeUnit

interface OnUserCreatedListener : Serializable {
    fun onUserCreated()
}

class UserListFragment : Fragment(),ApiResponseInterface,HomeClick {
    private lateinit var binding: FragmentUserListBinding
    private var userListPremise: PremiseUserList? = null
    private var userActiveDeactiveRes: ResponseBody? = null
    private var userActiveDeactiveResponse: UpdateUserStatus? = null
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
    private var isAdmin:String = ""
    // to check whether sub FABs are visible or not
    var isAllFabsVisible: Boolean? = null
    var isShowAdmin: Boolean? = null
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var otpViewModel : OTPViewModel

    @RequiresApi(Build.VERSION_CODES.R)
    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            getUserListRequest()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("ResourceType", "SuspiciousIndentation")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserListBinding.inflate(inflater, container, false)
        val root: View = binding.root
        objSharedPref = PrefUtils(requireContext())
        cryptLib = CryptLib2()
        profileViewModel = ViewModelProvider(requireActivity()).get(ProfileViewModel::class.java)
        otpViewModel = ViewModelProvider(this).get(OTPViewModel::class.java)
        otpViewModel.homeData.observe(requireActivity()) { data ->
            // Handle changes to the shared data in BottomBarFragment
            // The 'data' variable contains the updated value
            // Handle the updated profile data
            if (data != null) {
                // Access individual values like profileData["profileImage"], profileData["firstName"], etc.
                Log.e("OTPTrigggererd","djdkjfjdfi")
            }
        }

        swStatus="true"
        premiseId=activity?.intent?.getStringExtra("premiseId").toString()
        isAdmin = getUserModel()?.data?.isAdmin.toString()
        Log.e("original value userList== ",premiseId.toString())
        mAuth = FirebaseAuth.getInstance()
        pd = ProgressDialog(requireContext())
        pd.setMessage("Please wait...")

        if(isAdmin=="1") binding.addFab.visibility = View.VISIBLE else binding.addFab.visibility = View.GONE

        // Now set all the FABs and all the action name
        // texts as GONE
        binding.createUser.setVisibility(View.GONE)
        binding.addUser.setVisibility(View.GONE)
        binding.addAlarmActionText.setVisibility(View.GONE)
        binding.addPersonActionText.setVisibility(View.GONE)

        binding.addAlarmActionText.setOnClickListener {
            createNewUserPage()
        }
        binding.addPersonActionText.setOnClickListener {
            showAlertBox("adduser","")
        }

        // make the boolean variable as false, as all the
        // action name texts and all the sub FABs are
        // invisible


        isAllFabsVisible = false

        // Set the Extended floating action button to
        // shrinked state initially

        binding.addFab.shrink()

        // We will make all the FABs and action name texts
        // visible only when Parent FAB button is clicked So
        // we have to handle the Parent FAB button first, by
        // using setOnClickListener you can see below

        binding.addFab.setOnClickListener(
            View.OnClickListener {
                setFabIcon()
            })
        binding.closeFab.setOnClickListener(
            View.OnClickListener {
                setFabIcon()
            })

        //  below is the sample action to handle add user
        //  FAB. Here it shows simple alert box. The alert box
        //  will be shown only when they are visible and only
        //  when user clicks on them

        binding.addUser.setOnClickListener {
            showAlertBox("adduser","")
        }

        // below is the sample action to handle create user
        // FAB. Here it redirects to create new user page. Redirection
        // will be available only when they are visible and only
        // when user clicks on them

        binding.createUser.setOnClickListener {
            createNewUserPage()
        }
        getUserListRequest()
        // Inflate the layout for this fragment
        return root
    }

    @SuppressLint("ResourceAsColor")
    private fun setFabIcon() {
        if (!isAllFabsVisible!!) {

            // when isAllFabsVisible becomes
            // true make all the action name
            // texts and FABs VISIBLE.
            binding.createUser.show()
            binding.addUser.show()
            binding.addAlarmActionText.setVisibility(View.VISIBLE)
            binding.addPersonActionText.setVisibility(View.VISIBLE)

            // Now extend the parent FAB, as
            // user clicks on the shrinked
            // parent FAB
            binding.addFab.extend()
            // binding.addFab.setIconResource(R.drawable.icon_close)
            binding.clFab.background = resources.getDrawable(android.R.drawable.screen_background_light_transparent)
//                    binding.clFab.setBackgroundColor(R.)
            // make the boolean variable true as
            // we have set the sub FABs
            // visibility to GONE
            isAllFabsVisible = true

            binding.addFab.visibility= View.GONE
            binding.closeFab.visibility= View.VISIBLE
        } else {
            binding.clFab.setBackgroundColor(android.R.color.transparent)

            // when isAllFabsVisible becomes
            // true make all the action name
            // texts and FABs GONE.
            binding.createUser.hide()
            binding.addUser.hide()
            binding.addAlarmActionText.visibility = View.GONE
            binding.addPersonActionText.visibility = View.GONE
            binding.addFab.setIconResource(R.drawable.user_add)
            // Set the FAB to shrink after user
            // closes all the sub FABs
            binding.addFab.shrink()

            // make the boolean variable false
            // as we have set the sub FABs
            // visibility to GONE
            isAllFabsVisible = false
            binding.addFab.visibility= View.VISIBLE
            binding.closeFab.visibility= View.GONE
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onResume() {
        super.onResume()
        Log.e("tewq","yes come in userList fragment")
        if(isRefresh){
            isRefresh=false
            setFabIcon()
            getUserListRequest()
        }
    }
    @RequiresApi(Build.VERSION_CODES.R)
    fun createNewUserPage(){
        val intent= Intent(requireContext(), CreateNewUserActivity::class.java)
        intent.putExtra("premiseName",premiseName)
        intent.putExtra("premise_id",premiseId)
        intent.putExtra("isAdmin",isShowAdmin.toString())
        resultLauncher.launch(intent)
    }
    @RequiresApi(Build.VERSION_CODES.R)
    override fun homePostionClick(postion: Int) {
        Log.e("original value dash== ",userListPremise!!.data!!.staffdetails!![postion].userActiveStatus.toString())
        userActiveStatus=userListPremise!!.data!!.staffdetails!![postion].userActiveStatus.toString()
        userActiverName=userListPremise!!.data!!.staffdetails!![postion].firstName.toString()
        when (userActiveStatus) {
            "true" -> {
                userActiveStatus= InActivate
                showAlertBox("true",userListPremise!!.data!!.staffdetails!![postion].userId.toString())
            }
            "false" -> {
                userActiveStatus= Activate
                showAlertBox("false",userListPremise!!.data!!.staffdetails!![postion].userId.toString())
            }
            else -> {
                userActiveStatus=""
                showAlertBox("",userListPremise!!.data!!.staffdetails!![postion].userId.toString())
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
        val content = view.findViewById<View>(R.id.dialog_text) as TextView
        val mobileNum = view.findViewById<View>(R.id.phoneNumber) as TextView
        val tittle = view.findViewById<View>(R.id.dialog_tittle_text) as TextView
        val cancel = view.findViewById<View>(R.id.cancel_action) as Button
        val sw_status = view.findViewById<View>(R.id.sw_status) as SwitchCompat
        cancel.text="CANCEL"
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
                        val phone = mobileNum.text.toString().trim()
                        Log.e("print123",phone)
                        ok.text="ADD"
                        otpType = "add_user_to_premise"
                        progress_circular.visibility =View.VISIBLE
                        mobileNumberStr=mobileNum.text.toString()
                        addUserRequest(mobileNum.text.toString(),swStatus)
                        alert.dismiss();
                    }else{
                        Log.e("print123",swStatus)
                        mobileNum.error = getString(R.string.validMobileNumber)
                    }
                }
            }
            "false"->{
                content.visibility =View.VISIBLE
                add_user_ll.visibility =View.GONE
                sw_status.visibility =View.GONE
                cancel.visibility =View.GONE
                ok.text="Yes"
                tittle.text = ActiveInActivate
                content.text="Do you want to set $userActiverName as $userActiveStatus?"
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
                ok.text="Yes"
                tittle.text = ActiveInActivate
                content.text="Do you want to set $userActiverName as $userActiveStatus?"
                ok.setOnClickListener {
                    updateUserStatus(response,"false")
                    alert.dismiss()
                }
            }

            "api-success"->{
                // automatically close the AlertDialog after 2 seconds
                val handler = Handler()
                handler.postDelayed({
                    if (alert.isShowing) {
                        alert.dismiss()
                    }
                }, 2000)
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
                // automatically close the AlertDialog after 2 seconds
                val handler = Handler()
                handler.postDelayed({
                    if (alert.isShowing) {
                        alert.dismiss()
                    }
                }, 2000)
                tittle.visibility=View.VISIBLE
                tittle.text="Alert!"
                content.text=response
                ok.text= "Done"
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
                requireContext(),data,getUserModel()!!.data!!.userId.toString(), this,
                userListPremise!!.data!!.userDetails!!.userAccessLevel!!
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
                    setHomeOfferData(userListPremise!!.data!!)
                    if(userListPremise!!.data!!.userDetails!!.userAccessLevel!!.toLowerCase() !="admin"){
                        binding.addFab.visibility =View.GONE
                        isShowAdmin=false
                    }else if(userListPremise!!.data!!.userDetails!!.userAccessLevel!!.toLowerCase() =="admin"){
                        binding.addFab.visibility =View.VISIBLE
                        isShowAdmin=true
                    }
                    setUserAdapter(userListPremise!!.data!!.staffdetails)
                    premiseName=userListPremise!!.data!!.premiseDetails!!.premiseName.toString()

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
                userActiveDeactiveResponse = apiResponseManager.response as UpdateUserStatus
                if(userActiveDeactiveResponse!!.statusCode== SUCCESS_CODE){
                    getUserListRequest()
                    showAlertBox("api-success",userActiveDeactiveResponse!!.message.toString())
                }else{
                    showAlertBox("api-failure",userActiveDeactiveResponse!!.message.toString())}
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun sendVerificationCode(number: String, response: JSONObject) {
        pd.show()
        //this method is used for getting OTP on user phone number.
        PhoneAuthUtil.sendVerificationCode(
            requireContext(),
            requireActivity(),
            number, {
                // Success callback, navigate to the next activity or perform other actions
                forceResendingTokenGbl = PhoneAuthUtil.getForceResendingToken()
                pd.dismiss()
                    val getStartedIntent = Intent(requireContext(), OtpVerifyActivity::class.java)
                    getStartedIntent.putExtra("verificationId", PhoneAuthUtil.getVerificationId())
                    getStartedIntent.putExtra("phone_number", mobileNumberStr)
                    getStartedIntent.putExtra("otp_type",otpType)
                    getStartedIntent.putExtra("premiseID",premiseId)
                    getStartedIntent.putExtra("status",swStatus)
                    getStartedIntent.putExtra("premise_id",premiseId)
                    resultLauncher.launch(getStartedIntent)

            },{ errorMessage ->
                // Error callback, show a message or handle the error accordingly
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
            })
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


    private fun setHomeOfferData(dataModel: PremiseUserList.Data) {
        profileViewModel.setProfileData(dataModel!!.userDetails!!.userImage.toString(),
            dataModel.userDetails!!.userFirstName.toString(),
            dataModel.userDetails!!.userLastName.toString(),
            dataModel.userDetails!!.userStatus.toString(),
            dataModel.premiseDetails!!.premiseName +", "+dataModel.premiseDetails!!.city+", "+dataModel.premiseDetails!!.state,
        )

    }



}
