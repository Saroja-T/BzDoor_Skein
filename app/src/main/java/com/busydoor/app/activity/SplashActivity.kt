package com.busydoor.app.activity

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.busydoor.app.R
lateinit var dialog : AlertDialog.Builder



class SplashActivity : ActivityBase() {
    private lateinit var mHandler: Handler
    private lateinit var mRunnable: Runnable
    private val screenTimeOut = 2000L
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        askNotificationPermission()
    }
    @RequiresApi(Build.VERSION_CODES.R)
    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                redirectDashBord()
//                Log.e(TAG, "PERMISSION_GRANTED")

            } else {
//                Log.e(TAG, "NO_PERMISSION")
                // Directly ask for the permission
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            redirectDashBord()
        } else {
           showAlertBox("false")
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun showAlertBox(type:String){
        dialog = AlertDialog.Builder(this)
        val view: View = layoutInflater.inflate(R.layout.custom_alert_dialog, null)
        dialog.setView(view)
        dialog.setCancelable(false)
        val alert = dialog.create()
        alert.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val content = view.findViewById<View>(R.id.dialog_text) as TextView
        val tittle = view.findViewById<View>(R.id.dialog_tittle_text) as TextView
        val cancel = view.findViewById<View>(R.id.cancel_action) as Button
        content.visibility = View.VISIBLE
        cancel.visibility = View.VISIBLE
        cancel.text="No,thanks"
        cancel.setOnClickListener {
            redirectDashBord()
            alert.dismiss()  }
        val ok = view.findViewById<View>(R.id.ok_action) as Button
        when(type){
            "false"->{
                ok.text="Go to Settings"
                tittle.text = "Notification"
                content.text="To receive important updates, please enable notifications for Business-i"
                ok.setOnClickListener {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val settingsIntent: Intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                        startActivity(settingsIntent)
                    }
                    alert.dismiss()
                    redirectDashBord()
                }
            }
        }
        alert.show()
    }
    private fun redirectDashBord() {
        mRunnable = Runnable {
            run {
                if (objSharedPref.getBoolean(getString(R.string.isLogin))) {
                    val getStartedIntent =
                        Intent(this@SplashActivity, DashboardActivity::class.java)
                    getStartedIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    getStartedIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    getStartedIntent.putExtra("user_response",objSharedPref.getString(getString(R.string.userResponse)))
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
                    startActivity(getStartedIntent)
                    finish()
                } else {
                    val getStartedIntent =
                        Intent(this@SplashActivity, LoginHomeActivity::class.java)
                    getStartedIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    getStartedIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
                    startActivity(getStartedIntent)
                    finish()
                }

            }
        }
        mHandler = Handler(Looper.getMainLooper())
        mHandler.postDelayed(mRunnable, screenTimeOut)
    }
}