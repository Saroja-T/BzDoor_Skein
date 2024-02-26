package com.busydoor.app.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.busydoor.app.R
import com.busydoor.app.apiService.ApiInitialize
import com.busydoor.app.apiService.ApiRequest
import com.busydoor.app.apiService.ApiResponseInterface
import com.busydoor.app.apiService.ApiResponseManager
import com.busydoor.app.customMethods.DatePickerUtil
import com.busydoor.app.customMethods.PrefUtils
import com.busydoor.app.customMethods.REQUEST_OFFSITE
import com.busydoor.app.customMethods.SUCCESS_CODE
import com.busydoor.app.customMethods.activity
import com.busydoor.app.customMethods.convertDate
import com.busydoor.app.customMethods.isOnline
import com.busydoor.app.databinding.ActivityRequestOffsiteBinding
import com.busydoor.app.model.SendRequestOffsiteResponse
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class RequestOffsiteActivity : ActivityBase(),ApiResponseInterface {
    private var startHourMins = MutableLiveData<String>()
    private var endHourMins = MutableLiveData<String>()
    private var requestType: String = ""
    private var userSelectedDate: String?= null
    private var premiseID: String?= null
    private var displayCurrentDate: String?= null
    val outputFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private var requestOffsiteRes: SendRequestOffsiteResponse? = null
    /** biding for this Activity... **/
    private val binding by lazy { ActivityRequestOffsiteBinding.inflate(layoutInflater) }
    private var radioButtonPersonal: RadioButton? = null
    private  var radioButtonOffsite:RadioButton? = null
    private var lastCheckedRadioButton: RadioButton? = null


    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupUI(binding.root)
        objSharedPref = PrefUtils(this)
        activity = this@RequestOffsiteActivity
        premiseID= intent.getStringExtra("premise_id")
        /** Get a date form Home_fragment to this Activity... **/
        userSelectedDate= intent.getStringExtra("userSelectDate")
        /** Set radios buttons to local variables... **/
        radioButtonPersonal = binding.radioButtonPersonal
        radioButtonOffsite = binding.radioButtonOffsite
        setInitialTime()
        // Observe the LiveData
        startHourMins.observe(this, Observer { startTime ->
            // Update UI or perform actions based on the new value of startTime
            Log.d("MainActivity", "New start time: $startTime")
            setTimesToUI(startTime,endHourMins.value.toString())
        })
        endHourMins.observe(this, Observer { endTime ->
            // Update UI or perform actions based on the new value of startTime
            Log.d("MainActivity", "New start time: $endTime")
            setTimesToUI(startHourMins.value.toString(),endTime)
        })


                /** Set "Offsite" radio button as checked by default and api test also set. **/
        radioButtonOffsite!!.isChecked = true
        if (radioButtonOffsite!!.isChecked){
            requestType="offsite"
        }

        // Initially set to Personal
        lastCheckedRadioButton = radioButtonOffsite
        // set group of radioButton
        val radioGroup = binding.radioGroup
        // set on click listener to that radio button
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val checkedRadioButton = findViewById<RadioButton>(checkedId)
            if (checkedRadioButton != null && checkedRadioButton !== lastCheckedRadioButton) {
                lastCheckedRadioButton = checkedRadioButton
                Log.d(
                    "this",
                    checkedRadioButton.text.toString() + " radio button is selected"
                )
                /** Assign value based on selected Radio Button to store local var and to api **/
                if(checkedRadioButton.text.toString().toLowerCase()=="personal"){
                    requestType ="personal"
                }
                if(checkedRadioButton.text.toString().toLowerCase()=="offsite work"){
                    requestType ="offsite"
                }

            }
        }
        /** Set selected data to display **/
        displayCurrentDate = convertDate(userSelectedDate,"yyyy-MM-dd","EEE - dd MMM',' yyyy")
        binding.offsiteDateHeading.text= displayCurrentDate

        // Set a click listener on the "Select Date" button
        binding.calendarIcon.setOnClickListener {
            // Show the DatePicker dialog
            DatePickerUtil.showDatePicker(
                this,
                userSelectedDate!!
            ) { formattedDate ->
                // Update your UI or perform other actions with the selected date
                userSelectedDate = formattedDate;
                // Update the TextView to display the selected date with the format
                displayCurrentDate= convertDate(formattedDate,"yyyy-MM-dd","EEE - dd MMM',' yyyy")
                binding.offsiteDateHeading.text=displayCurrentDate
            }
        }
        /** Navigate to screen back to home. **/
        binding.requestOffciteTool.offsiteBack.setOnClickListener{
            finish();
        }

        /** Start&& End Time onClick function here**/
        binding.etStartHour.setOnClickListener {
            setTimePicker(startHourMins.value.toString(),"start")
        }
        binding.etStartMins.setOnClickListener {
            setTimePicker(startHourMins.value.toString(),"start")
        }
        binding.etEndHour.setOnClickListener{
            setTimePicker(endHourMins.value.toString(),"end")
        }
        binding.etEndMin.setOnClickListener{
            setTimePicker(endHourMins.value.toString(),"end")
        }
        /** offsiteCancel button fun. **/
        binding.offsiteCancel.setOnClickListener{
            showAlertBox("offsiteCancel")
        }
        /** offsiteSend button fun. **/
        binding.offsiteSend.setOnClickListener{
            checkValidation()
        }

    }

    fun setInitialTime() {
        // Get the current time
        val currentTime = Calendar.getInstance().time
        // Calculate the time after adding 15 minutes
        val calendar = Calendar.getInstance().apply {
            time = currentTime
            add(Calendar.MINUTE, 15) // Adding 15 minutes to the current time
        }
        startHourMins.value = convertDate(outputFormat.format(currentTime),"HH:mm:ss","hh:mm a")
        endHourMins.value = convertDate(outputFormat.format(calendar.time),"HH:mm:ss","hh:mm a")
    }

    private fun setTimesToUI(startHourMins: String, endHourMins: String) {
        /** Set Initial Times for both fields to Format the current time using the defined format... **/
        binding.etStartHour.text= startHourMins.substring(0,2)
        binding.etStartMins.text= startHourMins.substring(3,5)
        binding.etEndHour.text= endHourMins.substring(0,2)
        binding.etEndMin.text= endHourMins.substring(3,5)

        /** Check and set a UI for AM/PM background with conditions **/
        if(startHourMins.substring(6,8)=="am"|| startHourMins.substring(6,8) =="AM") {
            binding.buttonAM.setBackgroundResource(R.drawable.pm_bg)
            binding.buttonPM.setBackgroundResource(R.drawable.am_bg)
            binding.buttonPM.setTextColor(ContextCompat.getColor(this, R.color.text_color1))
            binding.buttonAM.setTextColor(ContextCompat.getColor(this, R.color.selectedTextColor))
        }
        if(startHourMins.substring(6,8)=="pm"|| startHourMins.substring(6,8) =="PM"){
            binding.buttonAM.setBackgroundResource(R.drawable.am_bg)
            binding.buttonPM.setBackgroundResource(R.drawable.pm_bg)
            binding.buttonPM.setTextColor(ContextCompat.getColor(this, R.color.selectedTextColor))
            binding.buttonAM.setTextColor(ContextCompat.getColor(this, R.color.text_color1))
        }
        if(endHourMins.substring(6,8)=="am"|| endHourMins.substring(6,8) =="AM"){
            binding.etEndAm.setBackgroundResource(R.drawable.pm_bg)
            binding.etEndPm.setBackgroundResource(R.drawable.am_bg)
            binding.etEndPm.setTextColor(ContextCompat.getColor(this, R.color.text_color1))
            binding.etEndAm.setTextColor(ContextCompat.getColor(this, R.color.selectedTextColor))
        }
        if(endHourMins.substring(6,8)=="pm"|| endHourMins.substring(6,8) =="PM"){
            binding.etEndAm.setBackgroundResource(R.drawable.am_bg)
            binding.etEndPm.setBackgroundResource(R.drawable.pm_bg)
            binding.etEndPm.setTextColor(ContextCompat.getColor(this, R.color.selectedTextColor))
            binding.etEndAm.setTextColor(ContextCompat.getColor(this, R.color.text_color1))
        }
        calculateDifference(startHourMins,endHourMins)


    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun setTimePicker(selectedTime: String, timeStatus: String) {
        var temp:String = convertDate(selectedTime,"hh:mm a","HH:mm:ss")
        var hour:Int = temp.substring(0,2).toInt()
        var minute:Int = temp.substring(3,5).toInt()
            val timePickerDialog = TimePickerDialog(
                this,
                { _, selectedHour, selectedMinute ->
                    val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    // Calculate new time
                    val calendar = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, selectedHour)
                        set(Calendar.MINUTE, selectedMinute)
                    }
                    val newTime = dateFormat.format(calendar.time)
                    println("Selected time: $newTime")
                    if(timeStatus=="start"){
                        startHourMins.value = dateFormat.format(calendar.time)
                        endHourMins.value = addFifteenMins(selectedHour,selectedMinute)
                    }else if(timeStatus == "end"){
                        endTimeValidation(startHourMins.value.toString(),newTime)
                    }
                },
                hour, // Initial hour
                minute, // Initial minute
                false
            )
            timePickerDialog.show()
    }

    private fun addFifteenMins(selectedHour: Int, selectedMinute: Int): String {
        val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        // Display fifteen minutes later time
        val calendar1 = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, selectedHour)
            set(Calendar.MINUTE, selectedMinute)
            add(Calendar.MINUTE,15)
        }
        return dateFormat.format(calendar1.time)
    }

    /** Show alert snackbar to fill start time. **/
    @RequiresApi(Build.VERSION_CODES.R)
//    private fun showTimerAlert(type:String){
//        onStartClickTime("end")
//    }

    /** Check the validation for All fields are fill or not before send api call **/
    @SuppressLint("NewApi")
    fun checkValidation(){
        if(startHourMins.value.toString().isEmpty() && binding.commentsTextView.text!!.length ==0 && endHourMins.value.toString().isEmpty()){
            binding.commentsTextView.error="Please fill Comments";
            showSnackBar(binding.root,"Please fill missed Fields",ACTIONSNACKBAR.DISMISS)
        }else if(startHourMins.value.toString().isEmpty()){
            binding.etStartMins.error="Please select Start Time";
        }else if(binding.commentsTextView.text!!.length.toInt() == 0){
            binding.commentsTextView.error="Please fill Comments";
        }else if(endHourMins.value.toString().isEmpty()){
            binding.etEndHour.error="Please select End Time";
        }else{
            showAlertBox("offsiteSend")
        }
    }

    /** Check the validation for selecting END time **/
    @RequiresApi(Build.VERSION_CODES.R)
    private fun endTimeValidation(startTime: String, endTime: String) {
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        // Parse the time strings into Calendar instances
        val startUpTime = Calendar.getInstance()
        val endUpTime = Calendar.getInstance()
        startUpTime.time = timeFormat.parse(startTime)!!
        endUpTime.time = timeFormat.parse(endTime)!!
        // Compare the times
        val comparison = endUpTime.compareTo(startUpTime)
        if (comparison < 0) {
            showAlertBox("timeBefore");
            //"Second time occurs before the first time"
        } else if (comparison == 0) {
            showAlertBox("timeSame");
            // "Both times are the same"
        } else {
            // Calculate the time difference in milliseconds
            val differenceInMillis = endUpTime.timeInMillis - startUpTime.timeInMillis
            // Convert milliseconds to hours, minutes, and seconds
            val diffSeconds = differenceInMillis / 1000 % 60
            val diffMinutes = differenceInMillis / (60 * 1000) % 60
            val diffHours = differenceInMillis / (60 * 60 * 1000)

            // Assuming diffHours is 12.0 for 12 hours and 10 minutes
            val wholeHours = diffHours.toInt() // Extract the whole hours
            val remainingMinutes = ((diffHours - wholeHours) * 60).toInt()

            Log.e("comparson",diffHours.toString()+remainingMinutes.toString())
            // Check if time difference is above 12 hours and below 10 minutes
            if (wholeHours >= 12 || (wholeHours == 11 && remainingMinutes >= 50)) {
                // Show your alert or Snackbar message
                showAlertBox("12 hours")
            } else if (differenceInMillis < 10 * 60 * 1000) {
                showAlertBox("10 minutes")
            }else{
                endHourMins.value = endTime
            }
            println("Time difference: $diffHours hours, $diffMinutes minutes, $diffSeconds seconds == $endTime")
        }
    }
    private fun calculateDifference(startTime: String,endTime: String){
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        // Parse the time strings into Calendar instances
        val startUpTime = Calendar.getInstance()
        val endUpTime = Calendar.getInstance()
        startUpTime.time = timeFormat.parse(startTime)!!
        endUpTime.time = timeFormat.parse(endTime)!!

        // Calculate the time difference in milliseconds
        val differenceInMillis = endUpTime.timeInMillis - startUpTime.timeInMillis
        // Convert milliseconds to hours, minutes
        val diffMinutes = differenceInMillis / (60 * 1000) % 60
        val diffHours = differenceInMillis / (60 * 60 * 1000)

        /** Set Initial Time Difference Start-End... **/
        binding.offsiteTimeDifferHR.text= "$diffHours"
        binding.offsiteTimeDifferMin.text= "$diffMinutes"

    }

    /** Set UI to Show AlerBox fun with different conditions based contents was shown*/
    @RequiresApi(Build.VERSION_CODES.R)
    fun showAlertBox(type:String){
        val dialog = AlertDialog.Builder(this)
        val view: View = layoutInflater.inflate(com.busydoor.app.R.layout.custom_alert_dialog, null)
        dialog.setView(view)
        dialog.setCancelable(false)
        val alert = dialog.create()
        alert.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val content = view.findViewById<View>(com.busydoor.app.R.id.dialog_text) as TextView
        val tittle = view.findViewById<View>(com.busydoor.app.R.id.dialog_tittle_text) as TextView
        val cancel = view.findViewById<View>(com.busydoor.app.R.id.cancel_action) as Button
        cancel.setOnClickListener { alert.dismiss() }
        val ok = view.findViewById<View>(com.busydoor.app.R.id.ok_action) as Button
        when(type){
            "timeBefore"->{
                cancel.visibility=View.GONE
                tittle.text="Time Alert";
                content.text="Time accures before the start time"
                ok.text="Okay"
                ok.setOnClickListener{
                    alert.dismiss();
                }
            }
            "timeSame"->{
                cancel.visibility=View.GONE
                tittle.text="Time Alert";
                content.text="Both Times are same"
                ok.text="Okay"
                ok.setOnClickListener{
                    alert.dismiss();
                }
            }
            "offsiteSend"->{
                tittle.text="Send Request";
                content.text="Do you want to send the offsite request for approval?"
                ok.setOnClickListener {
                    alert.dismiss()
                    sendRequest()
                }
            }
            "offsiteCancel"->{
                tittle.text="Cancel Request";
                content.text="Do you want to cancel this request? All your changes will be lost."
                ok.setOnClickListener{
                    finish()
                }

            }
            "api-success"->{
                // automatically close the AlertDialog after 2 seconds
                val handler = Handler()
                handler.postDelayed({
                    if (alert.isShowing) {
                        alert.dismiss()
                        if(intent.getStringExtra("home")=="yess"){
                            startActivity(
                                Intent(
                                    this,
                                    AllOffsiteRequestActivity::class.java
                                ).putExtra("userSelectDate", userSelectedDate).putExtra("premise_id", premiseID))
                            finish()
                        }else{
                            val intent=Intent()
                            intent.putExtra("date",userSelectedDate)
                            setResult(RESULT_OK,intent)
                            finish()
                        }
                    }
                }, 2000)
                tittle.text = "Success"
                content.text="Your Offsite request was sent for approval."
                cancel.visibility =View.GONE
                ok.visibility=View.GONE
            }
            "api-failure"-> {
                alert.setCancelable(true)
                tittle.text = "Alert!"
                content.text = requestOffsiteRes!!.message.toString();
                ok.text = "Done"
                cancel.visibility = View.GONE
                ok.setOnClickListener {
                    alert.dismiss()
                }
            }
                "12 hours"->{
                    alert.setCancelable(true)
                    tittle.text = "Alert!"
                    content.text = "Time difference is greater than 12 hours";
                    ok.text = "Done"
                    cancel.visibility = View.GONE
                    ok.setOnClickListener {
                        alert.dismiss()
                    }
                }
                    "10 minutes"->{
                    alert.setCancelable(true)
                    tittle.text="Alert!"
                    content.text="Time difference is less than 10 minutes";
                    ok.text= "Done"
                    cancel.visibility= View.GONE
                    ok.setOnClickListener {
                        alert.dismiss()
                    }
            }
        }
        alert.show()
    }

    /** Send data To Api requestOffsite */
    @RequiresApi(Build.VERSION_CODES.R)
    fun sendRequest() {
        try {
            if (isOnline(this)) {
                    ApiRequest(
                        this, ApiInitialize.initialize(ApiInitialize.LOCAL_URL).sendOffsiteRequest(
                            "Bearer ${getUserModel()!!.data.token}",
                            encrypt(premiseID.toString()),
                            encrypt(userSelectedDate.toString()),
                            encrypt(startHourMins.value.toString()),
                            encrypt(endHourMins.value.toString()),
                            encrypt(binding.commentsTextView.text.toString()),
                            encrypt(requestType)
                        ), REQUEST_OFFSITE, false, this
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

    /** Api response get here */
    @SuppressLint("NewApi")
    override fun getApiResponse(apiResponseManager: ApiResponseManager<*>) {
        requestOffsiteRes = apiResponseManager.response as SendRequestOffsiteResponse
        if(requestOffsiteRes!!.statusCode== SUCCESS_CODE){
            showAlertBox("api-success")
        }else{
            showAlertBox("api-failure")

        }
    }
}