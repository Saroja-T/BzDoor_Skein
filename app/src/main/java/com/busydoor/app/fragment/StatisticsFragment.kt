package com.busydoor.app.fragment

import MonthYearPickerDialog
import android.Manifest
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Color
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.busydoor.app.R
import com.busydoor.app.activity.ActivityBase
import com.busydoor.app.activity.CryptLib2
import com.busydoor.app.apiService.ApiInitialize
import com.busydoor.app.apiService.ApiRequest
import com.busydoor.app.apiService.ApiResponseInterface
import com.busydoor.app.apiService.ApiResponseManager
import com.busydoor.app.customMethods.FileDownloader
import com.busydoor.app.customMethods.*
import com.busydoor.app.customMethods.PrefUtils
import com.busydoor.app.customMethods.convertDate
import com.busydoor.app.customMethods.globalDate
import com.busydoor.app.databinding.FragmentStatisticsBinding
import com.busydoor.app.interfaceD.HomeClick
import com.busydoor.app.model.StaffGraphDetails
import com.busydoor.app.model.UserModel
import com.busydoor.app.viewmodel.ProfileViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.LargeValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.MPPointF
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


lateinit var chart: BarChart
val values1 = ArrayList<BarEntry>()
val values2 = ArrayList<BarEntry>()
val values3 = ArrayList<BarEntry>()
open class StatisticsFragment : Fragment(), OnChartValueSelectedListener,ApiResponseInterface {
    private lateinit var binding: FragmentStatisticsBinding
    private var REQUEST_WRITE_PERMISSION = 123
    /*Encryption variables*/
    private var cryptLib: CryptLib2? = null
    lateinit var objSharedPref: PrefUtils
    private var userID: String =""
    private var premiseID: String=""
    private var date: String=""
    private var selecetdMonthYear: String=""
    private lateinit var customMarkerView :CustomBarMarkerView
    private lateinit var staffGraphDetailsData:StaffGraphDetails
    private lateinit var selectedDate: Calendar
    private lateinit var profileViewModel: ProfileViewModel


    private val readImagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
                download()
            } else {
                // Explain to the user that the feature is unavailable because the
                // feature requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
                showToastShort(requireContext(), "isGranted false")

            }
        }

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
        binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        // Inflate the layout for this fragment
        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        chart=binding.chart
        /*** initialize encrypt fun here  */
        cryptLib = CryptLib2()
        /*** initialize shared-preference here  */
        objSharedPref = PrefUtils(requireContext())

        userID = getUserModel()?.data?.userId.toString()
        premiseID = activity?.intent?.getStringExtra("premiseId").toString()

        binding.offsiteHeading.text= convertDate(globalDate,"yyyy-MM-dd","EEE, MMM dd, yyyy")

        /*** initialize shared-preference here  */
        staffGraphDataGet(globalDate)
        date= globalDate
        selectedMonthAndYear(globalDate)
        selectedDate = Calendar.getInstance()

        binding.calendarIcon.setOnClickListener{
            showDatePicker(date)
        }

        binding.chart.clear()
        if(binding.chart.data != null)
            binding.chart.data.clearValues()
        binding.chart.description.isEnabled = false
        binding.chart.setBackgroundColor(Color.WHITE)
        binding.chart.setDrawGridBackground(false)
        binding.chart.setDrawBarShadow(false)
        binding.chart.isHighlightFullBarEnabled = false
        val l = binding.chart.legend
        l.setDrawInside(false)
        l.isEnabled = false

        val rightAxis = binding.chart.axisRight
        rightAxis.isEnabled = false
        rightAxis.axisMinimum = 0f // this replaces setStartAtZero(true)
        val leftAxis = binding.chart.axisLeft
        leftAxis.isEnabled = true
        leftAxis.setDrawGridLines(false)
        leftAxis.axisMinimum = 0f
        leftAxis.granularity = 1f
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        // this replaces setStartAtZero(true)
        val xAxis = binding.chart.xAxis
        xAxis.position = XAxisPosition.BOTTOM
        xAxis.axisMinimum = 0f
        xAxis.granularity = 1f

        // Sample date string
        binding.spinnerView.setOnClickListener {
            showMonthYearPickerDialog(selecetdMonthYear)
        }
        binding.actionDownIcon.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    readImagePermission
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // You can use the API that requires the permission.
                    download()
                }
                ActivityCompat.shouldShowRequestPermissionRationale(
                    this.requireActivity(), readImagePermission) -> {
                    // The user has previously denied the permission, explain why you need it.
                    //  showPermissionRationaleDialog()

                }
                else -> {
                    // You can directly ask for the permission.
                    // The registered ActivityResultCallback gets the result of this request.
                    requestPermissionLauncher.launch(
                        readImagePermission)
                }
            }
        }
        return root
    }



    private fun showToastShort(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun redirectToAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", requireContext().packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Required")
            .setMessage("Please grant permission to access images.")
            .setPositiveButton("OK") { _, _ ->
                // Redirect the user to the app settings page
                redirectToAppSettings()
            }
            .setNegativeButton("Cancel") { _, _ ->
                // Handle cancellation if needed
            }
            .show()
    }
    private fun download() {
        Log.d("Download complete", "----------")
        var file_path =""
        var uri: Uri
        class DownloadFile : AsyncTask<String?, Void?, Void?>(){

            var uploading: ProgressDialog? = null

            override fun onPreExecute() {
                super.onPreExecute()
                uploading =
                    ProgressDialog.show(
                        requireContext(),
                        "Please Wait",
                        "PDF Downloading...",
                        false,
                        false
                    )
            }
            override fun doInBackground(vararg strings: String?): Void? {
                Log.d("TAG", "doInBackground: ")
                val fileUrl: String = strings[0]!! // -> http://maven.apache.org/maven-1.x/maven.pdf
                strings[1]!! // -> maven.pdf

                val localStorage = requireActivity().getExternalFilesDir(null)
                val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US)
                val now = Date()
                val tempFilename :String = "Statistics"+"_"+selecetdMonthYear+"_"
                val storagePath = localStorage!!.absolutePath
                val rootPath = "$storagePath/$tempFilename"
                val file = File(rootPath + formatter.format(now).toString() + ".pdf")
                try {
//                    val permissionCheck = ContextCompat.checkSelfPermission(
//                        requireContext(),
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE
//                    )
                    // if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                    if (!file.createNewFile()) {
                        Log.i("Test", "This file is already exist: " + file.absolutePath)
                    }
                    file_path = file.absolutePath


                    uri = if (Build.VERSION.SDK_INT < 24) {
                        Uri.fromFile(file)
                    } else {
                        Uri.parse(file.path) // My work-around for new SDKs, worked for me in Android 10 using Solid Explorer Text Editor as the external editor.
                    }

                    //}
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                FileDownloader.downloadFile(fileUrl, file)
                return null
            }
            override fun onPostExecute(aVoid: Void?) {
                super.onPostExecute(aVoid)
                Log.d("onPostExecute", "onPostExecute: ")
                Log.d("onPostExecute", file_path)
                uploading?.dismiss()
                if(file_path!=""){
                    Log.d("onPostExecute", "file path: : ")

                    Log.d("TAG", "file path: ")
                    val d = Dialog(requireContext())
                    d.setTitle("File Download")
                    d.setContentView(R.layout.downloaded_dialog_layout)
                    val btnView: Button = d.findViewById<View>(R.id.btnView) as Button
                    val btnShare: Button = d.findViewById<View>(R.id.btnShare) as Button
                    val tvClose: TextView = d.findViewById<View>(R.id.tvClose) as TextView


                    btnView.setOnClickListener {
                        Log.d("TAG", "onPostExecute: $file_path")
                        val file = File(
                            file_path
                        )
                        Log.d("TAG", "onPostExecute: $file")

                        val photoURI = FileProvider.getUriForFile(requireContext(),
                            requireActivity().applicationContext.packageName
                                .toString() + ".provider",
                            file
                        )


                        val target = Intent(Intent.ACTION_VIEW)
                        target.setDataAndType(
                            photoURI,
                            "application/pdf"
                        )
                        target.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                        target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)


                        val intent = Intent.createChooser(target, "Open File")
                        try {
                            startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            // Instruct the user to install a PDF reader here, or something
                        }
                        d.dismiss()
                    }
                    btnShare.setOnClickListener {
                        val intentShareFile = Intent(Intent.ACTION_SEND)
                        val fileWithinMyDir = File(file_path)
                        val photoURI = FileProvider.getUriForFile(requireContext(),
                            requireActivity().applicationContext.packageName
                                .toString() + ".provider",
                            fileWithinMyDir
                        )

                        intentShareFile.type = "application/pdf"
                        intentShareFile.putExtra(
                            Intent.EXTRA_STREAM,
                            photoURI
                        )
                        intentShareFile.putExtra(
                            Intent.EXTRA_SUBJECT,
                            "Sharing File..."
                        )
                        intentShareFile.putExtra(Intent.EXTRA_TEXT, "Sharing File...")
                        intentShareFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                        val chooser = Intent.createChooser(intentShareFile, "Share File")

                        val resInfoList: List<ResolveInfo> = requireActivity().packageManager.queryIntentActivities(
                            chooser,
                            PackageManager.MATCH_DEFAULT_ONLY
                        )

                        for (resolveInfo in resInfoList) {
                            val packageName = resolveInfo.activityInfo.packageName
                            requireActivity().grantUriPermission(
                                packageName,
                                photoURI,
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                        }


                        startActivity(chooser)
                    }

                    tvClose.setOnClickListener {
                        d.dismiss()
                    }

                    d.show()

                    val window: Window? = d.window
                    window?.setLayout(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )


                }
            }
        }
        Log.d("TAG", "download: homeType__homeType")
        val premise_id = premiseID
        val date = selecetdMonthYear
        var type = ""

        val downLoadURL = ApiInitialize.LOCAL_URL+"user/pdfgraph?user_id=${encrypt(userID)}&premise_id=${encrypt(premise_id)}&date=${encrypt(date)}&type=${encrypt("statisticsdetails")}"
        Log.d("TAG", "download: $downLoadURL")
        DownloadFile().execute(
            downLoadURL,
            "maven.pdf"
        )
        Log.d("Download complete", "----------")
    }

    private fun showMonthYearPickerDialog(initialDate: String) {
        val calendar = Calendar.getInstance()

        val monthYearPickerDialog = MonthYearPickerDialog(requireContext(),
            object : MonthYearPickerDialog.OnDateSetListener {
                override fun onDateSet(month: String, year: Int) {
                    // Handle the selected date
                    selectedMonthAndYear("$year-${convertDate("$month","MMMM","MM")}-01")
                }
            },calendar,
            initialDate
        )
        monthYearPickerDialog.show()
    }
    fun selectedMonthAndYear(date: String){
        selecetdMonthYear=date
        binding.spinnerView.text = convertDate("$date","yyyy-MM-dd","MMM")
    }

    private fun setGraphData(staffGraphDetails : StaffGraphDetails.Data) {
        val xAxisLabelList = ArrayList<String>()

        if(values1.size>0){
            values1.clear()
        }

        if(values2.size>0){
            values2.clear()
        }

        if(values3.size>0){
            values3.clear()
        }
        for (i in staffGraphDetails.graphDetails){
            if(i.date!!.isNotEmpty() && i.date !=null){
                Log.e("totalInPercentage", i.totalInPercentage!!.toFloat().toString()+" == "+i.totalInPercentage!!.toFloat()/60)
                values1.add(BarEntry(7f, i.totalInPercentage!!.toFloat()/60))
                values2.add(BarEntry(7f, i.totalRequestOffsitePercentage!!.toFloat()/60))
                values3.add(BarEntry(7f, i.totalOfflinePercentage!!.toFloat()/60))
                xAxisLabelList.add(
                    convertDate(i.date!!.toString(), "yyyy-MM-dd", "dd/MM"))
            }
        }

        binding.chart.xAxis.valueFormatter = IndexAxisValueFormatter(xAxisLabelList)
        binding.chart.xAxis.labelCount = 21
        binding.chart.setFitBars(true)
        binding.chart.setVisibleXRangeMaximum(7f)

        val groupSpace = 0.10f
        val barSpace = 0.05f // x4 DataSet
        val barWidth = 0.25f // x4 DataSet

        val set1: BarDataSet
        val set2: BarDataSet
        val set3: BarDataSet


        if (binding.chart.data != null && binding.chart.data.dataSetCount > 0) {
            set1 = binding.chart.data.getDataSetByIndex(0) as BarDataSet
            set2 = binding.chart.data.getDataSetByIndex(1) as BarDataSet
            set3 = binding.chart.data.getDataSetByIndex(2) as BarDataSet
            set1.values = values1
            set2.values = values2
            set3.values = values3
            binding.chart.invalidate()
            binding.chart.data.notifyDataChanged()
            binding.chart.notifyDataSetChanged()
        } else {
            // create 4 DataSets
            set1 = BarDataSet(values1, "Company A")
            set1.color = Color.rgb(77, 228, 161)
            set2 = BarDataSet(values2, "Company B")
            set2.color = Color.rgb(252, 225, 129)
            set3 = BarDataSet(values3, "Company C")
            set3.color = Color.rgb(254, 165, 165)
            val data = BarData(set1, set2, set3)
            data.setValueFormatter(LargeValueFormatter())
            binding.chart.data = data
            binding.chart.invalidate()
            binding.chart.data.notifyDataChanged()
        }

        set1.setDrawValues(false)
        set2.setDrawValues(false)
        set3.setDrawValues(false)

        // specify the width each bar should have
        binding.chart.barData.barWidth = barWidth
        binding.chart.xAxis.setCenterAxisLabels(true)
        binding.chart.xAxis.granularity = 1f
        binding.chart.axisLeft.setCenterAxisLabels(true)
        binding.chart.setOnChartValueSelectedListener(this)
        binding.chart.axisLeft.isDrawTopYLabelEntryEnabled

        binding.chart.axisLeft.valueFormatter = IntValueFormatter()

        binding.chart.axisLeft.isGranularityEnabled = true
        binding.chart.axisLeft.granularity = 1f
        binding.chart.axisLeft.labelCount = 24
        binding.chart.axisLeft.mAxisMinimum = 0f

        // binding.chart.axisLeft.valueFormatter = IndexAxisValueFormatter(yAxisLabelList)

        // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it
        // For bounds control
        customMarkerView = CustomBarMarkerView(requireContext(), R.layout.tooltip, staffGraphDetails.graphDetails)
        binding.chart.groupBars(0f, groupSpace, barSpace)
        binding.chart.invalidate()
        binding.chart.fitScreen()
    }


override fun onValueSelected(e: Entry?, h: Highlight?) {
    Log.i("Activity", "Selected: $e, dataSet: ${h?.dataSetIndex}")

    binding.chartTimeDetailsIndicatorOnline.visibility = View.VISIBLE
    binding.indicatorOffsite.visibility = View.VISIBLE
    binding.indicatorOffline.visibility = View.VISIBLE
    customMarkerView.refreshContent(e!!, h!!)
    binding.chart.marker = customMarkerView

    val stringTemp = e?.x.toString()
    val stringForm = stringTemp?.substring(0, stringTemp.indexOf('.')) ?: ""
    val t = stringForm.toIntOrNull()

    t?.let {
        val details = staffGraphDetailsData.data?.graphDetails?.getOrNull(it)

        when (h?.dataSetIndex) {
            0 -> handleDataSet0(e, details)
            1 -> handleDataSet1(e, details)
            2 -> handleDataSet2(e, details)
        }
    }
}

    private fun handleDataSet0(e: Entry, details: StaffGraphDetails.Data.GraphDetails?) {
        if (values1[e.x.toInt()].y == e.y) {
            updateUI("Online", details)
        }
    }

    private fun handleDataSet1(e: Entry, details: StaffGraphDetails.Data.GraphDetails?) {
        if (values2[e.x.toInt()].y == e.y) {
            updateUI("Offsite", details)
        }
    }

    private fun handleDataSet2(e: Entry, details: StaffGraphDetails.Data.GraphDetails?) {
        if (values3[e.x.toInt()].y == e.y) {
            updateUI("Offline", details)
        }
    }

    private fun updateUI(status: String, details: StaffGraphDetails.Data.GraphDetails?) {
        Log.e("statuzzz",details!!.offsiteTimings.toString())
        binding.tvGraphStatusOnline.text="online"
        binding.tvGraphStatusOffsite.text="Offsite"
        binding.tvGraphStatusOffline.text="Offline"
        if(details!!.onlineTimings !=null&&details!!.onlineTimings !="") {binding.chartTimeDetailsOnline.text = details?.onlineTimings}else{binding.chartTimeDetailsOnline.text = "No timings available"}
        if(details!!.offsiteTimings !=null&&details!!.offsiteTimings !=""){binding.chartTimeDetailsOffsite.text = details?.offsiteTimings}else{binding.chartTimeDetailsOffsite.text = "No timings available"}
        if(details!!.offlineTimings !=null&&details!!.offlineTimings !=""){binding.chartTimeDetailsOffline.text = details?.offlineTimings}else{binding.chartTimeDetailsOffline.text = "No timings available"}
    }


    override fun onNothingSelected() {
        binding.chartTimeDetailsIndicatorOnline.visibility=View.GONE
        binding.indicatorOffline.visibility=View.GONE
        binding.indicatorOffsite.visibility=View.GONE
    }




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
            requireContext(),
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
                date=formattedDate
                selectedMonthAndYear(date)
                // Update the TextView to display the selected date with the format
                binding.offsiteHeading.text= convertDate(formattedDate,"yyyy-MM-dd","EEE - dd MMM',' yyyy")
                /*** Function to staffListGet when click datePicker select a date to call api request */
                chart.marker=null
                staffGraphDataGet(date)
            },
            initialCalendar.get(Calendar.YEAR),
            initialCalendar.get(Calendar.MONTH),
            initialCalendar.get(Calendar.DAY_OF_MONTH)
        )
        // Set the maximum date to the current date within the current month
        datePickerDialog.datePicker.maxDate = calendar.timeInMillis
        datePickerDialog.show()
    }


    /** Send data To Api requestOffsite */
    @RequiresApi(Build.VERSION_CODES.R)
    private fun staffGraphDataGet(date:String) {
        Log.e("original value","Bearer $date");
        if (isOnline(requireContext())) {
            ApiRequest(
                requireActivity(),
                ApiInitialize.initialize(ApiInitialize.LOCAL_URL).staffGraphOn7days(
                    "Bearer ${getUserModel()!!.data.token}",
                    encrypt(premiseID),
                    encrypt(date),
                ),
                HOME_DATA_GET, true, this
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
    fun getUserModel(): UserModel? {
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

    /*** Function to setup showSnackBar */
    protected fun showSnackBar(view: View, message: String, action: ActivityBase.ACTIONSNACKBAR) {
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

    override fun getApiResponse(apiResponseManager: ApiResponseManager<*>) {
        when (apiResponseManager.type) {
            HOME_DATA_GET -> {
                if(binding!=null){
                    Log.e("reponsseeee", apiResponseManager.response.toString())
                    val graphData = apiResponseManager.response as StaffGraphDetails
                    staffGraphDetailsData = graphData
                    if (graphData.statusCode== SUCCESS_CODE){
                        /*** graph datas are set */
                        setHomeOfferData(graphData.data!!)
                        setGraphData(graphData.data!!)
                        hideStatusView()
                        if(graphData.data!!.pdfDownload=="no"){
                            binding.pdfShowView.visibility=View.GONE
                        }
//                        showSnackBar(binding.root,graphData.message.toString(),ActivityBase.ACTIONSNACKBAR.DISMISS)
                    }else{
//                        showSnackBar(binding.root,graphData.message.toString(),ActivityBase.ACTIONSNACKBAR.DISMISS)
                    }
                }

            }
        }
    }

    private fun hideStatusView() {
        binding.chartTimeDetailsIndicatorOnline.visibility = View.GONE
        binding.indicatorOffsite.visibility = View.GONE
        binding.indicatorOffline.visibility = View.GONE
    }

    private fun setHomeOfferData(dataModel: StaffGraphDetails.Data) {
        profileViewModel.setProfileData(dataModel!!.userDetails!!.userImage.toString(),
            dataModel.userDetails!!.userFirstName.toString(),
            dataModel.userDetails!!.userLastName.toString(),
            dataModel.userDetails!!.userStatus.toString(),
            dataModel.premiseDetails!!.premiseName +", "+dataModel.premiseDetails!!.city+", "+dataModel.premiseDetails!!.state,
        )

    }



}
class CustomBarMarkerView(context: Context?, layoutResource: Int, private val dataModel: ArrayList<StaffGraphDetails.Data.GraphDetails>) : MarkerView(
    context,
    layoutResource
) {
    private val tvStatusGraph: TextView = findViewById<View>(R.id.tvStatusGraph) as TextView
    private val tvContent: TextView = findViewById<View>(R.id.data1) as TextView
    private val tvContent1: TextView = findViewById<View>(R.id.data2) as TextView
    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    override fun refreshContent(e: Entry, highlight: Highlight) {
        super.refreshContent(e, highlight)
        if (e is CandleEntry) {
            Log.e("e.x",e.x.toString())
            tvContent1.text = chart.xAxis.valueFormatter
                .getFormattedValue(
                    e.x
                )
            tvContent.text = com.github.mikephil.charting.utils.Utils.formatNumber(e.y, 0, true)
        } else {

            val string_temp = e.x.toString()
            val string_form = string_temp.substring(0, string_temp.indexOf('.'))
            val t = Integer.valueOf(string_form)
            Log.e("e.x1",t.toString())
            Log.e("e.x1",values1.size.toString())
            tvContent1.text =
                convertDate(dataModel[t].date,
                    "yyyy-MM-dd",
                    "dd/MM")

            if(highlight.dataSetIndex===0){
                if(values1[t].y===e.y){
                    tvContent.text = convertMinutesToHours(dataModel[t].totalInPercentage)
                    tvStatusGraph.text = "Online"
                }
            }else if(highlight.dataSetIndex===1){
                if(values2[t].y===e.y){
                    tvContent.text = convertMinutesToHours(dataModel[t].totalRequestOffsitePercentage)
                    tvStatusGraph.text = "Offsite"
                }

            }else if(highlight.dataSetIndex===2) {
                if(values3[t].y===e.y){
                    tvContent.text =convertMinutesToHours( dataModel[t].totalOfflinePercentage)
                    tvStatusGraph.text = "Offline"
                }
            }
        }

    }

    private fun convertMinutesToHours(time: Int?): String? {
        var tempTime: String? = null
        return if(time!!%60==0){
            tempTime =(time/60).toString()+ " hrs"
            tempTime
        }else if(time<60){
            tempTime =(time).toString()+ " mins"
            tempTime
        }else{
            tempTime =(time/60).toString()+ " hrs "+(time%60).toString()+ " mins"
            tempTime
        }
    }


    override fun getOffset(): MPPointF {
        return MPPointF((-(width / 2)).toFloat(), (-height).toFloat()) // place the midpoint of marker over the bar
    }

}


internal class IntValueFormatter : ValueFormatter() {
    private var previousValue = Int.MIN_VALUE // Initialize to a very small value
    override fun getAxisLabel(value: Float, axis: AxisBase): String {
        val intValue = value.toInt()

        // Check if the value is different from the previous one
        return if (intValue >= 0 && intValue != previousValue) {
            previousValue = intValue
            intValue.toString()
        } else {
            "" // Display an empty string if the value is repeated
        }
    }

}