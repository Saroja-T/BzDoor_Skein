package com.busydoor.app.activity

import android.annotation.SuppressLint
import android.app.Activity
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
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
import com.busydoor.app.viewmodel.SharedViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AllOffsiteRequestActivity : ActivityBase(),ApiResponseInterface, HomeClick {
    private var userSelectedDate: String?= null
    private var calenderSelectedDate: String?= null
    private var premiseID: String?= null
    private var displayCurrentDate: String?= null
    private var requestAllDataGet: RequestAllOffsiteResponse? = null

    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.R)
    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
//            getUserListRequest()
          Log.e("tttttttttttttttt1",result.data?.getStringExtra("date").toString())
          val date = result.data?.getStringExtra("date").toString()
            getAllRequest(date)
        }else{
            Log.e("ttttttttttttttt2222",intent.getStringExtra("date").toString())

        }
    }

    /**  Set binding the this page **/
    val binding by lazy { ActivityAllOffsiteRequestsBinding.inflate(layoutInflater) }
    @SuppressLint("NewApi", "SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        objSharedPref = PrefUtils(this)
        activity = this@AllOffsiteRequestActivity
        premiseID= intent.getStringExtra("premise_id")
        userSelectedDate= intent.getStringExtra("userSelectDate")
        calenderSelectedDate=userSelectedDate
        displayCurrentDate = convertDate(userSelectedDate,"yyyy-MM-dd","EEE - dd MMM',' yyyy")
        binding.offsiteSelectedDate.text= displayCurrentDate
        /**Initially call the api when the page is start... **/
        getAllRequest(userSelectedDate!!)
        /** show date picker onclick... **/
        binding.calendarIcon.setOnClickListener{
            showDatePicker(calenderSelectedDate!!)
        }
        /** Navigate to back page... **/
        binding.requestOffcite.backToDonut.setOnClickListener{
            finish()
        }
        binding.RequestOffsiteIcon.setImageResource(R.drawable.icon_request)
        /** Navigate to request page... **/
        binding.RequestOffsiteIcon.setOnClickListener{
            val intent= Intent(this, RequestOffsiteActivity::class.java).putExtra("userSelectDate",userSelectedDate).putExtra("premise_id",premiseID)
            resultLauncher.launch(intent)
        }
    }

    /** when the user select the view replay to list that data will get from adapter and to show to the alert box **/
    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.P)
    override fun homePostionClick(postion: Int) {
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
        val content = view.findViewById<View>(R.id.dialog_text) as TextView
        val tittle = view.findViewById<View>(R.id.dialog_tittle_text) as TextView
        val cancel = view.findViewById<View>(R.id.cancel_action) as Button
        cancel.setOnClickListener { alert.dismiss() }
        val ok = view.findViewById<View>(R.id.ok_action) as Button
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
    @RequiresApi(Build.VERSION_CODES.R)
    private fun showDatePicker(initialDate: String) {
        // Parse the initial date string into year, month, and day
        val initialCalendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        try {
            val parsedDate = dateFormat.parse(initialDate)
            parsedDate?.let {
                initialCalendar.time = it
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // Create a Calendar instance for the current date
        val calendar = Calendar.getInstance()
        // Create a DatePickerDialog with initial year, month, and day
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                // Create a new Calendar instance to hold the selected date
                val selectedDate = Calendar.getInstance().apply {
                    // Set the selected date using the values received from the DatePicker dialog
                    set(year, monthOfYear, dayOfMonth)
                }
                // Create a SimpleDateFormat to format the date as "dd/MM/yyyy"
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                // Format the selected date into a string
                val formattedDate = dateFormat.format(selectedDate.time)
                userSelectedDate = formattedDate;
                calenderSelectedDate=userSelectedDate
                // Update the TextView to display the selected date with the format
                displayCurrentDate= convertDate(formattedDate,"yyyy-MM-dd","EEE - dd MMM',' yyyy")
                binding.offsiteSelectedDate.text=displayCurrentDate
                /*** Function to staffListGet when click datePicker select a date to call api request */
                getAllRequest(userSelectedDate!!)
            },
            initialCalendar.get(Calendar.YEAR),
            initialCalendar.get(Calendar.MONTH),
            initialCalendar.get(Calendar.DAY_OF_MONTH)
        )

        // Set the maximum date to the current date within the current month
        datePickerDialog.datePicker.maxDate = calendar.timeInMillis
        datePickerDialog.show()
    }

    /** get data from api and send data to Adapter and show the list fun... **/
    private fun setListOfAllRequestData(data: ArrayList<RequestAllOffsiteResponse.Data>) {
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

    /** Api response for all api **/
    override fun getApiResponse(apiResponseManager: ApiResponseManager<*>) {
        requestAllDataGet = apiResponseManager.response as RequestAllOffsiteResponse
        if (requestAllDataGet!!.statusCode== SUCCESS_CODE){
            if(requestAllDataGet!!.data.isNotEmpty()) {
                setListOfAllRequestData(requestAllDataGet!!.data as ArrayList<RequestAllOffsiteResponse.Data>)
            }else{
                binding.rvRequestAll.visibility = View.GONE
                binding.noData.visibility =View.VISIBLE
                binding.noData.text=requestAllDataGet!!.message
            }
        }
        else{
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
                showSnackBar(
                    binding.root,
                    getString(R.string.noInternet),
                    ACTIONSNACKBAR.DISMISS
                )
            }
        } catch (e: Exception) {
            showSnackBar(
                binding.root,
                e.toString(),
                ACTIONSNACKBAR.DISMISS)
        }

    }
}