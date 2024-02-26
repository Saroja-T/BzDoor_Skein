package com.busydoor.app.activity

import ImageUtils
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.PopupMenu
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SwitchCompat
import androidx.core.text.HtmlCompat
import androidx.lifecycle.ViewModelProvider
import com.busydoor.app.R
import com.busydoor.app.apiService.ApiInitialize
import com.busydoor.app.apiService.ApiRequest
import com.busydoor.app.apiService.ApiResponseInterface
import com.busydoor.app.apiService.ApiResponseManager
import com.busydoor.app.apiService.UserRegistrationRequest
import com.busydoor.app.customMethods.CREATE_NEW_USER
import com.busydoor.app.customMethods.ERROR_CODE
import com.busydoor.app.customMethods.PermissionUtils
import com.busydoor.app.customMethods.PhoneAuthUtil
import com.busydoor.app.customMethods.PrefUtils
import com.busydoor.app.customMethods.SUCCESS_CODE
import com.busydoor.app.customMethods.ValidationHelper
import com.busydoor.app.customMethods.activity
import com.busydoor.app.customMethods.forceResendingTokenGbl
import com.busydoor.app.customMethods.gContext
import com.busydoor.app.customMethods.isOnline
import com.busydoor.app.customMethods.userSelectedImage
import com.busydoor.app.databinding.ActivityNewUserBinding
import com.busydoor.app.fragment.OnUserCreatedListener
import com.busydoor.app.viewmodel.OTPViewModel
import com.google.firebase.auth.FirebaseAuth
import okhttp3.ResponseBody
import org.json.JSONObject
import java.util.Locale


class CreateNewUserActivity : ActivityBase(), ApiResponseInterface, AdapterView.OnItemSelectedListener {
    private var premiseID: String = ""
    private var premiseName: String = ""
    private var accessLevelList: ArrayList<String>? = null
    private var accessLevelName: String = ""
    private var mAuth: FirebaseAuth? = null
    private var swStatus: String = ""
    lateinit var popUpView:View
    private val binding by lazy { ActivityNewUserBinding.inflate(layoutInflater) }
    private lateinit var listener: OnUserCreatedListener
    private lateinit var otpViewModel : OTPViewModel



    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupUI(binding.root)
        gContext = this@CreateNewUserActivity
        activity = this@CreateNewUserActivity
        objSharedPref = PrefUtils(this@CreateNewUserActivity)
        mAuth = FirebaseAuth.getInstance()
        accessLevelList = ArrayList()
        swStatus = "true"
        accessLevelList!!.add("")
        accessLevelList!!.add("Manager")
        accessLevelList!!.add("Staff")
        if(intent.getStringExtra("isAdmin")=="true"){
            accessLevelList!!.add("Admin")
        }

        premiseID = intent?.getStringExtra("premise_id").toString()
        premiseName = intent?.getStringExtra("premiseName").toString()
        Log.e("orui",premiseID)
        otpViewModel = ViewModelProvider(this).get(OTPViewModel::class.java)
        otpViewModel.sethomeData("sdsds")

        val sw_status = findViewById<View>(com.busydoor.app.R.id.sw_status) as SwitchCompat
        sw_status.setOnCheckedChangeListener { buttonView, isChecked ->
            swStatus = if (isChecked) {
                "true"
            } else {
                "false"
            }
        }

        /** set premise name here... **/
        premiseName= getColoredSpanned(premiseName, "#4355E3")
        binding.premiseName.text= HtmlCompat.fromHtml(
            "Create new user to $premiseName",
            HtmlCompat.FROM_HTML_MODE_COMPACT
        )

        binding.imageViewCameraIcon.setOnClickListener {
            popUpView = it
            if(checkPermissions()) {
                showActivePopupMenu(it)
            }
        }

        /** Access Level set fun here... **/
        setSpinner1(binding.spAccessLevel, accessLevelList!!)

        /** Back button fun... **/
        binding.backPagetoUserList.drawerMenu.setOnClickListener {
            finish();
        }
        /** verify button fun here ... **/
        binding.btnRegister.setOnClickListener {
            if (ValidationHelper.isRegistrationDataValid(
                    binding.etFirstName,
                    binding.edLastName,
                    binding.edMobileNumber,
                    binding.spinnerError,
                    accessLevelName,
                    this
                )) {
                // Your registration logic or API call
                registerApi()
            }

        }
    }

    /** timer seconds text color here ... **/
    private fun getColoredSpanned(text: String, color: String): String {
        return "<font color=$color>$text</font>"
    }

    /** onResume fun... **/
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onResume() {
        super.onResume()
        gContext = this@CreateNewUserActivity
    }

    private fun setSpinner1(spinner: Spinner, list: java.util.ArrayList<String>) {

        spinner.onItemSelectedListener = this
        val adapter = object : ArrayAdapter<String>(
            this@CreateNewUserActivity,
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


    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent) {
            binding.spAccessLevel -> {
                accessLevelName = accessLevelList!![position]
                binding.spinnerError.visibility = View.GONE
                Log.e("accessLevelName", accessLevelName)
            }
        }
    }


    private fun showPermissionAlertBox(){
        val dialog = AlertDialog.Builder(this)
        val view: View = layoutInflater.inflate(com.busydoor.app.R.layout.custom_alert_dialog, null)
        dialog.setView(view)
        val alert = dialog.create()
        alert.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val cancel = view.findViewById<View>(com.busydoor.app.R.id.cancel_action) as Button
        val title = view.findViewById<View>(com.busydoor.app.R.id.dialog_tittle_text) as TextView
        val content = view.findViewById<View>(com.busydoor.app.R.id.dialog_text) as TextView
        title.text= "Permission Denied!"
        content.text="Camera permission has been denied,Please make sure want enable access to upload image in your profile"
        cancel.setOnClickListener { alert.dismiss() }
        val ok = view.findViewById<View>(com.busydoor.app.R.id.ok_action) as Button
        ok.text="Setting"
        cancel.text="No,thanks"
        ok.setOnClickListener {
            alert.dismiss()
        }
        alert.show()
    }

    // Override onRequestPermissionsResult to handle the permission request result
    override fun onNothingSelected(p0: AdapterView<*>?) {
    }

    /** If user was a new to u need to register so call this fun... **/
    @RequiresApi(Build.VERSION_CODES.R)
    private fun registerApi() {
        if (isOnline(this@CreateNewUserActivity)) {
            ApiRequest(
                this,
                ApiInitialize.initialize(ApiInitialize.LOCAL_URL).createUser(
                    "Bearer ${getUserModel()!!.data.token}",
                    UserRegistrationRequest(
                        encrypt(binding.etFirstName.text.toString()),
                        encrypt(binding.edLastName.text.toString()),
                        encrypt(binding.edMobileNumber.text.toString()),
                        userSelectedImage,
                        encrypt(accessLevelName.lowercase(Locale.ROOT)),
                        encrypt(premiseID),
                        encrypt(swStatus),
                        encrypt("Android"),
                        encrypt(objSharedPref.getString("FCM_TOKEN")!!),
                        encrypt(timeZoneSet),
                        "check"
                        )
                ),
                CREATE_NEW_USER, true, this
            )
        } else {
            showSnackBar(
                binding.root,
                getString(R.string.noInternet),
                ACTIONSNACKBAR.DISMISS
            )
        }
    }

    /** apiResponse here ... **/
    override fun getApiResponse(apiResponseManager: ApiResponseManager<*>) {
        when (apiResponseManager.type) {

            CREATE_NEW_USER -> {
                val model = apiResponseManager.response as ResponseBody
                val responseValue = model.string()
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
    @SuppressLint("SuspiciousIndentation")
    private fun sendVerificationCode(number: String) {
        //this method is used for getting OTP on user phone number.
        Log.e("login","jio");
        PhoneAuthUtil.sendVerificationCode(
            this,
            this@CreateNewUserActivity,
            number, {
                forceResendingTokenGbl = PhoneAuthUtil.getForceResendingToken()
                val getStartedIntent = Intent(this@CreateNewUserActivity, OtpVerifyActivity::class.java)
                getStartedIntent.putExtra("verificationId", PhoneAuthUtil.getVerificationId())
                getStartedIntent.putExtra("phone_number", binding.edMobileNumber.text.toString())
                getStartedIntent.putExtra("otp_type","add_user")
                getStartedIntent.putExtra("first_name", binding.etFirstName.text.toString())
                getStartedIntent.putExtra("last_name",binding.edLastName.text.toString())
                getStartedIntent.putExtra("accessLevelName",accessLevelName)
                getStartedIntent.putExtra("premise_id",premiseID)
                getStartedIntent.putExtra("userStatus",swStatus)
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
                startActivity(getStartedIntent)
                finish()
            }, { errorMessage ->
                // Error callback, show a message or handle the error accordingly
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()

            })

    }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // Handle the result from the gallery
                val data: Intent? = result.data
                val selectedImageUri = data?.data
                // Do something with the selected image URI
                val base64Img= ImageUtils.convertImageToBase64(this,selectedImageUri!!)
                userSelectedImage= "data:image/jpeg;base64,$base64Img"
                Log.e("image data22", base64Img)
                binding.PremiseStaffImages.setImageURI(selectedImageUri as Uri?)
            }
        }
    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        val chooserIntent = Intent.createChooser(galleryIntent, "Select Image from")
        galleryLauncher.launch(chooserIntent)
    }
    @SuppressLint("SuspiciousIndentation")
    private fun showActivePopupMenu(view: View) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.menu.add("Take a picture")
        popupMenu.menu.add("Gallery")
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.title) {
                "Take a picture" -> {
                    getFromCamera()
                    true
                }
                "Gallery" -> {
                    openGallery()
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private val REQUEST_IMAGE_CAPTURE = 1
    private fun getFromCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as? Bitmap

            binding.PremiseStaffImages.setImageBitmap(imageBitmap)
            // Do something with the selected image URI
            val base64Img= ImageUtils.convertImageToBase64(this,imageBitmap!!)
            userSelectedImage="data:image/jpeg;base64,$base64Img"
            Log.e("image data11",base64Img)
            // Here, you can use the 'imageBitmap' as needed (e.g., display it in an ImageView)
        }
    }
    // Check if the app has camera and storage permissions
    private fun checkPermissions(): Boolean {
        return PermissionUtils.requestCameraAndStoragePermissions(this)
    }
    // Override onRequestPermissionsResult to handle the permission request result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera and storage permissions granted, proceed with camera intent
                if(::popUpView.isInitialized){
                    showActivePopupMenu(popUpView)
                }
            } else {
                // Permissions denied by the user, handle accordingly (e.g., show a message)
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }



}