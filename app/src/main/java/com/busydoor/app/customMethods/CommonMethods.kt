package com.busydoor.app.customMethods


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.*
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Base64
import android.util.Log
import android.view.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDialog
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@SuppressLint("SimpleDateFormat")
fun convertDate(date: String?, input_formate: String, output_formate: String): String {
    try {
        val input_formate = SimpleDateFormat(input_formate)
        val date1 = input_formate.parse(date)
        val output_formate = SimpleDateFormat(output_formate)
        Log.e("convertDate", "done = " + output_formate.format(date1))
        return output_formate.format(date1)
    } catch (e: Exception) {
        e.printStackTrace()
        Log.e("convertDate", "error = " + e.message)
    }
    return ""
}
fun getProgressDialog(context: Context): AppCompatDialog {
    val myCustomProgressDialog = MyCustomProgressDialog(context)
    myCustomProgressDialog.setCanceledOnTouchOutside(false)
     try {
         myCustomProgressDialog.show()
     }catch  (e: WindowManager.BadTokenException) {
         Log.e("WindowManagerBad ", e.toString())
     }
    return myCustomProgressDialog
}

var builder: AlertDialog.Builder? = null
lateinit var objSharedPref: PrefUtils

fun String.decode(): String {
    return Base64.decode(this, Base64.NO_WRAP).toString(charset("UTF-8"))
}
@RequiresApi(Build.VERSION_CODES.R)
fun isOnline(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (connectivityManager != null) {
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                return true
            }
        }
    }
    return false


}
fun String.encode(): String {
    return Base64.encodeToString(this.toByteArray(charset("UTF-8")), Base64.NO_WRAP)
}
fun dismissProgressDialog(pd: AppCompatDialog?) {
    try {
        if (pd != null && pd.isShowing) {
            pd.dismiss()
        }
    } catch (_: java.lang.Exception) {
    }
}
fun isNetWork(context: Context): Boolean {
    return isNetworkAvailable(context)
}

fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetworkInfo = connectivityManager.activeNetworkInfo
    return activeNetworkInfo != null
}



private var toast: android.widget.Toast? = null


@SuppressLint("ShowToast")
fun toast(msg: Any?, isShort: Boolean = true, app: Context) {

Log.e("ndf", "dhf")
    msg.let {
        if (toast == null) {
            toast = android.widget.Toast.makeText(
                app,
                msg.toString(),
                android.widget.Toast.LENGTH_SHORT
            )
        } else {
            toast!!.setText(msg.toString())

        }
        toast!!.duration =
            if (isShort) android.widget.Toast.LENGTH_SHORT else android.widget.Toast.LENGTH_LONG
        toast!!.show()
    }
}


fun changeTimeFormat(lastUpdated: String): String {

    Log.e("dateee12", lastUpdated)
    var timeStr = ""
    val tk = StringTokenizer(lastUpdated)
    val time = tk.nextToken()
    val date = lastUpdated.substring(lastUpdated.indexOf('-'), lastUpdated.length)
    Log.e("dateee12", date)
    val sdf = SimpleDateFormat("H:mm")
    val sdfs = SimpleDateFormat("hh:mm a")
    val dt: Date
    try {
        dt = sdf.parse(time)
        Log.e("lastUpdated123", sdfs.format(dt).toString())
        timeStr = sdfs.format(dt).toString() +" "+date
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return timeStr
}

/**
 * Function to setup calendar for every month
 */

fun calculateDifference(fromDate: String,toDate: String): Long {
    // Define the date format
    val dateFormat = SimpleDateFormat("yyyy-MM-dd")
    // Parse the past date string into a Date object
    val pastDate = dateFormat.parse(fromDate)
    val currentDate=dateFormat.parse(toDate)
    // Calculate the difference between current date and past date
    val differenceInMillis = currentDate.time - pastDate.time

    // Convert the time difference to days
    val differenceInDays = TimeUnit.MILLISECONDS.toDays(differenceInMillis)
    println("differenceInDays $differenceInDays")
    return differenceInDays
}

fun calculateDateDifference(pastDateString: String,): Long {
    // Define the date format
    val dateFormat = SimpleDateFormat("yyyy-MM-dd")

    // Get the current date
    val currentDate = Date()
    Log.e("current date",currentDate.toString())

    // Parse the past date string into a Date object
    val pastDate = dateFormat.parse(pastDateString)

    // Calculate the difference between current date and past date
    val differenceInMillis = currentDate.time - pastDate.time

    // Convert the time difference to days
    val differenceInDays = TimeUnit.MILLISECONDS.toDays(differenceInMillis)
    println("differenceInDays $differenceInDays")
    return differenceInDays
}