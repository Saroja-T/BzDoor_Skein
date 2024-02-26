package com.busydoor.app.service

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.text.TextUtils

import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import com.busydoor.app.customMethods.ENCRYPTION_IV
import com.busydoor.app.customMethods.STAFF_BLUETOOTH_LOG
import com.busydoor.app.customMethods.key
import com.busydoor.app.activity.CryptLib2
import com.busydoor.app.apiService.ApiInitialize
import com.busydoor.app.apiService.ApiRequestForBecon
import com.busydoor.app.customMethods.PrefUtils
import com.busydoor.app.customMethods.encode
import com.busydoor.app.customMethods.isOnline
import com.busydoor.app.model.UserModel
import com.google.gson.Gson
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.MonitorNotifier
import org.altbeacon.beacon.Region
import org.altbeacon.beacon.RegionViewModel


class MonitorBeaconsRegion(beaconManagerParam: BeaconManager,
                           apitoken: String,
                           apicontext: Context,
                           cryptLib: CryptLib2?,
                           apiResponseInterface: BDApplication) {
    lateinit var region: Region
    private var regionName: String  = ""
    private var regionAddress: String = ""
    private val beaconManager: BeaconManager =beaconManagerParam
    private val pagecontext: Context = apicontext
    private val apitokens: String = apitoken
    private var cryptLibs: CryptLib2? = cryptLib
    private val apiResInterface:BDApplication= apiResponseInterface
    private var count: Int = 0
    private var objSharedPref: PrefUtils? = null
    private var temptoken: String? = null
    var isSendapi: Boolean= true

    @RequiresApi(Build.VERSION_CODES.R)
    fun createRegion(  regionNameParam: String,
                       regionAddressParam: String,
    ) {
        Log.e("createRegion","service")
        objSharedPref = PrefUtils(pagecontext)
        cryptLibs = CryptLib2()
        regionName = regionNameParam
        regionAddress = regionAddressParam

        region = Region(regionName, regionAddress)
        /*** These two lines set up a Live Data observer so this Activity can get beacon data from the Application class ***/
        if(!beaconManager.isRegionViewModelInitialized(region)){
            Log.e("isRegionViewModelInitialized","false")
            var regionViewModel:RegionViewModel  = beaconManager.getRegionViewModel(region)
            /*** observer will be called each time the monitored regionState changes(inside vs. outside region)  ***/
            regionViewModel.regionState.observeForever(regionMonitoringObserver)
            /*** observer will be called each time the monitored rangingState changes  ***/
            regionViewModel.rangedBeacons.observeForever(regionRangingObserver)
        }else{
            /*** It mean already regions are created  ***/
            Log.e("isRegionViewModelInitialized","true")
        }
        /*** observer will be called each time a new list of beacons is ranged (typically ~1 second in the foreground) ***/
        startMonitoring()
    }

    /*** getUserModel of logged in user ***/
    fun getUserModel(): UserModel? {
        val gson = Gson()
        return gson.fromJson(
            objSharedPref?.getString("userResponse"), UserModel::class.java
        )
    }

    fun startMonitoring() {
        Log.e("BDApplication","startMonitoring")
        beaconManager.startMonitoring(region)
        beaconManager.startRangingBeacons(region)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun stopMonitoring(region:Region) {
        Log.e("createRegion","stopMonitoring")
        beaconManager.stopMonitoring(region)
        beaconManager.stopRangingBeacons(region)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    val regionMonitoringObserver = Observer<Int> { state ->
        if (state == MonitorNotifier.OUTSIDE) {
            //sendEmittersStatus("${regionAddress.toString()}","0","0","out","Android")
            //sendNotification("You are OutSide Now: ${regionAddress.toString()} ")
        } else if (state == MonitorNotifier.INSIDE){
            //sendEmittersStatus("${regionAddress.toString()}","0","0","in","Android")
            //sendNotification("You are Inside Now: ${regionAddress.toString()} ")
        }
    }

    /*** observer will be create and each time the monitored rangingState was changes  ***/
    @RequiresApi(Build.VERSION_CODES.R)
    private val regionRangingObserver = Observer<Collection<Beacon>> { beacons ->
        Log.d("regionRangingObserver", "Ranged: ${beacons.count()} beacons")
        for (beacon: Beacon in beacons) {
            Log.d("BDApplication", "${beacon.bluetoothAddress} about ${beacon.distance} meters away")
            sendEmittersStatus("${regionAddress.toString()}","${beacon.distance.toString()}","1","in","Android")
        }
    }


    // Check if the notification permission is granted
    private fun isNotificationPermissionGranted(context: Context): Boolean {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.M) {
            Log.e("notify","enabled");
            notificationManager.areNotificationsEnabled()
        } else {
            Log.e("notify","default");
            // For versions below Marshmallow, permission is granted by default
            true
        }
    }

    fun encrypt(value: String): String {
        return if (TextUtils.isEmpty(value)) {
            value.replace('\"', ' ', ignoreCase = false)
        } else {

            val values = cryptLibs!!.encrypt(value, key, ENCRYPTION_IV)
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
    fun sendEmittersStatus(
        deviceId: String,
        distance: String,
        reserved: String,
        status: String,
        deviceType: String
    ) {
        try {
            if (isOnline(pagecontext)) {
                Log.e("apiCalled", " yes")
                Log.d("BDApplication123", "the premises api!!: ${getUserModel()?.data?.token}")
                ApiRequestForBecon(apiResInterface,
                    ApiInitialize.initialize(ApiInitialize.LOCAL_URL).staffBluetoothLog(
                        "Bearer ${getUserModel()?.data?.token}",
                        encrypt(deviceId),
                        encrypt(distance),
                        encrypt(reserved),
                        encrypt(status),
                        deviceType
                    ), STAFF_BLUETOOTH_LOG, apiResInterface
                )
            } else {
                Log.e("Application", "offline")
            }
        } catch (e: Exception) {
            Log.e("APIEXceptions", e.toString())
        }
    }
}
