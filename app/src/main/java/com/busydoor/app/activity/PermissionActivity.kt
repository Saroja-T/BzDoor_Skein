package com.busydoor.app.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import com.busydoor.app.R
import com.busydoor.app.customMethods.PrefUtils
import com.busydoor.app.customMethods.objSharedPref
import com.busydoor.app.service.BDApplication

open class PermissionsActivity: AppCompatActivity() {

    val requestPermissionsLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            // Handle Permission granted/rejected
            permissions.entries.forEach {
                val permissionName = it.key
                val isGranted = it.value
                if (isGranted) {
                    Log.d(TAG, "$permissionName permission granted: $isGranted")
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                } else {
                    Log.d(TAG, "$permissionName permission granted: $isGranted")
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                }
            }
        }

    companion object {
        const val TAG = "PermissionsActivity"
    }
}

class PermissionsHelper(val context: Context) {
    // Manifest.permission.ACCESS_BACKGROUND_LOCATION
    // Manifest.permission.ACCESS_FINE_LOCATION
    // Manifest.permission.BLUETOOTH_CONNECT
    // Manifest.permission.BLUETOOTH_SCAN
    fun isPermissionGranted(permissionString: String): Boolean {
        return (ContextCompat.checkSelfPermission(context, permissionString) == PackageManager.PERMISSION_GRANTED)
    }
    fun setFirstTimeAskingPermission(permissionString: String, isFirstTime: Boolean) {
        val sharedPreference = context.getSharedPreferences("org.altbeacon.permisisons",
            AppCompatActivity.MODE_PRIVATE
        )
        sharedPreference.edit().putBoolean(permissionString,
            isFirstTime).apply()
    }

    fun isFirstTimeAskingPermission(permissionString: String): Boolean {
        val sharedPreference = context.getSharedPreferences(
            "org.altbeacon.permisisons",
            AppCompatActivity.MODE_PRIVATE
        )
        return sharedPreference.getBoolean(
            permissionString,
            true
        )
    }
    fun beaconScanPermissionGroupsNeeded(backgroundAccessRequested: Boolean = false): List<Array<String>> {
        val permissions = ArrayList<Array<String>>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // As of version M (6) we need FINE_LOCATION (or COARSE_LOCATION, but we ask for FINE)
            permissions.add(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // As of version Q (10) we need FINE_LOCATION and BACKGROUND_LOCATION
            if (backgroundAccessRequested) {
                permissions.add(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // As of version S (12) we need FINE_LOCATION, BLUETOOTH_SCAN and BACKGROUND_LOCATION
            // Manifest.permission.BLUETOOTH_CONNECT is not absolutely required to do just scanning,
            // but it is required if you want to access some info from the scans like the device name
            // and the aditional cost of requsting this access is minimal, so we just request it
            permissions.add(arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT))
        }



        return permissions
    }

}



class BeaconScanPermissionsActivity: PermissionsActivity()  {
    lateinit var layout: LinearLayout
    private lateinit var permissionGroups: List<Array<String>>
    private lateinit var continueButton: Button
    /** create var for the instance in bd-application... **/
    private lateinit var bdApplication: BDApplication
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private val REQUEST_ENABLE_BT = 1
    private var scale: Float = 1.0f


        get() {
            return this.resources.displayMetrics.density
        }
    private fun enableBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth; handle this case.
        } else {
            if (!(mBluetoothAdapter!!.isEnabled)) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            } else {
                objSharedPref.putBoolean("isBluetoothOn",true)
                // Bluetooth is already enabled; you can start using it.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !objSharedPref!!.getBoolean("isServiceRun")) {
                    // All permissions are granted, proceed with BLE logic
                    bdApplication.blelogic()
                    // Finish the activity after handling permissions and BLE logic
                    finish()
                }
            }
        }
    }

    override fun onBackPressed() {
        // Add your custom behavior here
        // For example, you can navigate to another activity or finish the current activity
        // super.onBackPressed() will still finish the current activity by default
        Log.d(TAG, "Back button pressed")
        // Add your custom behavior here
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layout = LinearLayout(this)
        /** assign the instance for bd-application... **/
        bdApplication = application as BDApplication
        objSharedPref = PrefUtils(this)
        requestLocationEnable(this)
        layout.setPadding(dp(20))
        layout.gravity = Gravity.CENTER
        layout.setBackgroundColor(Color.WHITE)
        layout.orientation = LinearLayout.VERTICAL

        val backgroundAccessRequested = intent.getBooleanExtra("backgroundAccessRequested", true)
        val title = intent.getStringExtra("title") ?: "Permissions Needed for Auto Check-In & Check-Out"
        val message = intent.getStringExtra("message") ?: "Please tap each button to grant the required permissions."
        val continueButtonTitle = intent.getStringExtra("continueButtonTitle") ?: "Continue"
        val permissionButtonTitles = intent.getBundleExtra("permissionBundleTitles") ?: getDefaultPermissionTitlesBundle()

        permissionGroups = PermissionsHelper(this).beaconScanPermissionGroupsNeeded(backgroundAccessRequested)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(dp(0), dp(10), dp(0), dp(10))


        val titleView = TextView(this)
        titleView.gravity = Gravity.CENTER
        titleView.textSize = resources.getDimension(R.dimen._13sdp)
        titleView.text = title
        titleView.layoutParams = params

        layout.addView(titleView)
        val messageView = TextView(this)
        messageView.text = message
        messageView.gravity = Gravity.CENTER
        messageView.textSize = resources.getDimension(R.dimen._6sdp)
        messageView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        messageView.layoutParams = params
        layout.addView(messageView)

        var button: Button
        var buttonTitle:String?=null
        var index = 0
        for (permissionGroup in permissionGroups) {
            button = Button(this)
            buttonTitle = permissionButtonTitles.getString(permissionGroup.first())
            button.id = index
            button.text = buttonTitle
            button.background = resources.getDrawable(R.drawable.btnradious)
            button.layoutParams= params
            button.setOnClickListener(buttonClickListener)
            layout.addView(button)
            index += 1
        }

        continueButton = Button(this)
        continueButton.text = continueButtonTitle
        continueButton.textSize= 15F;
        continueButton.setTextColor(application.resources.getColor(R.color.red)); //TAKE DEFAULT COLOR
        continueButton.background = resources.getDrawable(R.drawable.btnradious)
        continueButton.isEnabled = false
        continueButton.setOnClickListener {
            enableBluetooth()
        }
        continueButton.layoutParams = params
        layout.addView(continueButton)

        setContentView(layout)
    }


    private fun dp(value: Int): Int {
        return (value * scale + 0.5f).toInt()
    }

    private fun requestLocationEnable(context: Context) {
        val locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!isGpsEnabled) {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            context.startActivity(intent)
        }else{
            objSharedPref.putBoolean("isGPS",true)
        }
    }

    private val buttonClickListener = View.OnClickListener { button ->
        val permissionsGroup = permissionGroups[button.id]
        promptForPermissions(permissionsGroup)
    }

    @SuppressLint("InlinedApi")
    fun getDefaultPermissionTitlesBundle(): Bundle {
        val bundle = Bundle()
        bundle.putString(Manifest.permission.ACCESS_FINE_LOCATION, "Location")
        bundle.putString(Manifest.permission.ACCESS_BACKGROUND_LOCATION, "Background Location")
        bundle.putString(Manifest.permission.BLUETOOTH_SCAN, "Bluetooth")
        return bundle
    }


    private fun allPermissionGroupsGranted(): Boolean {
        for (permissionsGroup in permissionGroups) {
            if (!allPermissionsGranted(permissionsGroup)) {
                return false
            }
        }
        return true
    }

    private fun setButtonColors() {
        var index = 0
        for (permissionsGroup in this.permissionGroups) {
            Log.e("setButtonColors", "$index   $permissionsGroup")
            val button = findViewById<Button>(index)
            if (allPermissionsGranted(permissionsGroup)) {
                Log.e("setButtonColors", "allPermissionsGranted")
                val drawable = resources.getDrawable(R.drawable.btnradious)
                // Change the background color by modifying the existing drawable
                drawable.setColorFilter(Color.parseColor("#50c54c"), PorterDuff.Mode.SRC_IN) // Set the color you want
                // Set the modified drawable as the button's background
                button.background = drawable
            }
            else {
                Log.e("setButtonColors", "allPermissionsGranted else")
                val drawable = resources.getDrawable(R.drawable.btnradious)
                // Change the background color by modifying the existing drawable
                drawable.setColorFilter(Color.parseColor("#F03131"), PorterDuff.Mode.SRC_IN) // Set the color you want
                // Set the modified drawable as the button's background
                button.background = drawable
            }
            index += 1
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager =
                getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent()
                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }else{
                objSharedPref!!.putBoolean("isBatteryOpt",true)
            }
        }
    }



    override fun onResume() {
        super.onResume()
        setButtonColors()
        if (allPermissionGroupsGranted()) {
            continueButton.isEnabled = true
            continueButton.setTextColor(Color.WHITE)
            // Get the drawable resource for the button background
            val drawable = resources.getDrawable(R.drawable.btnradious)
            // Change the background color by modifying the existing drawable
            drawable.setColorFilter(Color.parseColor("#4355E3"), PorterDuff.Mode.SRC_IN) // Set the color you want
            // Set the modified drawable as the button's background
            continueButton.background = drawable

            // Request battery optimization when all permissions are granted
            requestBatteryOptimization()
        }
    }

    private fun promptForPermissions(permissionsGroup: Array<String>) {
        if (!allPermissionsGranted(permissionsGroup)) {
            val firstPermission = permissionsGroup.first()

            var showRationale = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                showRationale = shouldShowRequestPermissionRationale(firstPermission)
            }
            if (showRationale ||  PermissionsHelper(this).isFirstTimeAskingPermission(firstPermission)) {
                PermissionsHelper(this).setFirstTimeAskingPermission(firstPermission, false)
                requestPermissionsLauncher.launch(permissionsGroup)
            }
            else {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Can't request permission")
                builder.setMessage("This permission has been previously denied to this app.  In order to grant it now, you must go to Android Settings to enable this permission.")
                builder.setPositiveButton("OK", null)
                builder.show()
            }
        }
    }
    private fun allPermissionsGranted(permissionsGroup: Array<String>): Boolean {
        val permissionsHelper = PermissionsHelper(this)
        for (permission in permissionsGroup) {
            if (!permissionsHelper.isPermissionGranted(permission)) {
                return false
            }
        }
        return true
    }

    companion object {
        const val TAG = "BeaconScanPermissionActivity"
        fun allPermissionsGranted(context: Context, backgroundAccessRequested: Boolean): Boolean {
            val permissionsHelper = PermissionsHelper(context)
            val permissionsGroups = permissionsHelper.beaconScanPermissionGroupsNeeded(backgroundAccessRequested)
            for (permissionsGroup in permissionsGroups) {
                for (permission in permissionsGroup) {
                    if (!permissionsHelper.isPermissionGranted(permission)) {
                        return false
                    }
                }
            }
            return true
        }
    }
}