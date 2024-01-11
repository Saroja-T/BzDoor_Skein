package com.busydoor.app.service

import android.annotation.SuppressLint
import android.app.*
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Build
import android.text.TextUtils
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.busydoor.app.R
import com.busydoor.app.customMethods.STAFF_BLUETOOTH_LOG
import com.busydoor.app.customMethods.STAFF_BLUETOOTH_OUT
import com.busydoor.app.customMethods.apiTriggered
import com.busydoor.app.activity.CryptLib2
import com.busydoor.app.activity.MacAddressActivity
import com.busydoor.app.activity.SplashActivity
import com.busydoor.app.apiService.ApiInitialize
import com.busydoor.app.apiService.ApiRequestForBecon
import com.busydoor.app.apiService.ApiResponseManager
import com.busydoor.app.customMethods.DEVICE_TYPE
import com.busydoor.app.customMethods.ENCRYPTION_IV
import com.busydoor.app.customMethods.PrefUtils
import com.busydoor.app.customMethods.encode
import com.busydoor.app.customMethods.isOnline
import com.busydoor.app.customMethods.key
import com.busydoor.app.customMethods.objSharedPref
import com.busydoor.app.model.AttendanceResponse
import com.busydoor.app.model.BeaconStaticOutResponse
import com.busydoor.app.model.UserModel
import com.google.gson.Gson
import org.altbeacon.beacon.*


class BDApplication: Application() {
    private lateinit var beaconManager: BeaconManager
    var i = 0
    private val macRegex = Regex("([0-9a-fA-F]{2}:){5}[0-9a-fA-F]{2}")
    private fun isMac(str: String) = str.matches(macRegex)
    private var cryptLib: CryptLib2? = null
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate() {
        Log.d("BDApplication", "oncreate fun enter")
        super.onCreate()
        objSharedPref = PrefUtils(this)
        cryptLib =
            CryptLib2()
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(mReceiver, filter)
        val locationFilter = IntentFilter(LocationManager.MODE_CHANGED_ACTION)
        registerReceiver(mLocationReceiver, locationFilter)

        /*** Reboot scenario if check user was login and use have an macAddress then only Start service **/
        if (objSharedPref?.getBoolean(getString(R.string.isLogin))!! && objSharedPref?.getString("beaconMacAddress") != null &&
            objSharedPref?.getString("beaconMacAddress") != ""
        ) {
            blelogic()
        }
    }

    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.R)
    fun blelogic() {

        beaconManager = BeaconManager.getInstanceForApplication(this)
        beaconManager.beaconParsers.clear()
        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT))
        startMyOwnForeground("bsboss")
        beaconManager.setEnableScheduledScanJobs(false)
        beaconManager.backgroundBetweenScanPeriod = 60000
        beaconManager.backgroundScanPeriod = 1100
        beaconManager.foregroundScanPeriod = 1100
        beaconManager.foregroundBetweenScanPeriod = 60000
        /*
         check the MacAddress through the dashboard page is empty or not
         is empty means user don't have an beacons permission
        */
        /*** mac list get from @MacAddressActivity ***/
        if (objSharedPref != null && objSharedPref?.getString("beaconMacAddress") != null &&
            objSharedPref?.getString("beaconMacAddress") != ""
        ) {
            Log.d("maclist", objSharedPref!!.getString("beaconMacAddress")!!)
            val macAddressList: List<String> = ArrayList(
                listOf(
                    *objSharedPref!!.getString("beaconMacAddress")!!
                        .split(",").toTypedArray()
                )
            )
            /*** check the mac list format is correct or not ***/
            var isMac= true
            macAddressList.forEach {
                if (!isMac(it)) {
                    isMac = false
                }
            }
            if (isMac) {
                /*** for first time to start monitoring the beacons, create a regions for that beacons
                 * once we create the regions check if it's not created again in same beacons. ***/
                for (macAddress in macAddressList) {
                    Log.e("Before loop", "regionlist size: $macAddress")
                    val beaconRegions = beaconManager.monitoredRegions
                    /*** empty means before yet not created any regions of any beacons ***/
                    Log.e("sdsdsds","${getUserModel()?.data?.token}")
                    if(beaconRegions.isEmpty()){
                        MonitorBeaconsRegion(
                            beaconManager,
                            "Bearer ${getUserModel()?.data?.token}",
                            this@BDApplication,
                            cryptLib,
                            this
                        ).createRegion(macAddress,
                            macAddress)
                    }else {
                        var isRegionAvailable = false
                        var i = 0
                        for (beaconRegion in beaconRegions) {
                            if(beaconRegion.bluetoothAddress == macAddress){
                                isRegionAvailable = true
                            }
                            i++
                        }
                        /*** else part if add a new beacons at the time create regions for that beacons ***/
                        if(!isRegionAvailable && i==beaconRegions.size){
                            MonitorBeaconsRegion(
                                beaconManager,
                                "Bearer ${getUserModel()?.data?.token}",
                                this@BDApplication,
                                cryptLib,
                                this
                            ).createRegion(macAddress,
                                macAddress)
                        }
                    }

                }
            }
        }
    }


    /*** stopForegroundService fun ***/
    @RequiresApi(Build.VERSION_CODES.R)
    fun stopForegroundService() {
        beaconManager = BeaconManager.getInstanceForApplication(this)
        if (beaconManager != null) {
            val beaconRegions = beaconManager.monitoredRegions
            var beaconRegionsLength = beaconRegions.size
            var temp = 0

            for (beaconRegion in beaconRegions) {
                beaconManager.stopMonitoring(beaconRegion)
                beaconManager.stopRangingBeacons(beaconRegion)
                temp++
            }
            beaconManager.removeAllMonitorNotifiers()
            beaconManager.removeAllRangeNotifiers()
        }
        objSharedPref!!.putBoolean("isServiceRun", false)

    }

    /*** startMyOwnForeground fun ***/
    @RequiresApi(Build.VERSION_CODES.R)
    fun startMyOwnForeground(message: String) {
        objSharedPref!!.putBoolean("isServiceRun", true)
        val builder = Notification.Builder(this, "BDApp")
        builder.setSmallIcon(R.drawable.app_icon_foreground)
        builder.setContentTitle("Bi Service to Auto Check-In and Check-Out")
        val intent = Intent(this, SplashActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT + PendingIntent.FLAG_IMMUTABLE
        )
        builder.setContentIntent(pendingIntent)
        val channel = NotificationChannel(
            "bdapp-notification-id",
            "My Notification Name", NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = "My Notification Channel Description"
        val notificationManager = getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        builder.setChannelId(channel.id)
        BeaconManager.getInstanceForApplication(this)
            .enableForegroundServiceScanning(builder.build(), 123)
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
    fun sendEmittersStatus(
        status: String,
        deviceType: String
    ) {
        try {
            if (isOnline(this)) {
                Log.e("apiCalled", " yes")
                ApiRequestForBecon(
                    this,
                    ApiInitialize.initialize(ApiInitialize.LOCAL_URL).staffBluetoothLog(
                        "Bearer ${getUserModel()!!.data.token}",
                        "",
                        "",
                        "",
                        encrypt(status),
                        deviceType
                    ), STAFF_BLUETOOTH_LOG, this
                )
            } else {
                Log.e("Application", "offline")
            }
        } catch (e: Exception) {
            Log.e("APIEXceptions", e.toString())

        }

    }
    /*** getApiResponse of above beacons ***/
    fun getApiResponse(apiResponseManager: ApiResponseManager<*>) {
        when (apiResponseManager.type) {
            STAFF_BLUETOOTH_LOG -> {
                val beaconDetailsModel = apiResponseManager.response as AttendanceResponse
                apiTriggered = true
                Log.e("qas", beaconDetailsModel.statusCode.toString())
                if (beaconDetailsModel != null && beaconDetailsModel.statusCode == 200) {
                    Log.e("STAFF_BLUETOOTH_LOG", "AttendanceResponse Success")
                } else {
                    Log.e("STAFF_BLUETOOTH_LOG", "AttendanceResponse Failed")

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


    /*** getUserModel of logged in user ***/
    fun getUserModel(): UserModel? {
        val gson = Gson()
        return gson.fromJson(
            objSharedPref?.getString(getString(R.string.userResponse)), UserModel::class.java
        )
    }

    /*** mReceiver to bluetooth status monitor ***/

    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.R)
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(
                    BluetoothAdapter.EXTRA_STATE,
                    BluetoothAdapter.ERROR
                )
                when (state) {
                    BluetoothAdapter.STATE_OFF -> {
                        Log.d("Bluapter", "Bluetooth Turned OFF ")
                        sendEmittersStatus("bluetooth_disabled", DEVICE_TYPE)
                        if (isNotificationPermissionGranted(context)){
                            sendNotification("Enable Bluetooth to Auto Check-In and Check-Out")
                        }
                    }

                    BluetoothAdapter.STATE_ON -> {
                        Log.d("Bluapter", "Bluetooth Turned on ")
                        sendEmittersStatus("bluetooth_enabled", DEVICE_TYPE)
                        if (isNotificationPermissionGranted(context)){
                            sendNotification("Auto Check-In and Check-Out is Turned ON")
                        }
                    }
                }
            }
        }
    }

    /*** mLocationReceiver to monitor status to location ***/
    private val mLocationReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.P)
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == LocationManager.MODE_CHANGED_ACTION) {
                val locationManager =
                    context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                if (locationManager.isLocationEnabled) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        sendEmittersStatus("location_enabled", DEVICE_TYPE)
                    }
                    Log.d("Location", "Location turned ON")
                    if (isNotificationPermissionGranted(context)){
                        sendNotification("Location services are now enabled")
                    }
                } else {
                    Log.d("Location", "Location turned OFF")
                    // Perform actions when location is turned off
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        sendEmittersStatus("location_disabled", DEVICE_TYPE)
                    }
                    if (isNotificationPermissionGranted(context)){
                        sendNotification("Location services are now disabled")
                    }
                }
            }
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

    private fun sendNotification(notificationText: String) {
        val contentText = notificationText;
        val builder = NotificationCompat.Builder(this, "bdapp-notification-id")
            .setContentTitle("Business-i")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.app_icon_notification)
        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addNextIntent(Intent(this, SplashActivity::class.java))
        val resultPendingIntent = stackBuilder.getPendingIntent(
            0,
            PendingIntent.FLAG_UPDATE_CURRENT + PendingIntent.FLAG_IMMUTABLE
        )
        builder.setContentIntent(resultPendingIntent)
        val notificationManager =
            this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, builder.build())
    }
}