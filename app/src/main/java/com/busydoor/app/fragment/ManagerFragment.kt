package com.busydoor.app.fragment

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.busydoor.app.R
import com.busydoor.app.activity.ActivityBase
import com.busydoor.app.activity.CryptLib2
import com.busydoor.app.activity.StaffDetailsOnWeekActivity
import com.busydoor.app.apiService.ApiInitialize
import com.busydoor.app.apiService.ApiRequest
import com.busydoor.app.apiService.ApiResponseInterface
import com.busydoor.app.apiService.ApiResponseManager
import com.busydoor.app.customMethods.ENCRYPTION_IV
import com.busydoor.app.customMethods.PrefUtils
import com.busydoor.app.customMethods.SUCCESS_CODE
import com.busydoor.app.customMethods.USER_STAFF_DATA
import com.busydoor.app.customMethods.convertDate
import com.busydoor.app.customMethods.encode
import com.busydoor.app.customMethods.globalDate
import com.busydoor.app.customMethods.isOnline
import com.busydoor.app.customMethods.key
import com.busydoor.app.databinding.FragmentManagerBinding
import com.busydoor.app.model.StaffCountResponse
import com.busydoor.app.model.UserModel
import com.github.mikephil.charting.charts.PieChart
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 */
class ManagerFragment : Fragment(),ApiResponseInterface {
    private var userID: String =""
    private var premiseID: String=""
    private var staffCountData: StaffCountResponse? = null
    lateinit var objSharedPref: PrefUtils
    private var chart: PieChart? = null
    /*Encryption variables*/
    private var cryptLib: CryptLib2? = null
    private lateinit var binding: FragmentManagerBinding

    private var percentage = 0
    private var presentStaffCount = 0
    private var totalStaffCount = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentManagerBinding.inflate(inflater, container, false)
        val root: View = binding.root
        // Inflate the layout for this fragment
        /*** initialize encrypt fun here  */
        cryptLib = CryptLib2()
        /*** initialize shared-preference here  */
        objSharedPref = PrefUtils(requireContext())
        /*** set userId premiseID  here  */
        userID = getUserModel()?.data?.userId.toString()
        premiseID = activity?.intent?.getStringExtra("premiseId").toString()
        Log.e("original value home== ",premiseID.toString())
        staffCountGet(globalDate)

        binding.userProfileView.backPage.setOnClickListener {
            requireActivity().finish();
        }
        binding.calendarIcon.setOnClickListener {
            showDatePicker()
        }

        binding.pgbStaffs.setOnClickListener {
            startActivity(Intent(requireContext(),StaffDetailsOnWeekActivity::class.java).putExtra("premiseId",premiseID))
        }
        binding.offsiteDateTime.text= convertDate(globalDate,"yyyy-MM-dd","EEE - dd 'th' MMM',' yyyy")
        return root
    }


    @SuppressLint("NewApi")
    private fun showDatePicker() {
        // Create a Calendar instance for the current date
        val calendar = Calendar.getInstance()

        // Create a DatePickerDialog with current year, month, and day as default selections
        val datePickerDialog = DatePickerDialog(
            requireActivity(),
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
                // Update the TextView to display the selected date with the format
//                displayCurrentDate= outputFormat.format(inputFormat.parse(formattedDate))
                binding.offsiteDateTime.text= convertDate(formattedDate,"yyyy-MM-dd","EEE - dd 'th' MMM',' yyyy")
                staffCountGet(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Set the maximum date to the current date within the current month
        datePickerDialog.datePicker.maxDate = calendar.timeInMillis

//        // Set the minimum date to the first day of the current month
//        val minCalendar = Calendar.getInstance().apply {
//            set(Calendar.DAY_OF_MONTH, 1)
//        }
//        datePickerDialog.datePicker.minDate = minCalendar.timeInMillis

        // Show the DatePicker dialog
        datePickerDialog.show()
    }
    /*** Function to api staffCountGet api request */
    @RequiresApi(Build.VERSION_CODES.R)
    private fun staffCountGet(date:String) {
        Log.e("original value","Bearer ${getUserModel()!!.data.token}");
        if (isOnline(requireContext())) {
            ApiRequest(
                requireActivity(),
                ApiInitialize.initialize(ApiInitialize.LOCAL_URL).getStaffCount(
                    "Bearer ${getUserModel()!!.data.token}",
                    encrypt(premiseID),
                    encrypt(date),
                ),
                USER_STAFF_DATA, true, this
            )
            Log.e("Request","check if inside log");
        } else {
            Log.e("homeDataGet","no")
            showSnackBar(
                binding.root,
                getString(com.busydoor.app.R.string.noInternet),
                ActivityBase.ACTIONSNACKBAR.DISMISS
            )
        }
    }
    /*** Function to user logged in details */
    private fun getUserModel(): UserModel? {
        val gson = Gson()
        return gson.fromJson(
            objSharedPref.getString(getString(com.busydoor.app.R.string.userResponse))!!, UserModel::class.java
        )
    }
    /*** Function to setup encrypt */
    fun encrypt(value: String): String {
        if (TextUtils.isEmpty(value)) {
            return value.replace('\"', ' ', ignoreCase = false)
        } else {
            val values = cryptLib!!.encrypt(value, key, ENCRYPTION_IV)
            Log.e(
                "TAG",
                "original value == $value " +
                        "encrypted message ==> ${
                            values!!.trimEnd().encode()
                        }"
            )
            return values!!.trimEnd().encode()
        }
    }

    /*** Function to setup all getApiResponse here */
    override fun getApiResponse(apiResponseManager: ApiResponseManager<*>) {
        Log.e("reponsseeee", apiResponseManager.response.toString())
        when (apiResponseManager.type) {
            USER_STAFF_DATA -> {
                if(binding!=null){
                    Log.e("reponsseeee", apiResponseManager.response.toString())
                    staffCountData = apiResponseManager.response as StaffCountResponse
                    if (staffCountData!!.statusCode == SUCCESS_CODE) {
                        if (staffCountData!!.data!! != null) {
                            if (staffCountData!!.data!!.count !=null) {
                                setStaffCountData(staffCountData!!.data!!)
                                presentStaffCount =
                                    staffCountData!!.data!!.count!!.totalPresentCount!!.toInt()
                                totalStaffCount =
                                    staffCountData!!.data!!.count!!.totalCount!!.toInt()

                                binding.staffCountView.text= "$presentStaffCount/$totalStaffCount"
                                if (presentStaffCount != 0 && presentStaffCount != null) {
                                    percentage = ((100 * presentStaffCount)
                                            / totalStaffCount)
                                }
                                binding.pgbStaffs?.progress = percentage.toFloat()
                            } else {
                                chart?.clear()
                            binding.staffCountView.text ="No staffs"
                            }
                        }
                        }

                    }
                }

            }
        }

    /*** Function to setup setProfileData */
    private fun setStaffCountData(dataModel: StaffCountResponse.Data) {

        val circularProgressDrawable = CircularProgressDrawable(requireContext())
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()

        if(dataModel.userdetails !=null) {
            Glide.with(requireContext())
                .load(dataModel.userdetails!!.userImage)
                .placeholder(circularProgressDrawable)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(binding.userProfileView.PremiseStaffImage)
        }else{
            Glide.with(requireContext())
                .load(R.drawable.icon_users)
                .placeholder(circularProgressDrawable)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(binding.userProfileView.PremiseStaffImage)
        }

        when (dataModel.userdetails!!.userStatus) {
            "in" -> {
                binding.userProfileView.staffStatus.setImageResource(R.drawable.icon_staff_profile_in)
            }
            "inout" -> {
                binding.userProfileView.staffStatus.setImageResource(R.drawable.icon_profile_status_inout)
            }
            "out" -> {
                binding.userProfileView.staffStatus.setImageResource(R.drawable.icon_profile_status_out)
            }
            "offline" -> {
                binding.userProfileView.staffStatus.setImageResource(R.drawable.icon_profile_status_offline)
            }
            else -> {
    //            binding.userProfileView.staffStatus.visibility= View.GONE
                binding.userProfileView.staffStatus.setImageResource(R.drawable.icon_profile_status_offline)
    //            binding.userProfileView.staffStatus.setImageResource(R.drawable.icon_profile_status_offline)
            }
        }

        binding.userProfileView.userName.text =dataModel.userdetails!!.userFirstName +" "+staffCountData!!.data!!.userdetails!!.userFirstName;
        binding.userProfileView.userNumber.text =dataModel.premisedetails!!.premiseName+","+ dataModel.premisedetails!!.city+","+dataModel.premisedetails!!.state
    }

    /*** Function to setup showSnackBar */
    private fun showSnackBar(view: View, message: String, action: ActivityBase.ACTIONSNACKBAR) {
        val snackBar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
        snackBar.view.setBackgroundColor(Color.DKGRAY)

        val tv = snackBar.view.findViewById(com.google.android.material.R.id.snackbar_text) as AppCompatTextView
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13f);
        tv.setTextColor(Color.WHITE)
        tv.maxLines = 25

        val snackBaraction = snackBar.view.findViewById(com.google.android.material.R.id.snackbar_action) as AppCompatButton
        snackBaraction.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13f);

        snackBar.setAction(action.actionMessage) {
            when (action.actionMessage) {
                ActivityBase.ACTIONSNACKBAR.DISMISS.actionMessage -> {
                    snackBar.dismiss()
                }
                ActivityBase.ACTIONSNACKBAR.FINISH_ACTIVITY.actionMessage -> {
                    requireActivity().finish()
                }
            }
        }
        snackBar.setActionTextColor(Color.WHITE)
        snackBar.show()
    }

}