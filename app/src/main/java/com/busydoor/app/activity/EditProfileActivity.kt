package com.busydoor.app.activity

import ImageUtils
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.busydoor.app.R
import com.busydoor.app.apiService.ApiInitialize
import com.busydoor.app.apiService.ApiRequest
import com.busydoor.app.apiService.ApiResponseInterface
import com.busydoor.app.apiService.ApiResponseManager
import com.busydoor.app.apiService.editUserRequest
import com.busydoor.app.customMethods.ERROR_CODE
import com.busydoor.app.customMethods.GET_USERDETAIL
import com.busydoor.app.customMethods.PermissionUtils
import com.busydoor.app.customMethods.PhoneAuthUtil
import com.busydoor.app.customMethods.PrefUtils
import com.busydoor.app.customMethods.SUCCESS_CODE
import com.busydoor.app.customMethods.UPDATE_USER
import com.busydoor.app.customMethods.forceResendingTokenGbl
import com.busydoor.app.customMethods.isOnline
import com.busydoor.app.customMethods.userSelectedImage
import com.busydoor.app.databinding.ActivityEditProfileBinding
import com.busydoor.app.model.UserDetails
import okhttp3.ResponseBody
import org.json.JSONObject


class EditProfileActivity : ActivityBase(),ApiResponseInterface {
    /**  Set binding the this page **/
    private val binding by lazy { ActivityEditProfileBinding.inflate(layoutInflater) }
    override lateinit var objSharedPref: PrefUtils
    override var cryptLib: CryptLib2? = null
    private var accessLevelList: ArrayList<String>? = null
    private var accessLevelName: String = ""
    private var isSendOtp: Boolean = false
    lateinit var popUpView:View

    @RequiresApi(Build.VERSION_CODES.R)
    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // conform your result code perform api
                getProfile()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*** initialize biding here  */
        setContentView(binding.root)
        setupUI(binding.root)
        objSharedPref = PrefUtils(this)
        cryptLib = CryptLib2()
        accessLevelList = ArrayList()
        getProfile()
        binding.backPagetoUserList.drawerMenu.setOnClickListener {
            finish()
        }

        binding.imageViewCameraIcon.setOnClickListener {
            popUpView = it
            if(checkPermissions()) {
                showActivePopupMenu(it)
            }
        }
        binding.btnUpdate.setOnClickListener {
            if(binding.edMobileNumber.text.toString()!=getUserModel()!!.data.phoneNumber){
//                updateProfile("check")
                isSendOtp=true
            }else{
                //Api update fun here
                updateProfile("edituser")
            }
        }
        accessLevelList!!.add("")
        accessLevelList!!.add("Manager")
        accessLevelList!!.add("Staff")

        /** Access Level set fun here... **/
        when (getUserModel()!!.data.accessLevel.toLowerCase()) {
            "manager" -> {
                Log.e("accessLevelNames",getUserModel()!!.data.accessLevel)
                binding.spAccessLevel.text="Manager"
            }
            "staff" -> {
                Log.e("accessLevelNames",getUserModel()!!.data.accessLevel)
                binding.spAccessLevel.text="Staff"
            }
            "admin" ->{
                Log.e("accessLevelName",getUserModel()!!.data.accessLevel)
                binding.spAccessLevel.text="Admin"
            }
            else -> {
                Log.e("accessLevelNames",getUserModel()!!.data.accessLevel)
                binding.spAccessLevel.text=""

            }
        }
    }


    private fun setSpinner1(spinner: Spinner, list: java.util.ArrayList<String>, defaultValue: String?) {

//        spinner.onItemSelectedListener = this
        val updatedList = ArrayList(list) // Copy the original list to avoid modifying it directly
        defaultValue?.let {
            updatedList.add(0, it) // Add the default value at the beginning of the list
        }
        val adapter = object : ArrayAdapter<String>(
            this@EditProfileActivity,
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
        defaultValue?.let { value ->
            val index = updatedList.indexOf(value)
            if (index != -1) {
                spinner.setSelection(index) // Set the selection to the default value if found
            }
        }
    }


    fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent) {
            binding.spAccessLevel -> {
                accessLevelName = accessLevelList!![position]
                Log.e("accessLevelName",accessLevelName)
            }
        }
    }


    fun onNothingSelected(p0: AdapterView<*>?) {
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun sendVerificationCode(number: String) {
        //this method is used for getting OTP on user phone number.
        PhoneAuthUtil.sendVerificationCode(
            this,
            this@EditProfileActivity,
            number, {
                // Success callback, navigate to the next activity or perform other actions
                forceResendingTokenGbl = PhoneAuthUtil.getForceResendingToken()
                val getStartedIntent = Intent(this, OtpVerifyActivity::class.java)
                getStartedIntent.putExtra("verificationId", PhoneAuthUtil.getVerificationId())
                getStartedIntent.putExtra("phone_number", binding.edMobileNumber.text.toString())
                getStartedIntent.putExtra("firstName", binding.etFirstName.text.toString())
                getStartedIntent.putExtra("lastName", binding.edLastName.text.toString())
                getStartedIntent.putExtra("otp_type","edituser")
                resultLauncher.launch(getStartedIntent)
            },{ errorMessage ->
                // Error callback, show a message or handle the error accordingly
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            })
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun updateProfile(type: String) {
        try {
            if (isOnline(this)) {
                Log.e("updateProfile",binding.etFirstName.text.toString())
                Log.e("updateProfile",binding.edLastName.text.toString())
                Log.e("updateProfile",binding.edMobileNumber.text.toString())
                Log.e("updateProfile", type.toString())
                Log.e("updateProfile", userSelectedImage)
                ApiRequest(
                    this, ApiInitialize.initialize(ApiInitialize.LOCAL_URL).editUser(
                        "Bearer ${getUserModel()!!.data.token}",
                        editUserRequest(
                            encrypt(binding.etFirstName.text.toString()),
                            encrypt(binding.edLastName.text.toString()),
                            encrypt(binding.edMobileNumber.text.toString()),
                            userSelectedImage,
                            type,
                        ),

                    ), UPDATE_USER, true, this
                )
            }else {
                showSnackBar(
                    binding.root,
                    getString(R.string.noInternet),
                    ACTIONSNACKBAR.DISMISS
                )
                Log.e("Application", "offline")
            }
        } catch (e: Exception) {
            Log.e("APIEXceptions", e.toString())
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun getProfile() {
        try {
            if (isOnline(this)) {
                ApiRequest(
                    this, ApiInitialize.initialize(ApiInitialize.LOCAL_URL).getUserDetail(
                        "Bearer ${getUserModel()!!.data.token}",
                    ), GET_USERDETAIL, true, this
                )
            }else {
                showSnackBar(
                    binding.root,
                    getString(R.string.noInternet),
                    ACTIONSNACKBAR.DISMISS
                )
                Log.e("Application", "offline")
            }
        } catch (e: Exception) {
            Log.e("APIEXceptions", e.toString())
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun getApiResponse(apiResponseManager: ApiResponseManager<*>) {
        when (apiResponseManager.type) {
            UPDATE_USER -> {
                val model = apiResponseManager.response as ResponseBody
                val responseValue = model.string()
                val response = JSONObject(responseValue)
                when (response.optInt("status_code")) {
                    SUCCESS_CODE -> {
                        // refresh after updated image
                        if(isSendOtp){
                            sendVerificationCode("+91"+binding.edMobileNumber.text.toString())
                        }else {
                            showMessage(response.optString("message"))
                            getProfile()
                        }
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
            GET_USERDETAIL -> {
                val model = apiResponseManager.response as UserDetails
                if(model.statusCode == SUCCESS_CODE){
                    objSharedPref.putString("userImage",model.data!!.photo.toString())
                    setdataToUI(model.data!!)
                }else{
                   showMessage(model.message.toString())
                }

            }
        }
    }

    private fun setdataToUI(data: UserDetails.Data) {
        userSelectedImage=data!!.originalImageName.toString()
        binding.etFirstName.setText(data!!.firstName.toString())
        binding.edLastName.setText(data!!.lastName.toString())
        binding.edMobileNumber.setText(data!!.phoneNumber.toString())
        binding.spAccessLevel.text = data!!.accessLevel.toString()

        Glide.with(this)
            .load(data!!.photo)
            .timeout(1000)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .fitCenter()
            .into(binding.PremiseStaffImages)
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