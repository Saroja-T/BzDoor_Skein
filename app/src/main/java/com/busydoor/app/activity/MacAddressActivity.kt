package com.busydoor.app.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.busydoor.app.customMethods.BEACON_DETAILS
import com.busydoor.app.R
import com.busydoor.app.apiService.ApiInitialize
import com.busydoor.app.apiService.ApiRequest
import com.busydoor.app.apiService.ApiResponseInterface
import com.busydoor.app.apiService.ApiResponseManager
import com.busydoor.app.customMethods.ENCRYPTION_IV
import com.busydoor.app.customMethods.PrefUtils
import com.busydoor.app.customMethods.STAFF_BLUETOOTH_LOG
import com.busydoor.app.customMethods.STAFF_BLUETOOTH_OUT
import com.busydoor.app.customMethods.encode
import com.busydoor.app.customMethods.isOnline
import com.busydoor.app.customMethods.key
import com.busydoor.app.databinding.ActivityMacAddressBinding
import com.busydoor.app.model.BeaconStaticOutResponse
import com.busydoor.app.model.EmitterDetailsRespons
import com.busydoor.app.service.BDApplication
import com.google.android.material.snackbar.Snackbar
import org.altbeacon.beacon.BeaconManager


class MacAddressActivity : AppCompatActivity(), ApiResponseInterface {
    private var objSharedPref: PrefUtils? = null
    /** binding initialize... **/
    private val binding by lazy { ActivityMacAddressBinding.inflate(layoutInflater) }
    /** create var for the instance in bd-application... **/
    private lateinit var bdApplication: BDApplication
    private var macAddressArrayList = ""
    private var cryptLib: CryptLib2? = null
    var StaticToken= "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImp0aSI6ImI2NWU4MDA5ZDg4YjE1YjcwYmIzMTQ4YTM2ZGRkZTg4MjBhNTRkN2RlOWFkOGY3YmZiMzQ4MDVkZWM5YjU3ZmRlYWVlNmMyZTdhYjhlMjY2In0.eyJhdWQiOiIzIiwianRpIjoiYjY1ZTgwMDlkODhiMTViNzBiYjMxNDhhMzZkZGRlODgyMGE1NGQ3ZGU5YWQ4ZjdiZmIzNDgwNWRlYzliNTdmZGVhZWU2YzJlN2FiOGUyNjYiLCJpYXQiOjE3MDE4Njg2ODQsIm5iZiI6MTcwMTg2ODY4NCwiZXhwIjoxNzMzNDkxMDg0LCJzdWIiOiIxIiwic2NvcGVzIjpbXX0.GIO864LTuYeH_nW80IiF1ScssfacTG82ON6vO7qGvzUeV7OhNsvpSrPRnQU_52lbAw6sTG7GCju7D7tV9G_MDyHb0Vf2Cc8vykNy1omR3qUeGjZkGvLCEK1a2RjTiiCrf4tyvFGC16aaFXaNFzNgcDIw9a-zyZHDr-PITy44IvjuxY-1xkLX9yccWxtEUwi2t0BvYJ7sQmJX5darnAeAxOuMGVz2MDd3NR-iAyX-jArCbKcvcrjFAPBSrHoXtF5lvFoLs-J6N5hIlD2qc0HpSAA8DYNiP1vsIZipK-IgY9mpRN-pu7bv44VZE7b07LS7hybj0eIopXdv2kshfl6lUUtKQl_ESVTJxEY_DQyjZog4p6RF5YHDstuERPFQKksH0Zw-JrEaKIPHZWNFsi8CHil-qPe8lEJNDmSmpDSorWQUtdHd9ZHAt03WqjU7FHEVZcfZqi8-4fgzh2s6PY-BrQNE57XVG8uDufJQJa4Jaqr7U3iP1ll2FPOB2LS5Op9xq_mutBkfyEkfzpMlmHD0LPJniCJIaE1zlpR42YGFdc6mwr4UKY_mGyvMgrDwEqfk0jdglxSmAfpOUZcPhYrxsRxJa8M07-Cd7TKcsmfRNTU4NhdZzYSfuTGf0hOSHANkIbNyY6dWRvjFTFQkNMuI_TsdxEIFhTB3w85mE5dQBP8";
    val localToken="eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImp0aSI6IjlhM2E3YThhZDY2ZWFkMzJkZTdhNGY4MGMxMzYzNWQxZTViMGFlOGI3NGI2NjIwMjgyNDMyZTg5NzYwYWRkODZjYWY3MWI5YzA5Y2I4MGI5In0.eyJhdWQiOiIzIiwianRpIjoiOWEzYTdhOGFkNjZlYWQzMmRlN2E0ZjgwYzEzNjM1ZDFlNWIwYWU4Yjc0YjY2MjAyODI0MzJlODk3NjBhZGQ4NmNhZjcxYjljMDljYjgwYjkiLCJpYXQiOjE3MDE3NTM1MTgsIm5iZiI6MTcwMTc1MzUxOCwiZXhwIjoxNzMzMzc1OTE4LCJzdWIiOiIxIiwic2NvcGVzIjpbXX0.Yhx5THi_THSpIsJgxFcEULp2eFIYRZ1pdYy4hB8z7FbZqCwR8rsr6ZsVt8jV4q5PpxKn55o3l3KAydV5y1fwRw_MwE_7Wn1-lmtW_laPqUzbZWcEMoZOqRWmtHG6sdV32Nm6g-tAerRRODftNo5xoojR4DjuOPm5RolkFeRx8JykZXr0VqctH54miwodxYMdn-m9rovku60gSbT0B8pxf_kNERz4s4c5xzJRm7-Z8CWz4XkTk3SnjfWST9YipLRyVk7FYq6XTrtVjyPAh6eheTrB7t5d2hkquZfinVXDonlWMwek5aG-16SBtt_57PuFJyNlIlL6zOn67MmeD9G0_VjK6YinLKKH8J-WCQCO6CBs67dN9a9i8_XwUAPBcOSrTt91d_8475NbAXes7aJ_JR2Di73LFFC0sgC7xGW_oFIGLjzzcW8rPOL5KmsW2fpnQrhrc5j2gskT6eUt6WA7EzR4OUkPK_d-_bsO_T2mFJ1-9VRlB82fCJ-bwPkC7xocqOeMR0yNiZxMfFhsV5BzZ-10NzrLHoeNZjZ17l3qTszA_0BwrVOvvtArfAIDAJmMx74yQAT8uRm-WlO_2g9ukxi5tYocr8rgx_DlhVTBaam-zY4GdSsSL9QpJzrj7ros5A_iVtpuJbuZBDaejjafOx2f2pIIixGi324Cfle5rg8";

    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        objSharedPref = PrefUtils(this)
        /** assign the instance in bd-application... **/
        bdApplication = application as BDApplication
        binding.textContent.text = objSharedPref!!.getString("Beacon-Status")
        cryptLib =
            CryptLib2()
        if (objSharedPref!!.getBoolean("isServiceRun")){
            binding.MacDetailsContent.visibility = View.VISIBLE
            binding.button.text="Stop Monitor"
        }
        getBeaconDeviceList();
        /*** setOnclick fun for Start button ***/
        binding.button.setOnClickListener {
            if (!objSharedPref!!.getBoolean("isServiceRun")){
            /*** check condition for Inputs Mac1 & Mac2 ***/
            if (binding.Mac1.text.length > 15) {
                /*** Call the BDApplication Logic DD:36:02:01:07:78  DD:34:02:06:C0:3D ***/
                /*** save enter details to local storage and use anywhere this details ***/
                objSharedPref!!.putString("mac1",binding.Mac1.text.toString())
                bdApplication.blelogic()
                /*** Snackbar fun for Start button ***/
                val snackbar =
                    Snackbar.make(
                        binding.root,
                        "Start Beacon Services 1. ${binding.Mac1.text}",
                        Snackbar.LENGTH_SHORT
                    )
                snackbar.show()
                /*** Set Visibility for after api call fun ***/
                binding.MacDetailsContent.visibility = View.VISIBLE
                binding.button.text="Stop Service"
            } else {
                Log.e("checkz",objSharedPref!!.getString("mac1").toString() + objSharedPref!!.getString("mac2").toString())
                /*** Snackbar fun for Unsatisfied condition on button click ***/
                val snackbar =
                    Snackbar.make(
                        binding.root,
                        "Please enter Valid Mac Address",
                        Snackbar.LENGTH_LONG
                    )
                snackbar.show()
                binding.textContent.visibility = View.GONE
            }
            }else{
                (objSharedPref!!.putBoolean("isServiceRun",false))
                /*** Stop service its means already run ***/
                buildAlertMessageAppRestart()
//                staticBeaconOut()
//                bdApplication.stopForegroundService()
//                recreate()

            }
        }
        /*** refresh fun ***/
        binding.refresh.setOnClickListener{
            recreate()
        }
    }
    // Check if the notification permission is granted
    private fun isNotificationPermissionGranted(context: Context): Boolean {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        return if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.M) {
            notificationManager.areNotificationsEnabled()
        } else {
            // For versions below Marshmallow, permission is granted by default
            true
        }
    }
    // Request notification permission
    private fun requestNotificationPermission(activity: Activity, requestCode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channel = NotificationChannel(
                "channel_id",
                "Channel Name",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            notificationManager.createNotificationChannel(channel)
        }

        if (!isNotificationPermissionGranted(activity)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, activity.packageName)
                activity.startActivityForResult(intent, requestCode)
            } else {
                // For versions below Oreo, request permission directly
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.ACCESS_NOTIFICATION_POLICY),
                    requestCode
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("Before loop","ondestroy")
        objSharedPref!!.putString("beaconMacAddress","")
        objSharedPref!!.putString("mac2","")
    }


    /*** onResume fun for show permission activity page ***/
    override fun onResume() {
        super.onResume()
//        requestNotificationPermission(this@MacAddressActivity,101)
        if(!BeaconScanPermissionsActivity.allPermissionsGranted(this,true)) {
            val getStartedIntent = Intent(this, BeaconScanPermissionsActivity::class.java)
            startActivity(getStartedIntent)
        }
    }

    @SuppressLint("NewApi")
    private fun getBeaconDeviceList() {
        if (isOnline(this@MacAddressActivity)) {
            ApiRequest(
                this,
                ApiInitialize.initialize(ApiInitialize.LOCAL_URL).getEmitterDetails(
                    "Bearer $StaticToken",
                ),
                BEACON_DETAILS, false, this
            )
        } else {
//            showSnackBar(
//                binding.root,
//                getString(R.string.no_internet),
//                ActivityBase.ACTIONSNACKBAR.DISMISS
//            )
        }
    }


    override fun getApiResponse(apiResponseManager: ApiResponseManager<*>) {
        when (apiResponseManager.type) {
            BEACON_DETAILS -> {
                val beaconDetailsModel = apiResponseManager.response as EmitterDetailsRespons
                if (beaconDetailsModel.statusCode == 200) {
                    if (beaconDetailsModel.data != null && beaconDetailsModel.data.size > 0) {
                        val userIdRes =
                            objSharedPref!!.getString(getString(R.string.userResponse)).toString()
                        var arrayList = ""
                        var nameSpaceArrayList = ""
                        var instanceArrayList = ""
                        for (data in beaconDetailsModel.data) {
                            arrayList = arrayList + data.beaconDeviceId + ","
                            nameSpaceArrayList = nameSpaceArrayList + data.nameSpaceId + ","
                            instanceArrayList = instanceArrayList + data.instanceId + ","
                            macAddressArrayList =
                                macAddressArrayList + data.beaconDeviceAddress + ","
                        }

                        /*** save the mac details to local ***/
                        objSharedPref!!.putString(
                            "beaconMacAddress",
                            macAddressArrayList.substring(0, macAddressArrayList.length - 1)
                        )
                        /*** set a emitter details after get this api data to set ***/
                        binding.Mac1.text= objSharedPref!!.getString(
                            "beaconMacAddress").toString()

                    } else {
                        /*** set a emitter details after get this api data to set ***/
                        binding.Mac1.text= objSharedPref!!.putString(
                            "beaconMacAddress","No Beacons in this User").toString()
//                        objSharedPref.putString("beaconMacAddress", "")
//                        objSharedPref.putString("regionBeaconListTemp", "")
//                        objSharedPref.putString("service_status", "stop")
                    }
                }
            }
            STAFF_BLUETOOTH_OUT -> {
                val beaconStaticOutModel = apiResponseManager.response as BeaconStaticOutResponse
                if (beaconStaticOutModel.statusCode == 200) {
                    Log.e("STAFF_BLUETOOTH_OUT", beaconStaticOutModel.message.toString())
                }
            }
        }
    }

    fun encrypt(value: String): String {
        return if (TextUtils.isEmpty(value)) {
            value.replace('\"', ' ', ignoreCase = false)
        } else {

            val values = cryptLib!!.encrypt(value, key, ENCRYPTION_IV)
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


    @RequiresApi(Build.VERSION_CODES.R)
    fun staticBeaconOut(
    ) {
        try {
            if (isOnline(this)) {
                Log.e("apiCalled", " yes")
                Log.d("BDApplication", "the premises api!!: ${"region"}")
                ApiRequest(
                    this,ApiInitialize.initialize(ApiInitialize.LOCAL_URL).updatestaticout(
                        "Bearer $StaticToken",
                        encrypt("Out"),
                        "Android"
                    ), STAFF_BLUETOOTH_LOG, false,this
                )
            } else {
                Log.e("Application", "offline")
            }
        } catch (e: Exception) {
            Log.e("APIEXceptions", e.toString())

        }

    }


    @RequiresApi(Build.VERSION_CODES.R)
    private fun buildAlertMessageAppRestart() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("To ensure proper beacon monitoring, please restart the application after any reconfiguration or changes.")
            .setCancelable(false)
            .setPositiveButton(
                "Yes"
            ) { dialog, id ->
                (objSharedPref!!.putBoolean("IsRestart",false))
                var beaconManager = BeaconManager.getInstanceForApplication(this);
                val beaconRegions = beaconManager.monitoredRegions
                var beaconRegionsLength = beaconRegions.size
                staticBeaconOut()
                finishAffinity()
                restartApplication()
            }
        val alert = builder.create()
        alert.show()

    }
    @RequiresApi(Build.VERSION_CODES.R)
    fun restartApplication() {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            bdApplication.stopForegroundService()
            finish()
        }
    }
}