package com.busydoor.app.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.busydoor.app.R
import com.busydoor.app.adapter.HomeListAdapter
import com.busydoor.app.apiService.ApiInitialize
import com.busydoor.app.apiService.ApiInitialize.ABOUTS_US
import com.busydoor.app.apiService.ApiInitialize.PRIVACY_POLICY
import com.busydoor.app.apiService.ApiInitialize.TERM_AND_CONDITION
import com.busydoor.app.apiService.ApiRequest
import com.busydoor.app.apiService.ApiResponseInterface
import com.busydoor.app.apiService.ApiResponseManager
import com.busydoor.app.customMethods.ACTIVITY_PREMISE_ID
import com.busydoor.app.customMethods.BEACON_DETAILS
import com.busydoor.app.customMethods.CHECK_DIALOG_OPEN_CLOSE
import com.busydoor.app.customMethods.FORCE_LOGOUT
import com.busydoor.app.customMethods.HOME_DATA_GET
import com.busydoor.app.customMethods.LOG_OUT
import com.busydoor.app.customMethods.PrefUtils
import com.busydoor.app.customMethods.STAFF_BLUETOOTH_LOG
import com.busydoor.app.customMethods.SUCCESS_CODE
import com.busydoor.app.customMethods.activity
import com.busydoor.app.customMethods.gContext
import com.busydoor.app.customMethods.isOnline
import com.busydoor.app.databinding.ActivityDashboardBinding
import com.busydoor.app.interfaceD.HomeClick
import com.busydoor.app.model.AttendanceResponse
import com.busydoor.app.model.EmitterDetailsRespons
import com.busydoor.app.model.PremiseResponse
import com.busydoor.app.model.PremiseListModel
import com.busydoor.app.service.BDApplication
import org.altbeacon.beacon.BeaconManager


class DashboardActivity : ActivityBase(),ApiResponseInterface,HomeClick{
    private var dialog: Dialog? = null
    private var sideMenuSelectKey: String = "Home"
    private var homeDataGet: PremiseResponse? = null
    private var checkDialog: Boolean = false
    private var homeListData: ArrayList<PremiseListModel>? = null
    private var macAddressArrayList = ""
    /** create var for the instance in bd-application... **/
    private lateinit var bdApplication: BDApplication
    private val binding by lazy { ActivityDashboardBinding.inflate(layoutInflater) }

    /** OnCreate this method is initially called and set all data and UI... **/
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /** assign the instance in setContentView... **/
        setContentView(binding.root)

        objSharedPref = PrefUtils(this)
        /** assign the instance in bd-application... **/
        bdApplication = application as BDApplication
        /** assign the DashboardActivity in global variable... **/
        activity = this@DashboardActivity
        homeListData = ArrayList()
        Log.e("setHomeOfferData","")
        homeDataGet();
        binding.DashboardAppbar.drawerMenu.setOnClickListener{
            setDrawerDialog();
        }
        binding.DashboardAppbar.refreshBtn.setOnClickListener{
            onRefresh()
        }
    }

    override fun homePostionClick(postion: Int) {
        ACTIVITY_PREMISE_ID = homeDataGet!!.data[postion].premiseId.toString()
        startActivity(
            Intent(
                this@DashboardActivity,
                BottomNavigationBarActivity::class.java
            ).putExtra("premiseId",homeDataGet!!.data[postion].premiseId.toString())
        )
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun showAlertBoxLogout(){
        val dialog = AlertDialog.Builder(this)
        val view: View = layoutInflater.inflate(com.busydoor.app.R.layout.custom_alert_dialog, null)
        dialog.setView(view)
        val alert = dialog.create()
        alert.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val cancel = view.findViewById<View>(com.busydoor.app.R.id.cancel_action) as Button
        val title = view.findViewById<View>(com.busydoor.app.R.id.dialog_tittle_text) as TextView
        val content = view.findViewById<View>(com.busydoor.app.R.id.dialog_text) as TextView
        title.text= "Logout"
        content.text="Are your sure do you want to logout from the application?"
        cancel.setOnClickListener { alert.dismiss() }
        val ok = view.findViewById<View>(R.id.ok_action) as Button
        ok.setOnClickListener {
            logOutApi()
            alert.dismiss()
            objSharedPref.putBoolean(getString(R.string.isLogin), false)
            Log.e("QQQQQ=== ","logout "+objSharedPref.getString(getString(R.string.userResponse)).toString())

        }
        alert.show()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun staticBeaconOut(
    ) {
        try {
            if (isOnline(this)) {
                ApiRequest(
                    this,ApiInitialize.initialize(ApiInitialize.LOCAL_URL).staffBluetoothLog(
                        "Bearer ${getUserModel()!!.data.token}",
                        "","","",
                        encrypt("logout"),
                        "Android"
                    ), STAFF_BLUETOOTH_LOG, false,this
                )
            } else {
                showSnackBar(
                    binding.root,
                    getString(R.string.noInternet),
                    ACTIONSNACKBAR.DISMISS
                )
            }
        } catch (e: Exception) {
            Log.e("APIEXceptions", e.toString())
            showSnackBar(
                binding.root,
                e.toString(),
                ACTIONSNACKBAR.DISMISS)
        }

    }

    /** stopBackgroundService fun of ble_logic... **/
    @RequiresApi(Build.VERSION_CODES.R)
    private fun stopBackgroundService(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            bdApplication.stopForegroundService()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun logOutApi(
    ) {
        try {
            if (isOnline(this)) {
                Log.e("nnnnnnnnnnnnnn","${getUserModel()!!.data.token}.toString()")
                ApiRequest(
                    this,ApiInitialize.initialize(ApiInitialize.LOCAL_URL).logOut(
                        "Bearer ${getUserModel()!!.data.token}"
                    ), LOG_OUT, true,this
                )
            } else {
                showSnackBar(
                    binding.root,
                    getString(R.string.noInternet),
                    ACTIONSNACKBAR.DISMISS
                )
            }
        } catch (e: Exception) {
            Log.e("APIEXceptions", e.toString())
            showSnackBar(
                binding.root,
                e.toString(),
                ACTIONSNACKBAR.DISMISS)
        }

    }
    /** restartApplication fun here Its used to start a BD-application ble_logic... **/
    @RequiresApi(Build.VERSION_CODES.R)
    fun restartApplication() {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            bdApplication.blelogic()
            finish()
        }
    }

    /** page refresh functionality... **/
    @RequiresApi(Build.VERSION_CODES.R)
    fun onRefresh() {
            homeDataGet()
    }

    /** this function is used to back the app... **/
    override fun onBackPressed() {
        super.onBackPressed()
    }

    /** onResume function ... **/
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onResume() {
        super.onResume()
        gContext = this@DashboardActivity
    }
    fun showDialogeBox(context: Context,status: String) {
        // Create and show the dialog using Intent to create an Activity:
        val dialogIntent = Intent(context, DialogActivity::class.java)
        dialogIntent.putExtra("dialogTitle", "Bluetooth Disabled")
        dialogIntent.putExtra("dialogMessage", "Auto Check-In and Check-Out requires Bluetooth. Please enable it.")
        dialogIntent.putExtra("isClose", status)
        // Add flags to ensure the Activity is created on top of other activities and
        // doesn't get added to the back stack:
        dialogIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        context.startActivity(dialogIntent)
    }
    fun showGpsDialog(context: Context,Status:String) {
        val intent = Intent(context, GpsDialogActivity::class.java)
        intent.putExtra("dialogTitle", "Enable GPS")
        intent.putExtra("dialogMessage", "Auto Check-In and Check-Out requires GPS. Enable GPS?")
        intent.putExtra("isClose", Status)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        context.startActivity(intent)
    }
    private fun checkBatteryOptimizationPermissionss() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent()
                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
            else{
                com.busydoor.app.customMethods.objSharedPref.putBoolean("isBatteryOpt",true)
            }
        }
    }

    /** buildAlertMessageAppRestart function when its called after that logout the user... **/
    @RequiresApi(Build.VERSION_CODES.R)
    private fun buildAlertMessageAppRestart() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("To ensure proper beacon monitoring, please restart the application after any reconfiguration or changes.")
            .setCancelable(false)
            .setPositiveButton(
                "Yes"
            ) { dialog, id ->
                (objSharedPref.putBoolean("IsRestart",false))
                var beaconManager = BeaconManager.getInstanceForApplication(this)
                val beaconRegions = beaconManager.monitoredRegions
                var beaconRegionsLength = beaconRegions.size
                finishAffinity()
                restartApplication()
            }
        val alert = builder.create()
        alert.show()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun homeDataGet() {
        if (isOnline(this@DashboardActivity)) {

            ApiRequest(
                this,
                ApiInitialize.initialize(ApiInitialize.LOCAL_URL).homeDataGet(
                    "Bearer " + getUserModel()!!.data.token
                ),
                HOME_DATA_GET, true, this
            )
            Log.e("Request","check if inside log");
        } else {
            showSnackBar(
                binding.llHomeDashbord,
                getString(R.string.noInternet),
                ACTIONSNACKBAR.DISMISS
            )
        }
    }

    /** getApiResponse function show all the premise and emitters list get here ... **/
    @SuppressLint("NewApi")
    override fun getApiResponse(apiResponseManager: ApiResponseManager<*>) {
        Log.e("setHomeOfferData","")
        when (apiResponseManager.type) {
            HOME_DATA_GET -> {
                homeDataGet = apiResponseManager.response as PremiseResponse
                if (homeDataGet!!.statusCode == SUCCESS_CODE) {
                    Log.e("setHomeOfferDatass", homeDataGet!!.data.toString())
                    if (homeDataGet!!.data.isNotEmpty()) {
                        binding.PremiseListView.visibility = View.VISIBLE
                        binding.premiseNoData.visibility = View.GONE
                        setHomeOfferData(homeDataGet!!.data as ArrayList<PremiseResponse.Data>)
                    } else {
                        binding.premiseNoData.text = homeDataGet!!.message
                        binding.premiseNoData.visibility = View.VISIBLE
                        binding.PremiseListView.visibility = View.GONE
                    }
                    getEmittersList()
                }
            }

            BEACON_DETAILS -> {
                val emittersDetailsModel = apiResponseManager.response as EmitterDetailsRespons
                if (emittersDetailsModel.statusCode == 200) {
                    if (emittersDetailsModel.data != null && emittersDetailsModel.data.size > 0) {
                        val userIdRes =
                            objSharedPref.getString(getString(R.string.userResponse)).toString()
                        var arrayList = ""
                        var nameSpaceArrayList = ""
                        var instanceArrayList = ""
                        for (data in emittersDetailsModel.data) {
                            arrayList = arrayList + data.beaconDeviceId + ","
                            nameSpaceArrayList = nameSpaceArrayList + data.nameSpaceId + ","
                            instanceArrayList = instanceArrayList + data.instanceId + ","
                            macAddressArrayList =
                                macAddressArrayList + data.beaconDeviceAddress + ","
                        }

                        if (macAddressArrayList.isNotEmpty() && !(BeaconScanPermissionsActivity.allPermissionsGranted(
                                this,
                                true
                            ))
                        ) {
                            val intent = Intent(this, BeaconScanPermissionsActivity::class.java)
                            intent.putExtra("backgroundAccessRequested", true)
                            startActivity(intent)
                        } else if (macAddressArrayList.isNotEmpty() && (BeaconScanPermissionsActivity.allPermissionsGranted(
                                this,
                                true
                            )) && !(objSharedPref.getBoolean("isServiceRun"))
                        ) {
                            buildAlertMessageAppRestart()

                        }
                        objSharedPref.putString(
                            "beaconMacAddress",
                            macAddressArrayList.substring(0, macAddressArrayList.length - 1)
                        )

                    } else {
                        objSharedPref.putString("beaconMacAddress", "")

                        if ((objSharedPref.getBoolean("isServiceRun"))) {
                            stopBackgroundService()
                        }
                    }
                }
            }

            LOG_OUT -> {
                var forceLogout = apiResponseManager.response as AttendanceResponse
                if (forceLogout.statusCode == SUCCESS_CODE) {
                    setUserDataEmptyAndNavigation()
                    showMessage(forceLogout.message.toString())
                } else {
                    showMessage(forceLogout.message.toString())
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun setUserDataEmptyAndNavigation(){
        // Once check Service run
        if ((objSharedPref.getBoolean("isServiceRun"))) {
            // stop the service after user log out..
            stopBackgroundService()
            staticBeaconOut()
        }
        // set empty to beaconMacAddress data for stop monitoring that regions...
        objSharedPref.putString("beaconMacAddress", "")
        // set empty to user data.. and deviceId
        objSharedPref.putString(getString(R.string.userResponse), "")
        objSharedPref.putString("deviceId", "")
        // Navigate to Login Page
        val getStartedIntent = Intent(this@DashboardActivity, LoginHomeActivity::class.java)
        startActivity(getStartedIntent)
        finishAffinity()
    }


    /** Once getApiResponse function will called ang get data then create list to that data fun here ... **/
    private fun setHomeOfferData(data: ArrayList<PremiseResponse.Data>) {
        Log.e("setHomeOfferData","")
        if (data.size > 0) {
            binding.PremiseListView.setHasFixedSize(true)
            val layoutManager =
                LinearLayoutManager(this@DashboardActivity, LinearLayoutManager.VERTICAL, false)
            binding.PremiseListView.layoutManager = layoutManager
            val homeListAdapter =
                HomeListAdapter(
                    this@DashboardActivity,
                    data, this
                )
            // Create adapter object
                binding.PremiseListView.adapter = homeListAdapter
                binding.PremiseListView.visibility = View.VISIBLE
                binding.premiseNoData.visibility = View.GONE
            homeListAdapter.notifyDataSetChanged()

        } else {
                binding.premiseNoData.visibility = View.VISIBLE
                binding.PremiseListView.visibility = View.GONE
        }
    }

    /** Side menu design and function ... **/
    @RequiresApi(Build.VERSION_CODES.R)
    private fun setDrawerDialog() {
        dialog = Dialog(this@DashboardActivity, R.style.MyDialogTheme)
        dialog!!.setContentView(R.layout.sidemenu_drawer_dialog)
        dialog!!.setCancelable(true)
        dialog!!.setCanceledOnTouchOutside(true)
        dialog!!.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        dialog!!.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        val close = dialog!!.findViewById<ImageView>(R.id.ic_close_side_menu)


        val tv_side_menu_privacy_polivy =dialog!!.findViewById<ImageView>(R.id.tv_side_menu_privacy_polivy)
        val tv_side_menu_term_condition =dialog!!.findViewById<ImageView>(R.id.tv_side_menu_term_condition)
        val tv_side_menu_abouts =dialog!!.findViewById<ImageView>(R.id.tv_side_menu_abouts)
        val tv_side_menu_contacts =dialog!!.findViewById<ImageView>(R.id.tv_side_menu_contacts)
        val tv_side_menu_home =dialog!!.findViewById<ImageView>(R.id.tv_side_menu_home)
        val tv_side_menu_logout = dialog!!.findViewById<ImageView>(R.id.tv_side_menu_logout)
        val tv_side_manage_users =dialog!!.findViewById<ImageView>(R.id.tv_side_manage_users)

        tv_side_menu_logout.setOnClickListener {
            dialog!!.dismiss()
            showAlertBoxLogout()
        }
        tv_side_manage_users.setOnClickListener {
//            startActivity(Intent(this@DashboardActivity, UserListActivity::class.java))

            sideMenuSelectKey = "list_users"
            checkDialog = false
            objSharedPref.putBoolean(CHECK_DIALOG_OPEN_CLOSE, checkDialog)
            dialog!!.dismiss()
        }
        tv_side_menu_home.setOnClickListener {
            sideMenuSelectKey = "home"
            checkDialog = false
            objSharedPref.putBoolean(CHECK_DIALOG_OPEN_CLOSE, checkDialog)
            dialog!!.dismiss()
        }
        tv_side_menu_privacy_polivy.setOnClickListener {

            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(PRIVACY_POLICY)
            startActivity(i)
            sideMenuSelectKey = "privacy_policy"

            checkDialog = false
            objSharedPref.putBoolean(CHECK_DIALOG_OPEN_CLOSE, checkDialog)
            dialog!!.dismiss()
        }
        tv_side_menu_term_condition.setOnClickListener {

            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(TERM_AND_CONDITION)
            startActivity(i)

            sideMenuSelectKey = "term_condition"
            checkDialog = false
            objSharedPref.putBoolean(CHECK_DIALOG_OPEN_CLOSE, checkDialog)
            dialog!!.dismiss()
        }
        tv_side_menu_contacts.setOnClickListener {
//            startActivity(Intent(this@DashboardActivity, ContactsUsActiivty::class.java))
            sideMenuSelectKey = "contacts"
            checkDialog = false
            objSharedPref.putBoolean(CHECK_DIALOG_OPEN_CLOSE, checkDialog)
            dialog!!.dismiss()
        }
        /*privacy policy*/
        tv_side_menu_abouts.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(ABOUTS_US)
            startActivity(i)

            sideMenuSelectKey = "abouts"
            checkDialog = false
            objSharedPref.putBoolean(CHECK_DIALOG_OPEN_CLOSE, checkDialog)
            dialog!!.dismiss()
        }

        when (sideMenuSelectKey) {
            "home" -> {
                tv_side_menu_home.performClick()
            }
            "privacy_policy" -> {
                tv_side_menu_privacy_polivy.performClick()
            }
            "term_condition" -> {
                tv_side_menu_term_condition.performClick()
            }
            "contacts" -> {
                tv_side_menu_contacts.performClick()
            }
            "abouts" -> {
                tv_side_menu_abouts.performClick()
            }
            "list_user" -> {
                tv_side_manage_users.performClick()
            }
        }

        close.setOnClickListener {
            checkDialog = false
            objSharedPref.putBoolean(CHECK_DIALOG_OPEN_CLOSE, checkDialog)
            dialog!!.dismiss()
        }
        checkDialog = true
        objSharedPref.putBoolean(CHECK_DIALOG_OPEN_CLOSE, checkDialog)
        dialog!!.show()
    }

    /** getEmittersList Api call function ... **/
    @SuppressLint("NewApi")
    private fun getEmittersList() {
        Log.e("eee123", getUserModel()!!.data.token)
        if (isOnline(this@DashboardActivity)) {
            ApiRequest(
                this,
                ApiInitialize.initialize(ApiInitialize.LOCAL_URL).getEmitterDetails(
                    "Bearer " + getUserModel()!!.data.token,
                ),
                BEACON_DETAILS, false, this
            )
        } else {
            showSnackBar(
                binding.llHomeDashbord,
                getString(R.string.noInternet),
                ACTIONSNACKBAR.DISMISS
            )
        }
    }
}