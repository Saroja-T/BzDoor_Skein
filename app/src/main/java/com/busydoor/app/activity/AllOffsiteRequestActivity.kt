package com.busydoor.app.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.busydoor.app.R
import com.busydoor.app.adapter.OffsiteListAdapter
import com.busydoor.app.apiService.ApiInitialize
import com.busydoor.app.apiService.ApiRequest
import com.busydoor.app.apiService.ApiResponseInterface
import com.busydoor.app.apiService.ApiResponseManager
import com.busydoor.app.customMethods.ALL_REQUEST_OFFSITE
import com.busydoor.app.customMethods.PrefUtils
import com.busydoor.app.customMethods.SUCCESS_CODE
import com.busydoor.app.customMethods.activity
import com.busydoor.app.customMethods.convertDate
import com.busydoor.app.customMethods.isOnline
import com.busydoor.app.databinding.ActivityAllOffsiteRequestsBinding
import com.busydoor.app.interfaceD.HomeClick
import com.busydoor.app.model.RequestAllOffsiteResponse
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AllOffsiteRequestActivity : ActivityBase(),ApiResponseInterface, HomeClick {
    private var userSelectedDate: String?= null
    private var premiseID: String?= null
    private var displayCurrentDate: String?= null
    private var requestAllDataGet: RequestAllOffsiteResponse? = null
    /**  Set binding the this page **/
    val binding by lazy { ActivityAllOffsiteRequestsBinding.inflate(layoutInflater) }
    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        objSharedPref = PrefUtils(this)
        activity = this@AllOffsiteRequestActivity
        premiseID= intent.getStringExtra("premise_id")
        userSelectedDate= intent.getStringExtra("userSelectDate")
        displayCurrentDate = convertDate(userSelectedDate,"yyyy-MM-dd","EEE - dd MMM',' yyyy")
        binding.offsiteSelectedDate.text= displayCurrentDate
        /**Initially call the api when the page is start... **/
        getAllRequest(userSelectedDate!!)
        /** show date picker onclick... **/
        binding.calendarIcon.setOnClickListener{
            showDatePicker()
        }
        /** Navigate to back page... **/
        binding.requestOffcite.backToDonut.setOnClickListener{
            finish()
        }
        binding.RequestOffsiteIcon.setImageResource(R.drawable.icon_request)
        /** Navigate to request page... **/
        binding.RequestOffsiteIcon.setOnClickListener{
            startActivity(
                Intent(
                    this,
                    RequestOffsiteActivity::class.java
                ).putExtra("userSelectDate", userSelectedDate).putExtra("premise_id", premiseID)
            )
        }
    }

    /** when the user select the view replay to list that data will get from adapter and to show to the alert box **/
    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.P)
    override fun homePostionClick(postion: Int) {
        Log.e("original value dash== ",requestAllDataGet!!.data[postion].commentsByApprover.toString())
        if(requestAllDataGet!!.data[postion].timePermissionStatus.toString()=="approved"){
           if(requestAllDataGet!!.data[postion].commentsByApprover !=null)
            showAlertBox(requestAllDataGet!!.data[postion].timePermissionStatus.toString(),requestAllDataGet!!.data[postion].commentsByApprover.toString())

        }else if(requestAllDataGet!!.data[postion].timePermissionStatus.toString()=="rejected"){
            if(requestAllDataGet!!.data[postion].commentsByApprover !=null)
            showAlertBox(requestAllDataGet!!.data[postion].timePermissionStatus.toString(),requestAllDataGet!!.data[postion].commentsByApprover.toString())

        }else{
            if(requestAllDataGet!!.data[postion].commentsByApprover !=null)
            showAlertBox("viewReason",requestAllDataGet!!.data[postion].commentsByApprover.toString())
        }

    }

    /** Show alert box with the different condition bases to shows the contents here... **/
    @RequiresApi(Build.VERSION_CODES.P)
    private fun showAlertBox(type: String, reason: String){
        val dialog = AlertDialog.Builder(this)
        val view: View = layoutInflater.inflate(com.busydoor.app.R.layout.custom_alert_dialog, null)
        dialog.setView(view)
        dialog.setCancelable(true)
        val alert = dialog.create()
        alert.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val content = view.findViewById<View>(com.busydoor.app.R.id.dialog_text) as TextView
        val tittle = view.findViewById<View>(com.busydoor.app.R.id.dialog_tittle_text) as TextView
        val cancel = view.findViewById<View>(com.busydoor.app.R.id.cancel_action) as Button
        cancel.setOnClickListener { alert.dismiss() }
        val ok = view.findViewById<View>(com.busydoor.app.R.id.ok_action) as Button
        cancel.visibility = View.GONE
        content.textAlignment= View.TEXT_ALIGNMENT_TEXT_START
        content.text = reason
        content.background= getDrawable(R.drawable.view_reason_back)
        ok.text = "Done"
        when(type){
            "viewReason"-> {
                tittle.text = "Reason for Approving"
                if(reason!=null){content.text = reason}
                ok.setOnClickListener {
                    alert.dismiss();
                }
            }
            "approved"->{
                tittle.text = "Reason for Approving"
                if(reason!=null){content.text = reason}
                ok.setOnClickListener {
                    alert.dismiss();
                }
            }
            "rejected"->{
                tittle.text = "Reason for Rejecting"
                if(reason!=null){content.text = reason}
                ok.setOnClickListener {
                    alert.dismiss();
                }
            }
        }
        alert.show()
    }

    /** Show datePicker **/
    @SuppressLint("NewApi")
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
//                displayCurrentDate= outputFormat.format(inputFormat.parse(formattedDate))
                displayCurrentDate= convertDate(formattedDate,"yyyy-MM-dd","EEE - dd 'th' MMM',' yyyy")
                binding.offsiteSelectedDate.text=displayCurrentDate
                getAllRequest(userSelectedDate!!)
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

    /** Api response for all api **/
    override fun getApiResponse(apiResponseManager: ApiResponseManager<*>) {
        requestAllDataGet = apiResponseManager.response as RequestAllOffsiteResponse
        if (requestAllDataGet!!.statusCode== SUCCESS_CODE){
            if(requestAllDataGet!!.data.isNotEmpty()) {
                Log.e("dataz",requestAllDataGet!!.data.toString())
                setListOfAllRequestData(requestAllDataGet!!.data as ArrayList<RequestAllOffsiteResponse.Data>)
            }else{
                Log.e("dataz","null")
                binding.rvRequestAll.visibility = View.GONE
                binding.noData.visibility =View.VISIBLE
                binding.noData.text=requestAllDataGet!!.message
            }
        }
        else{
            Log.e("dataz","null")
            binding.rvRequestAll.visibility = View.GONE
            binding.noData.visibility =View.VISIBLE
            binding.noData.text=requestAllDataGet!!.message
        }

    }
    /** Api request for get all data fun... **/
    @RequiresApi(Build.VERSION_CODES.R)
    fun getAllRequest(date:String) {
        try {
            if (isOnline(this)) {
                    Log.e("apiCalled", " ap")
                    ApiRequest(
                        this,
                        ApiInitialize.initialize(ApiInitialize.LOCAL_URL).getRequestOffsiteDetails(
                            "Bearer ${getUserModel()!!.data.token}",
                            encrypt(premiseID.toString()),
                            encrypt(date)
                        ),
                        ALL_REQUEST_OFFSITE,
                        true,
                        this
                    )
            }else {
                Log.e("Application", "offline")
            }
        } catch (e: Exception) {
            Log.e("APIEXceptions", e.toString())

        }

    }

    /** get data from api and send data to Adapter and show the list fun... **/
    private fun setListOfAllRequestData(data: ArrayList<RequestAllOffsiteResponse.Data>) {
        Log.e("setHomeOfferData","")
        if (data.size > 0) {
            binding.rvRequestAll.setHasFixedSize(true)
            val layoutManager =
                LinearLayoutManager(this@AllOffsiteRequestActivity, LinearLayoutManager.VERTICAL, false)
            binding.rvRequestAll.layoutManager = layoutManager
            val treatment_reason =
                OffsiteListAdapter(
                    this@AllOffsiteRequestActivity,
                    data,this
                )
            // Create adapter object
            binding.rvRequestAll.adapter = treatment_reason
            binding.rvRequestAll.visibility = View.VISIBLE
            binding.noData.visibility = View.GONE
            treatment_reason.notifyDataSetChanged()

        } else {
            binding.noData.visibility = View.VISIBLE
            binding.rvRequestAll.visibility = View.GONE
        }
    }
}