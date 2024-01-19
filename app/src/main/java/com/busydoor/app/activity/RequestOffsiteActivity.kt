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
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.busydoor.app.apiService.ApiInitialize
import com.busydoor.app.apiService.ApiRequest
import com.busydoor.app.apiService.ApiResponseInterface
import com.busydoor.app.apiService.ApiResponseManager
import com.busydoor.app.customMethods.PrefUtils
import com.busydoor.app.customMethods.REQUEST_OFFSITE
import com.busydoor.app.customMethods.SUCCESS_CODE
import com.busydoor.app.customMethods.activity
import com.busydoor.app.customMethods.convertDate
import com.busydoor.app.customMethods.isOnline
import com.busydoor.app.databinding.ActivityRequestOffsiteBinding
import com.busydoor.app.model.SendRequestOffsiteResponse
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class RequestOffsiteActivity : ActivityBase(),ApiResponseInterface {
    private var selectedHour: Int? =null
    private var selectedMinute: Int? = null
    private var startHourMins: String = ""
    private var requestType: String = ""
    private var endHourMins: String = ""
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
        objSharedPref = PrefUtils(this)
        activity = this@RequestOffsiteActivity
        premiseID= intent.getStringExtra("premise_id")
        /** Get a date form Home_fragment to this Activity... **/
        userSelectedDate= intent.getStringExtra("userSelectDate")
        /** Set radios buttons to local variables... **/
        radioButtonPersonal = binding.radioButtonPersonal
        radioButtonOffsite = binding.radioButtonOffsite

        // Get the current time
        val currentTime = Calendar.getInstance().time
        // Calculate the time after adding 15 minutes
        val calendar = Calendar.getInstance().apply {
            time = currentTime
            add(Calendar.MINUTE, 15) // Adding 15 minutes to the current time
        }
        /** Set Initial Times for both fields to Format the current time using the defined format... **/
        binding.etStartHour.text= convertDate(outputFormat.format(currentTime),"HH:mm:ss","hh")
        binding.etStartMins.text= convertDate(outputFormat.format(currentTime),"HH:mm:ss","mm")
        binding.etEndHour.text= convertDate(outputFormat.format(calendar.time),"HH:mm:ss","hh")
        binding.etEndMin.text= convertDate(outputFormat.format(calendar.time),"HH:mm:ss","mm")
        startHourMins= convertDate(outputFormat.format(currentTime),"HH:mm:ss","hh:mm a")
        endHourMins= binding.etEndHour.text.toString()+":"+binding.etEndMin.text.toString()+" "+convertDate(outputFormat.format(currentTime),"HH:mm:ss","a")

        /** Set Initial Time Difference Start-End... **/
        binding.offsiteTimeDifferHR.text="00"
        binding.offsiteTimeDifferMin.text="15"
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
                if(checkedRadioButton.text.toString().toLowerCase()=="personal request"){
                    requestType ="personal"
                }
                if(checkedRadioButton.text.toString().toLowerCase()=="offsite request"){
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
            showDatePicker()
        }
        /** Navigate to screen back to home. **/
        binding.requestOffciteTool.offsiteBack.setOnClickListener{
            finish();
        }

        /** Start&& End Time onClick function here**/
        binding.etStartHour.setOnClickListener {

            onStartClickTime("start")
        }
        binding.etStartMins.setOnClickListener {
            onStartClickTime("start")
        }
        binding.etEndHour.setOnClickListener{
            showTimerAlert("end")
        }
        binding.etEndMin.setOnClickListener{
            showTimerAlert("end")
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

    /** Show alert snackbar to fill start time. **/
    @RequiresApi(Build.VERSION_CODES.R)
    private fun showTimerAlert(type:String){
        if(selectedHour ==null && selectedHour ==null){
            val snackbar = Snackbar.make(binding.root, "Please Update Start Time", Snackbar.LENGTH_SHORT)
            // Show the Snackbar
            snackbar.show()
        }else{
            onStartClickTime("end")
        }

    }

    /** Check the validation for All fields are fill or not before send api call **/
    @SuppressLint("NewApi")
    fun checkValidation(){
        if(startHourMins.isEmpty() && binding.commentsTextView.text!!.length ==0 && endHourMins.isEmpty()){
            binding.commentsTextView.error="Please fill Comments";
            showSnackBar(binding.root,"Please fill missed Fields",ACTIONSNACKBAR.DISMISS)
        }else if(startHourMins.isEmpty()){
            binding.etStartMins.error="Please select Start Time";
        }else if(binding.commentsTextView.text!!.length.toInt() == 0){
            binding.commentsTextView.error="Please fill Comments";
        }else if(endHourMins.isEmpty()){
            binding.etEndHour.error="Please select End Time";
        }else{
            showAlertBox("offsiteSend")
        }
    }

    /** Show the DatePicker With current Month days only Showing Condition **/
    private fun showDatePicker() {
        // Create a Calendar instance for the current date
        val calendar = Calendar.getInstance()

        // Create a DatePickerDialog with current year, month, and day as default selections
        val datePickerDialog = DatePickerDialog(
            this,
            { datePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                // Create a new Calendar instance to hold the selected date
                val selectedDate = Calendar.getInstance().apply {
                    // Set the selected date using the values received from the DatePicker dialog
                    set(year, monthOfYear, dayOfMonth)
                }

                // Create a SimpleDateFormat to format the date as "dd/MM/yyyy"
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                // Format the selected date into a string
                val formattedDate = dateFormat.format(selectedDate.time)

                // Update the TextView to display the selected date with the "Selected Date: " prefix
                userSelectedDate = formattedDate;
                // Update the TextView to display the selected date with the format
                displayCurrentDate= convertDate(formattedDate,"yyyy-MM-dd","EEE - dd 'th' MMM',' yyyy")
                binding.offsiteDateHeading.text=displayCurrentDate
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Set the maximum date to the current date within the current month
        datePickerDialog.datePicker.maxDate = calendar.timeInMillis

        // Set the minimum date to the first day of the current month
        val minCalendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
        }
        datePickerDialog.datePicker.minDate = minCalendar.timeInMillis

        // Show the DatePicker dialog
        datePickerDialog.show()
    }

    /** Check the type and selecting START And END times **/
    @RequiresApi(Build.VERSION_CODES.R)
    private fun onStartClickTime(type: String) {
        val cal = Calendar.getInstance()
        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            if(type=="start"){
                binding.etStartHour.text = SimpleDateFormat("hh").format(cal.time)
                binding.etStartMins.text = SimpleDateFormat("mm").format(cal.time)
                /** Check and set a UI for AM/PM with conditions **/
                if(SimpleDateFormat("a").format(cal.time)=="am"||SimpleDateFormat("a").format(cal.time)=="AM"){
                    binding.buttonAM.setBackgroundResource(com.busydoor.app.R.drawable.pm_bg)
                    binding.buttonPM.setBackgroundResource(com.busydoor.app.R.drawable.am_bg)
                }
                if(SimpleDateFormat("a").format(cal.time)=="pm"||SimpleDateFormat("a").format(cal.time)=="PM"){
                    binding.buttonPM.setBackgroundResource(com.busydoor.app.R.drawable.pm_bg)
                    binding.buttonAM.setBackgroundResource(com.busydoor.app.R.drawable.am_bg)
                }

                selectedHour=SimpleDateFormat("HH").format(cal.time).toInt()
                selectedMinute=SimpleDateFormat("mm").format(cal.time).toInt()
                startHourMins=SimpleDateFormat("hh:mm a").format(cal.time).toString()
                if(selectedHour !=null && selectedMinute !=null){
                    /** If Start time was selected automatically add 15  ins to end time fun here **/
                    addFifteenMinutes()
                }

            }else if(type=="end"){
                endHourMins=SimpleDateFormat("hh:mm a").format(cal.time).toString()
                Log.e("enddd",startHourMins+endHourMins)
                /** Check the validation for selecting START And END times Validation fun to call **/
                endTimeValidation(startHourMins.toString(),endHourMins.toString(),cal)
            }
        }
        /** Show the time picker time showing added time on that picker after selecting Start Time
         * So that Condition was check and showing the time on time picker **/
        if(type=="start"){
            TimePickerDialog(this, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show()
        }else{
            cal.add(Calendar.MINUTE, 15)
            TimePickerDialog(this, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show()
        }
    }

    /** Check the validation for selecting END time **/
    @RequiresApi(Build.VERSION_CODES.R)
    private fun endTimeValidation(startTime: String, endTime: String, cal: Calendar) {
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        val time1Str = startTime //"12:05 PM" Example time 1
        val time2Str = endTime //12:10 PM" // Example time 2

        // Parse the time strings into Calendar instances
        val startUpTime = Calendar.getInstance()
        val endUpTime = Calendar.getInstance()

        startUpTime.time = timeFormat.parse(time1Str)!!
        endUpTime.time = timeFormat.parse(time2Str)!!

        // Compare the times
        val comparison = endUpTime.compareTo(startUpTime)

        if (comparison < 0) {
            showAlertBox("timeBefore");
            //"Second time occurs before the first time"
        } else if (comparison == 0) {
            showAlertBox("timeSame");
            // "Both times are the same"
        } else {
            // "Second time is after the first time"
            binding.etEndHour.text = SimpleDateFormat("hh").format(cal.time)
            binding.etEndMin.text = SimpleDateFormat("mm").format(cal.time)
            // Calculate the time difference in milliseconds
            val differenceInMillis = endUpTime.timeInMillis - startUpTime.timeInMillis

            // Convert milliseconds to hours, minutes, and seconds
            val diffSeconds = differenceInMillis / 1000 % 60
            val diffMinutes = differenceInMillis / (60 * 1000) % 60
            val diffHours = differenceInMillis / (60 * 60 * 1000)

            println("Time difference: $diffHours hours, $diffMinutes minutes, $diffSeconds seconds")
            /** Set UI to Show Time Difference to selected Start ANd END times With show Hours Mins With Conditions added prefix 0 or Not */
            binding.offsiteTimeDifferHR.text ="$diffHours"
            binding.offsiteTimeDifferMin.text = "$diffMinutes"

            /** Set UI to Show Time with AM/PM BGColor */
            if(SimpleDateFormat("a").format(cal.time)=="am"||SimpleDateFormat("a").format(cal.time)=="AM"){
                binding.etEndAm.setBackgroundResource(com.busydoor.app.R.drawable.pm_bg)
                binding.etEndPm.setBackgroundResource(com.busydoor.app.R.drawable.am_bg)
            }
            if(SimpleDateFormat("a").format(cal.time)=="pm"||SimpleDateFormat("a").format(cal.time)=="PM"){
                binding.etEndPm.setBackgroundResource(com.busydoor.app.R.drawable.pm_bg)
                binding.etEndAm.setBackgroundResource(com.busydoor.app.R.drawable.am_bg)
            }
        }
    }
    /** Add 15 Mins to user selected time */
    private fun addFifteenMinutes() {
        // Create a Calendar instance and set the selected hour and minute
        val calendar = Calendar.getInstance()
        calendar[Calendar.HOUR_OF_DAY] = selectedHour!!
        calendar[Calendar.MINUTE] = selectedMinute!!
        // Add 15 minutes to the selected time
        calendar.add(Calendar.MINUTE, 15)

        // Get the updated hour and minute after adding 15 minutes
        val updatedHour = SimpleDateFormat("hh").format(calendar.time)
        val updatedMinute = SimpleDateFormat("mm").format(calendar.time)
        val updatedAmPm=  SimpleDateFormat("a").format(calendar.time)


        // Display the updated time with UI AM/PM
        binding.etEndHour.text= updatedHour.toString()
        binding.etEndMin.text= updatedMinute.toString()
        if(updatedAmPm=="am"||updatedAmPm=="AM"){
            binding.etEndAm.setBackgroundResource(com.busydoor.app.R.drawable.pm_bg)
            binding.etEndPm.setBackgroundResource(com.busydoor.app.R.drawable.am_bg)
        }
        if(updatedAmPm=="pm"||updatedAmPm=="PM"){
            binding.etEndPm.setBackgroundResource(com.busydoor.app.R.drawable.pm_bg)
            binding.etEndAm.setBackgroundResource(com.busydoor.app.R.drawable.am_bg)
        }
        binding.offsiteTimeDifferHR.text="00"
        binding.offsiteTimeDifferMin.text="15"
        endHourMins=SimpleDateFormat("hh:mm a").format(calendar.time).toString()
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
                ok.text="Okey"
                ok.setOnClickListener{
                    alert.dismiss();
                }
            }
            "timeSame"->{
                cancel.visibility=View.GONE
                tittle.text="Time Alert";
                content.text="Both Times are same"
                ok.text="Okey"
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
                    recreate()
                    binding.commentsTextView.text!!.clear()
                    alert.dismiss()
                }

            }
            "api-success"->{
                tittle.text = "Success"
                content.text="Your Offsite request was sent for approval."
                cancel.visibility =View.GONE
                ok.text= "Done"
                ok.setOnClickListener {
                    finish()
                    startActivity(
                        Intent(
                            this,
                            AllOffsiteRequestActivity::class.java
                        ).putExtra("userSelectDate", userSelectedDate).putExtra("premise_id", premiseID)
                    )
                }
            }
            "api-failure"->{
                alert.setCancelable(true)
                tittle.text=""
                content.text=requestOffsiteRes!!.message.toString();
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
                if (objSharedPref.getString(getString(com.busydoor.app.R.string.userResponse)) != null && objSharedPref.getString(
                        getString(com.busydoor.app.R.string.userResponse)
                    ) != "null" && objSharedPref.getString(getString(com.busydoor.app.R.string.userResponse)) != ""
                ) {
                    Log.e("apiCalled", " yes")
                    Log.d("BDApplication", "the p ${requestType.toString()}")
                    ApiRequest(
                        this, ApiInitialize.initialize(ApiInitialize.LOCAL_URL).sendOffsiteRequest(
                            "Bearer ${getUserModel()!!.data.token}",
                            encrypt(premiseID.toString()),
                            encrypt(userSelectedDate.toString()),
                            encrypt(startHourMins.toString()),
                            encrypt(endHourMins.toString()),
                            encrypt(binding.commentsTextView.text.toString()),
                            encrypt(requestType)
                        ), REQUEST_OFFSITE, false, this
                    )
                }else{
                    finish()
                    startActivity(Intent(this,LoginHomeActivity::class.java))
                }
            }else {
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