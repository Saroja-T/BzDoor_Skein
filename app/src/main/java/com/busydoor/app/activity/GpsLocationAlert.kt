package com.busydoor.app.activity

import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.busydoor.app.R

class GpsDialogActivity : AppCompatActivity() {

    private val REQUEST_ENABLE_GPS = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val title = intent.getStringExtra("dialogTitle") ?: ""
        val message = intent.getStringExtra("dialogMessage") ?: ""
        val isHide = intent.getStringExtra("isClose") ?: ""

        val dialog = AlertDialog.Builder(this)
            .setTitle(title)
            .setCancelable(false)
            .setMessage(message)
            .setPositiveButton("Enable GPS") { _, _ ->
                checkAndRequestGpsPermission()
            }
            .setNegativeButton("Cancel") { _, _ -> finish() } // Close the Activity on Cancel
            .create()
        dialog.show()

        // Once location permission is granted, the popup will be hidden
        if (isHide == "yes") {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
//        Toast.makeText(this, "onResume", Toast.LENGTH_SHORT).show()

    }
    private fun checkAndRequestGpsPermission() {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // GPS is not enabled, request user to enable it
            val enableGpsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivityForResult(enableGpsIntent, REQUEST_ENABLE_GPS)
        } else {
            // GPS is already enabled, finish the activity
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_GPS) {
            if (resultCode == RESULT_OK) {
                // User enabled GPS, finish the activity
                finish()
            } else {
                // User canceled or did not enable GPS, handle it accordingly
//                Toast.makeText(this, "GPS was not enabled", Toast.LENGTH_SHORT).show()
                recreate()
            }
        }
    }
}
