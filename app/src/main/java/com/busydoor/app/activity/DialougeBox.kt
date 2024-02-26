package com.busydoor.app.activity

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import com.busydoor.app.R
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class DialogActivity : AppCompatActivity() {

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
            .setPositiveButton("Enable Bluetooth") { _, _ ->
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // Request Bluetooth permission
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                        REQUEST_ENABLE_BLUETOOTH
                    )
                } else {
                    // Bluetooth permission already granted, check Bluetooth status
                    checkBluetoothStatus()
                }
            }
            .setNegativeButton("Cancel") { _, _ -> finish() } // Close the Activity on Cancel
            .create()

        dialog.show()

        // Once turn on the permission the popup will be hidden
        if (isHide == "yes") {
            finish()
        }
    }

    private fun checkBluetoothStatus() {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled) {
            // Bluetooth is not enabled, request user to enable it
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BLUETOOTH)
        } else {
            // Bluetooth is already enabled, finish the activity
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            if (resultCode == RESULT_OK) {
                // User enabled Bluetooth, check Bluetooth permission
                checkBluetoothStatus()
            } else {
                // User canceled or did not enable Bluetooth, handle it accordingly
                recreate()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Bluetooth permission granted, check Bluetooth status
                checkBluetoothStatus()
            } else {
                finish()
                Toast.makeText(this, "Permission Was Denied", Toast.LENGTH_SHORT).show()
                // Handle permission denial gracefully, e.g., inform the user why
                // the permission is needed and offer options to retry or provide
                // alternative functionalities
            }
        }
    }

    companion object {
        private const val REQUEST_ENABLE_BLUETOOTH = 1
    }
}
