package com.busydoor.app.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.busydoor.app.R
import com.busydoor.app.apiService.*
import com.busydoor.app.customMethods.*
import com.busydoor.app.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import okhttp3.ResponseBody
import org.json.JSONObject
import java.util.Locale

class RegisterActivity : ActivityBase(), ApiResponseInterface, AdapterView.OnItemSelectedListener {
    private var accessLevelList: ArrayList<String>? = null
    private var accessLevelName: String = ""
    private var mAuth: FirebaseAuth? = null
    private var premiseID: String = ""
    lateinit var popUpView:View
    private val binding by lazy { ActivityRegisterBinding.inflate(layoutInflater) }




    /** Initially set all UI data set here... **/
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupUI(binding.root)
        gContext = this@RegisterActivity
        activity = this@RegisterActivity
        objSharedPref = PrefUtils(this@RegisterActivity)
        mAuth = FirebaseAuth.getInstance()
        accessLevelList = ArrayList()
        accessLevelList!!.add("")
        accessLevelList!!.add("Manager")
        accessLevelList!!.add("Staff")

        premiseID = intent?.getStringExtra("premise_id").toString()

        binding.imageViewCameraIcon.setOnClickListener {
            popUpView = it
            if(checkPermissions()) {
                showActivePopupMenu(it)
            }
        }

        setSpinner1(binding.spAccessLevel,accessLevelList!!)

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

        /** if u want to login just click here ... **/
        binding.tvTermAndCondition.setOnClickListener {
            val loginActivity =Intent(this@RegisterActivity,LoginHomeActivity::class.java)
            startActivity(loginActivity)
            finish()
        }

       objSharedPref.putString("deviceId",getAndroidId(this))
    }

    private fun getAndroidId(context: Context): String {
        Log.e("getAndroidId",
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID).toString())
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
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
        if (isOnline(this@RegisterActivity)) {
            ApiRequest(
                this,
                ApiInitialize.initialize(ApiInitialize.LOCAL_URL).userRegisterFunction(
                    CommonRegistrationRequest(
                encrypt(binding.etFirstName.text.toString()),
                encrypt(binding.edLastName.text.toString()),
                    encrypt(binding.edMobileNumber.text.toString()),
                    userSelectedImage,
                    encrypt(accessLevelName.lowercase(Locale.ROOT)),
                encrypt(DEVICE_TYPE),
                objSharedPref.getString("FCM_TOKEN")!!,
                encrypt(timeZoneSet),
                objSharedPref.getString("deviceId")!!,
                "check")
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

        PhoneAuthUtil.sendVerificationCode(
            this,
            this@RegisterActivity,
            number, {

                forceResendingTokenGbl = PhoneAuthUtil.getForceResendingToken()
                dismissProgress()
                Log.e("OTPPPP", objSharedPref.getString("deviceId")!!)
                val getStartedIntent = Intent(this@RegisterActivity, OtpVerifyActivity::class.java)
                getStartedIntent.putExtra("verificationId",PhoneAuthUtil.getVerificationId())
                getStartedIntent.putExtra("phone_number", binding.edMobileNumber.text.toString())
                getStartedIntent.putExtra("otp_type", "Register")
                getStartedIntent.putExtra("first_name", binding.etFirstName.text.toString())
                getStartedIntent.putExtra("last_name", binding.edLastName.text.toString())
                getStartedIntent.putExtra("accessLevelName", accessLevelName)
                getStartedIntent.putExtra("deviceId", objSharedPref.getString("deviceId"))
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
                startActivity(getStartedIntent)
                finish()
            },{ errorMessage ->
                // Error callback, show a message or handle the error accordingly
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()

            })

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