package com.busydoor.app.activity

import MonthYearPickerDialog
import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
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
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.busydoor.app.R
import com.busydoor.app.apiService.ApiInitialize
import com.busydoor.app.apiService.ApiRequest
import com.busydoor.app.apiService.ApiResponseInterface
import com.busydoor.app.apiService.ApiResponseManager
import com.busydoor.app.customMethods.DatePickerUtil
import com.busydoor.app.customMethods.FileDownloader
import com.busydoor.app.customMethods.HOME_DATA_GET
import com.busydoor.app.customMethods.NameConvertion
import com.busydoor.app.customMethods.SUCCESS_CODE
import com.busydoor.app.customMethods.activity
import com.busydoor.app.customMethods.convertDate
import com.busydoor.app.customMethods.globalDate
import com.busydoor.app.customMethods.isOnline
import com.busydoor.app.databinding.ActivityStaffDetailGraphBinding
import com.busydoor.app.interfaceD.HomeClick
import com.busydoor.app.model.StaffGraphDetails
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
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
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

lateinit var chart: BarChart
val values1 = ArrayList<BarEntry>()
val values2 = ArrayList<BarEntry>()
val values3 = ArrayList<BarEntry>()
open class StaffDetailGraphActivity : ActivityBase(), OnChartValueSelectedListener,
    ApiResponseInterface,HomeClick {
    /*** Create a binding for current Activity*/
    val binding by lazy { ActivityStaffDetailGraphBinding.inflate(layoutInflater) }
    private var REQUEST_WRITE_PERMISSION = 123
    /*Encryption variables*/
    private var userID: String =""
    private var userName: String =""
    private var premiseID: String=""
    private var userSelectedDate: String=""
    private var selecetdMonthYear: String=""
    private lateinit var customMarkerView : CustomBarMarkerView
    private lateinit var staffGraphDetailsData:StaffGraphDetails
    private lateinit var selectedDate: Calendar

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
                showToastShort(this, "Permission Denied")

            }
        }
    @SuppressLint("SuspiciousIndentation")
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        // Inflate the layout for this fragment
        chart =binding.chart
        /*** initialize encrypt fun here  */
        userID = getUserModel()?.data?.userId.toString()
        premiseID = activity?.intent?.getStringExtra("premiseId").toString()
        userID =intent.getStringExtra("userId").toString()
        premiseID = intent.getStringExtra("premiseId").toString()
        userName = intent.getStringExtra("userName").toString()
        userSelectedDate = intent.getStringExtra("selectedDate").toString()
        binding.offsiteHeading.text= convertDate(userSelectedDate,"yyyy-MM-dd","EEE - dd MMM',' yyyy")
        /*** initialize shared-preference here  */
        staffGraphDataGet(userSelectedDate)

        selectedMonthAndYear(userSelectedDate)
        selectedDate = Calendar.getInstance()

        binding.calendarIcon.setOnClickListener{
            // Show the DatePicker dialog
            DatePickerUtil.showDatePicker(
                this,
                userSelectedDate!!
            ) { formattedDate ->
                // Update your UI or perform other actions with the selected date
                userSelectedDate=formattedDate
                selectedMonthAndYear(userSelectedDate)
                // Update the TextView to display the selected date with the format
                binding.offsiteHeading.text= convertDate(formattedDate,"yyyy-MM-dd","EEE - dd 'th' MMM',' yyyy")
                /*** Function to staffListGet when click datePicker select a date to call api request */
                staffGraphDataGet(userSelectedDate)
                chart.marker=null
            }
        }
        binding.staffDetailToolbar.backPage.setOnClickListener {
            finish()
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
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.axisMinimum = 0f
        xAxis.granularity = 1f

        // Sample date string
        binding.tvMonthSelector.setOnClickListener {
            showMonthYearPickerDialog(selecetdMonthYear)
        }
        binding.actionDownIcon.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    readImagePermission
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // You can use the API that requires the permission.
                    download()
                }
                ActivityCompat.shouldShowRequestPermissionRationale(
                    this@StaffDetailGraphActivity, readImagePermission) -> {
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
    }



    private fun showToastShort(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun redirectToAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(this)
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
                        this@StaffDetailGraphActivity,
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

                val localStorage = this@StaffDetailGraphActivity.getExternalFilesDir(null)
                val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US)
                val now = Date()
                val tempFilename :String = userName+"_"+selecetdMonthYear+"_"
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
                    val d = Dialog(this@StaffDetailGraphActivity)
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

                        val photoURI = FileProvider.getUriForFile(this@StaffDetailGraphActivity,
                            this@StaffDetailGraphActivity.applicationContext.packageName
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
                        val photoURI = FileProvider.getUriForFile(this@StaffDetailGraphActivity,
                            this@StaffDetailGraphActivity.applicationContext.packageName
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

                        val resInfoList: List<ResolveInfo> = this@StaffDetailGraphActivity.packageManager.queryIntentActivities(
                            chooser,
                            PackageManager.MATCH_DEFAULT_ONLY
                        )

                        for (resolveInfo in resInfoList) {
                            val packageName = resolveInfo.activityInfo.packageName
                            this@StaffDetailGraphActivity.grantUriPermission(
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

        val monthYearPickerDialog = MonthYearPickerDialog(this,
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
                values2.add(BarEntry(7f, i.totalOfflinePercentage!!.toFloat()/60))
                values3.add(BarEntry(7f, i.totalRequestOffsitePercentage!!.toFloat()/60))
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
        customMarkerView = CustomBarMarkerView(this, R.layout.tooltip, staffGraphDetails.graphDetails)
        binding.chart.groupBars(0f, groupSpace, barSpace)
        binding.chart.invalidate()
        binding.chart.fitScreen()
        // Select the last entry by default
        val lastIndex = values1.size - 1
        // highlight the first dataset
        val dataSetIndex = 0
        binding.chart.highlightValue(lastIndex.toFloat(), dataSetIndex, false)
        // Manually trigger onValueSelected for the last entry
       handleOnValueSelected(lastIndex.toFloat(), dataSetIndex)
    }
    private fun handleOnValueSelected(xIndex: Float, dataSetIndex: Int) {
        val entry = chart.data.getDataSetByIndex(dataSetIndex).getEntryForIndex(xIndex.toInt())
        val highlight = Highlight(xIndex, entry.y, dataSetIndex)
        onValueSelected(entry, highlight)
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        Log.i("Activityzzz", "Selected: $e, dataSet: ${h?.dataSetIndex}")
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
        Log.e("statuzzz",details!!.toString())
        binding.tvGraphStatusOnline.text="Online"
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




    /** Send data To Api requestOffsite */
    @RequiresApi(Build.VERSION_CODES.R)
    private fun staffGraphDataGet(date:String) {
        Log.e("original value","Bearer $date");
        if (isOnline(this)) {
            ApiRequest(
                this@StaffDetailGraphActivity,
                ApiInitialize.initialize(ApiInitialize.LOCAL_URL).staffDetailGraphOn7days(
                    "Bearer ${getUserModel()!!.data.token}",
                    encrypt(premiseID),
                    encrypt(userSelectedDate),
                    encrypt(userID),
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

    /*** Function to setup showSnackBar */


    override fun getApiResponse(apiResponseManager: ApiResponseManager<*>) {
        when (apiResponseManager.type) {
            HOME_DATA_GET -> {
                if(binding!=null){
                    Log.e("reponsseeee", apiResponseManager.response.toString())
                    val graphData = apiResponseManager.response as StaffGraphDetails
                    staffGraphDetailsData = graphData
                    if (graphData.statusCode== SUCCESS_CODE){
                        setHomeOfferData(graphData.data!!)
                        /*** graph datas are set */
                        setGraphData(graphData.data!!)
                    }
                    else{
                        hideStatusView()
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
        binding.staffDetailToolbar.userName.text = NameConvertion().truncateText(dataModel.userDetails!!.userFirstName+" "+dataModel.userDetails!!.userLastName)
        Glide.with(this)
            .load(dataModel!!.userDetails!!.userImage.toString())
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .into(binding.staffDetailToolbar.PremiseStaffImage)


        when (dataModel!!.userDetails!!.userStatus) {
            "in" -> {
                binding.staffDetailToolbar.staffStatus.setImageResource(R.drawable.icon_staff_profile_in)
            }
            "inout" -> {
                binding.staffDetailToolbar.staffStatus.setBackgroundResource(R.drawable.icon_profile_status_inout)
            }
            "out" -> {
                binding.staffDetailToolbar.staffStatus.setBackgroundResource(R.drawable.icon_profile_status_out)
            }
            "offline" -> {
                binding.staffDetailToolbar.staffStatus.setBackgroundResource(R.drawable.icon_profile_status_offline)
            }
            else -> {
                binding.staffDetailToolbar.staffStatus.setBackgroundResource(R.drawable.icon_profile_status_offline)
            }
        }


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
