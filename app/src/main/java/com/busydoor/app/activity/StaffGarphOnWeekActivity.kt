package com.busydoor.app.activity

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
import android.graphics.DashPathEffect
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
import androidx.recyclerview.widget.GridLayoutManager
import com.busydoor.app.R
import com.busydoor.app.adapter.StaffListAdapter
import com.busydoor.app.apiService.ApiInitialize
import com.busydoor.app.apiService.ApiRequest
import com.busydoor.app.apiService.ApiResponseInterface
import com.busydoor.app.apiService.ApiResponseManager
import com.busydoor.app.customMethods.FileDownloader
import com.busydoor.app.customMethods.PrefUtils
import com.busydoor.app.customMethods.STAFF_GRAPH_DATA
import com.busydoor.app.customMethods.STAFF_LIST_DATA
import com.busydoor.app.customMethods.SUCCESS_CODE
import com.busydoor.app.customMethods.convertDate
import com.busydoor.app.customMethods.globalDate
import com.busydoor.app.customMethods.isOnline
import com.busydoor.app.customMethods.toolTipDate
import com.busydoor.app.customMethods.toolTipTime
import com.busydoor.app.databinding.ActivityStaffDetailsGraphInweekBinding
import com.busydoor.app.interfaceD.HomeClick
import com.busydoor.app.model.StaffGraphcount
import com.busydoor.app.model.StaffListOnDate
import com.busydoor.app.viewmodel.SharedViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.MPPointF
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import kotlin.collections.ArrayList

class StaffDetailsOnWeekActivity : ActivityBase(),ApiResponseInterface,HomeClick {
    private var values = java.util.ArrayList<Entry>()
    private var userSelectedDate : String = ""
    private var calSelectedDate : String = ""
    private var endDate : String = ""
    private var PremiseId : String = ""
    private var staffGraphOnWeek: StaffGraphcount? = null
    private var staffListOnDay: StaffListOnDate? = null
    private var temp_staffListOnDay: StaffListOnDate? = null
    private var isSwitchChecked : Boolean = true
    private var selecetdMonthYear: String=""
    private lateinit var sharedViewModel: SharedViewModel
    /*** Create a binding for current Activity*/
    val binding by lazy { ActivityStaffDetailsGraphInweekBinding.inflate(layoutInflater) }

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
//                showToastShort(this, "isGranted false")

            }
        }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        objSharedPref = PrefUtils(this);
        PremiseId = intent.getStringExtra("premiseId").toString()
        Log.e("premise_id",PremiseId.toString())
        calSelectedDate= intent.getStringExtra("selectDate").toString()
        setUserSelectedDate(calSelectedDate)
        selectedMonthAndYear(globalDate)


        binding.calendarIcon.setOnClickListener {
            showDatePicker(calSelectedDate)
        }
        // Sample date string
        binding.spinnerView.setOnClickListener {
            showMonthYearPickerDialog(selecetdMonthYear)
        }
        binding.actionDownIcon.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(
                    this@StaffDetailsOnWeekActivity,
                    readImagePermission
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // You can use the API that requires the permission.
                    download()
                }
                ActivityCompat.shouldShowRequestPermissionRationale(
                    this, readImagePermission) -> {
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


        binding.staffToolbar.backPage.setOnClickListener {
            finish();
        }
        /*** Function to hide/show  Staff View*/
        binding.scSelected.setOnCheckedChangeListener { _, isChecked ->
            isSwitchChecked = isChecked
            if (isChecked) {
                binding.staffListView.visibility = View.VISIBLE
            } else {
                binding.staffListView.visibility = View.GONE
            }
        }
        /*** Set a date from select on date picker*/
        binding.offsiteHeading.text = convertDate(calSelectedDate,"yyyy-MM-dd","EEE - dd MMM',' yyyy")
        binding.tvStaffSelectedLabel.text = convertDate(calSelectedDate,"yyyy-MM-dd","EEE - dd MMM',' yyyy")
        /*** Initial APi call to staff 7days Data*/
        staffGraphGet(calSelectedDate)
        /*** Initially to call staffList Api to get a data in Current date*/
        staffListGet(calSelectedDate)
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
                        this@StaffDetailsOnWeekActivity,
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

                val localStorage = getExternalFilesDir(null)
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
                    val d = Dialog(this@StaffDetailsOnWeekActivity)
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

                        val photoURI = FileProvider.getUriForFile(this@StaffDetailsOnWeekActivity,
                            this@StaffDetailsOnWeekActivity.applicationContext.packageName
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
                        val photoURI = FileProvider.getUriForFile(this@StaffDetailsOnWeekActivity,
                            applicationContext.packageName
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

                        val resInfoList: List<ResolveInfo> = packageManager.queryIntentActivities(
                            chooser,
                            PackageManager.MATCH_DEFAULT_ONLY
                        )

                        for (resolveInfo in resInfoList) {
                            val packageName = resolveInfo.activityInfo.packageName
                            grantUriPermission(
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
        val downLoadURL = ApiInitialize.LOCAL_URL+"user/pdfgraph?user_id=${encrypt(getUserModel()!!.data.userId.toString())}&premise_id=${encrypt(PremiseId)}&date=${encrypt(selecetdMonthYear)}&type=${encrypt("staffdetails")}"
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
    override fun homePostionClick(postion: Int) {

        Log.e("homePostionClick",staffListOnDay!!.data[postion].userId.toString())

        val intent= Intent(this@StaffDetailsOnWeekActivity,StaffDetailGraphActivity::class.java)
        .putExtra("userId",staffListOnDay!!.data[postion].userId.toString())
        .putExtra("premiseId",PremiseId)
        .putExtra("userName",staffListOnDay!!.data[postion].firstName+staffListOnDay!!.data[postion].lastName)
        .putExtra("selectedDate",userSelectedDate)

        startActivity(
            intent)
    }

    /*** Function to show date picker*/
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
                calSelectedDate=formattedDate
                setUserSelectedDate(calSelectedDate)
                selectedMonthAndYear(calSelectedDate)
                // Update the TextView to display the selected date with the format
                binding.offsiteHeading.text= convertDate(formattedDate,"yyyy-MM-dd","EEE - dd MMM',' yyyy")
                binding.tvStaffSelectedLabel.text = convertDate(formattedDate,"yyyy-MM-dd","EEE - dd MMM',' yyyy")
                /*** Function to staffListGet when click datePicker select a date to call api request */
                staffGraphGet(formattedDate)
                staffListGet(formattedDate)
            },
            initialCalendar.get(Calendar.YEAR),
            initialCalendar.get(Calendar.MONTH),
            initialCalendar.get(Calendar.DAY_OF_MONTH)
        )
        // Set the maximum date to the current date within the current month
        datePickerDialog.datePicker.maxDate = calendar.timeInMillis
        datePickerDialog.show()
    }

    private fun setUserSelectedDate(calSelectedDate: String) {
        userSelectedDate = calSelectedDate
    }

    /*** Function to staffListGet when click graph count to call api request */
    @RequiresApi(Build.VERSION_CODES.R)
    private fun staffListGet(date:String) {
        Log.e("original value","Bearer ${encrypt(date)}");
        if (isOnline(this)) {
            ApiRequest(
                this,
                ApiInitialize.initialize(ApiInitialize.LOCAL_URL).getStaffDetailOnDate(
                    "Bearer ${getUserModel()!!.data.token}",
                    encrypt(PremiseId),
                    encrypt(date),
                ),
                STAFF_LIST_DATA, true, this
            )
            Log.e("Request","check if inside log");
        } else {
            Log.e("homeDataGet","no")
            showSnackBar(
                binding.root,
                getString(R.string.noInternet),
                ACTIONSNACKBAR.DISMISS
            )
        }
    }

    /*** Function to 7Days staffGraph api request */
    @RequiresApi(Build.VERSION_CODES.R)
    private fun staffGraphGet(date:String) {
        Log.e("original value","Bearer ${encrypt(date)}");
        if (isOnline(this)) {
            ApiRequest(
                this,
                ApiInitialize.initialize(ApiInitialize.LOCAL_URL).getStaffGraphpresentCount(
                    "Bearer ${getUserModel()!!.data.token}",
                    encrypt(PremiseId),
                    encrypt(date),
                ),
                STAFF_GRAPH_DATA, true, this
            )
            Log.e("Request","check if inside log");
        } else {
            Log.e("homeDataGet","no")
            showSnackBar(
                binding.root,
                getString(R.string.noInternet),
                ACTIONSNACKBAR.DISMISS
            )
        }
    }

    /*** Function to Set a Graph for staff's 7Days Data here*/
    private fun tempGraphDataSet(data: StaffGraphcount.Data) {
        Log.e("tempGraphDataSet","tempGraphDataSet")
        values = ArrayList<Entry>()
        values.clear()
        val xAxisLabelList = ArrayList<String>()

        /*** for loop for 7 days api data*/
        for (i in data.totalPresentCountBydate.indices) {
                    if (data.totalPresentCountBydate[i].totalPresentCount != 0) {
                        /*** To add X,Y axis data eg:X=0,1,2; Y=totalPresentCounts */
                        values.add(
                            Entry(
                                i.toFloat(),
                                data.totalPresentCountBydate[i].totalPresentCount!!.toFloat()
                            )
                        )

                    }
                    else {
                        /*** To add X,Y axis data Manually set 0F is mean empty */
                        values.add(
                            Entry(
                                0F,
                                0f
                            )
                        )
                    }

                    /*** To add label for X axis based on Api Date for all Data inside the loop and Its Convert */
                    xAxisLabelList.add(
                        convertDate(data.totalPresentCountBydate[i].date, "yyyy-MM-dd", "dd/MM")
                    )

                }
        /*** To Customize all property here like graph color radius etc.. */
        setLineChartDataWithoutDate(
                    10f,
                    xAxisLabelList,
                    binding.staffGraphOneWeek,
                    data,
                )

            }

    /**Set Graph for 7days below... **/
    private fun setLineChartDataWithoutDate(
        lineValue: Float,
        weekdays: ArrayList<String>,
        row_line_chart: LineChart,
        StaffGraphData: StaffGraphcount.Data,
    ) {
        row_line_chart.clear()
        val zeroNumberList = java.util.ArrayList<Entry>()
        val otherNumberList = java.util.ArrayList<Entry>()
        zeroNumberList.clear()
        otherNumberList.clear()

        for (i in values.indices) {
            Log.e("zeroNumberList", this.values[i].y.toString())
            zeroNumberList.add(Entry(i.toFloat(), this.values[i].y))
        }

        for (i in this.values.indices) {
            Log.e("otherNumberList", this.values[i].y.toString())
            otherNumberList.add(Entry(i.toFloat(), this.values[i].y))
        }


        val set1 = LineDataSet(zeroNumberList, "")
        val set2 = LineDataSet(otherNumberList, "")
        set1.setDrawIcons(false)
        set2.setDrawIcons(false)

        /*** To set bg_color for chart background */
        row_line_chart.setBackgroundColor(
            ContextCompat.getColor(
                this@StaffDetailsOnWeekActivity,
                R.color.white
            )
        )


        row_line_chart.description.isEnabled = false
        row_line_chart.setTouchEnabled(true)

        val xAxis = row_line_chart.xAxis
        xAxis.enableGridDashedLine(lineValue, 0f, 0f)
        xAxis.gridColor = ContextCompat.getColor(this@StaffDetailsOnWeekActivity, R.color.black)
        xAxis.gridLineWidth = 1f
        xAxis.setCenterAxisLabels(false)
        xAxis.granularity = 1f
        /*** To set X label position*/
        row_line_chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        row_line_chart.xAxis.setDrawGridLines(false)
        row_line_chart.xAxis.axisLineWidth = 1f
        /*** To set values based on the api date or time to set this formatter*/
        xAxis.valueFormatter = IndexAxisValueFormatter(weekdays)
        xAxis.setDrawAxisLine(true)
        xAxis.setDrawLabels(true)
        xAxis.mLabelWidth = 5
        /*** To set Y label position*/
        val yAxis = row_line_chart.axisLeft
        row_line_chart.axisLeft.isEnabled = true
        row_line_chart.axisRight.isEnabled = false

        row_line_chart.axisLeft.setDrawGridLines(true)
        yAxis.granularity = 1F
        yAxis.setDrawLabels(true)
        yAxis.setDrawAxisLine(false)
        row_line_chart.animateXY(500, 500)
        row_line_chart.legend.isEnabled = false
        // black lines and points
        set1.color = ContextCompat.getColor(this@StaffDetailsOnWeekActivity, R.color.linegreen1)
        set2.color = ContextCompat.getColor(this@StaffDetailsOnWeekActivity, R.color.linegreen1)
        set1.setCircleColor(ContextCompat.getColor(this@StaffDetailsOnWeekActivity, R.color.linegreen1))
        set2.setCircleColor(ContextCompat.getColor(this@StaffDetailsOnWeekActivity, R.color.linegreen1))
        set1.lineWidth = 2.5f
        set2.lineWidth = 2.5f
        set1.circleRadius = 6.5f
        set2.circleRadius = 6.5f
        set1.circleHoleRadius = 4f
        set2.circleHoleRadius = 4f
        set1.setDrawValues(false)
        set2.setDrawValues(false)


        // draw points as solid circles
        set1.setDrawCircleHole(true)
        set2.setDrawCircleHole(true)

        // customize legend entry
        set1.formLineWidth = 1f
        set2.formLineWidth = 1f
        set1.formLineDashEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
        set2.formLineDashEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
        set1.formSize = 15f
        set2.formSize = 15f

        // text size of values
        set1.valueTextSize = 9f
        set2.valueTextSize = 9f

        // draw selection line as dashed
        set1.enableDashedHighlightLine(10f, 5f, 0f)
        set2.enableDashedHighlightLine(10f, 5f, 0f)

        // set the filled area
        set1.setDrawFilled(false)
        set2.setDrawFilled(false)
        set1.fillFormatter =
            IFillFormatter { _, _ -> binding.staffGraphOneWeek.axisLeft.axisMinimum }
        set2.fillFormatter =
            IFillFormatter { _, _ -> binding.staffGraphOneWeek.axisLeft.axisMinimum }
        // set color of filled area
        val drawable = ContextCompat.getColor(this@StaffDetailsOnWeekActivity, R.color.green)
        set1.fillColor = drawable
        set2.fillColor = drawable

        val dataSets = java.util.ArrayList<ILineDataSet>()

        /*** To set chart Line style here*/
        set1.mode = LineDataSet.Mode.CUBIC_BEZIER
        set2.mode = LineDataSet.Mode.CUBIC_BEZIER
        dataSets.add(set1)
        dataSets.add(set2)
        // create a data object with the data sets
        val data = LineData(dataSets)
        row_line_chart.data = data
        /*** To set label with detail for current position value here*/
        val mv = CustomMarkerView(this@StaffDetailsOnWeekActivity, R.layout.tooltip)
        /*** To set label value in chart*/
        row_line_chart.markerView = mv

        /*** OnChartValueSelectedListener function */
        row_line_chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            @RequiresApi(Build.VERSION_CODES.R)
            override fun onValueSelected(e: Entry, h: Highlight?) {

                toolTipTime = ""
                toolTipDate = ""
                row_line_chart.xAxis.valueFormatter.getFormattedValue(
                    e.x,
                    row_line_chart.xAxis
                )
                /** Update staff data fun user click**/
                updateStaffDetails(
                        row_line_chart.xAxis.valueFormatter.getFormattedValue(
                            e.x,
                            row_line_chart.xAxis
                        ).toString(), StaffGraphData
                    )


                val highlight = arrayOfNulls<Highlight>(
                    row_line_chart.data.dataSets.size
                )

                /** this loop is used to set a value for selected chart point **/
                for (j in 0 until row_line_chart.data.dataSets.size) {
                    val iDataSet: IDataSet<*> = row_line_chart.data.dataSets[j]
                    for (i in (iDataSet as LineDataSet).values.indices) {
                        if (iDataSet.values[i].x == e.x) {
                            highlight[j] = Highlight(e.x, e.y, j)
                            if (toolTipDate == "") {
                                for (i in StaffGraphData.totalPresentCountBydate.indices) {
                                    if (row_line_chart.xAxis.valueFormatter
                                            .getFormattedValue(
                                                e.x,
                                                row_line_chart.xAxis
                                            ) == convertDate(
                                            StaffGraphData.totalPresentCountBydate[i].date,
                                            "yyyy-MM-dd",
                                            "dd/MM"
                                        )
                                    ) {

                                        toolTipTime= StaffGraphData.totalPresentCountBydate[i].totalPresentCount.toString()
                                        toolTipDate = convertDate(StaffGraphData.totalPresentCountBydate[i].date,"yyyy-MM-dd","dd 'th',MMM")

                                    }

                                }
                            }
                        }
                    }
                }

                row_line_chart.highlightValues(highlight)
                row_line_chart.marker

                Log.e(
                    "VAL SELECTED", "Value: " + e.data + ", index: " + h!!.x.toInt()
                            + ", DataSet index: "
                )
                val postion = h.x.toString()
                Log.e("dfdfjd", "Postion IS that$postion")
        }

            override fun onNothingSelected() {
            }
        }
        )
    }
    /** Update the staff data to click different chart points to set UI ,like selected point date... **/
    @RequiresApi(Build.VERSION_CODES.R)
    private fun updateStaffDetails(selectedDate: String, data: StaffGraphcount.Data) {
        for (i in data.totalPresentCountBydate.indices) {
            if(selectedDate==convertDate(data.totalPresentCountBydate[i].date, "yyyy-MM-dd", "dd/MM")){
                setUserSelectedDate(data.totalPresentCountBydate[i].date.toString())
                endDate = data.totalPresentCountBydate[i].date.toString()
            }
        }
        binding.tvStaffSelectedLabel.text =convertDate(
            endDate,
            "yyyy-MM-dd",
            "EEEE, MMMM dd, yyyy"
        )
        // api call to user click graph point
        staffListGet(userSelectedDate)

    }

    /** dateConvert fun here ... **/
    private fun dateConvert(date: String): String {
        var date = date
        var spf = SimpleDateFormat("yyyy-MM-dd")
        val newDate = spf.parse(date)
        spf = SimpleDateFormat("MMM dd, yyyy")
        date = spf.format(newDate)
        return date
    }

    /** Get a All api response data are get here ... **/
    override fun getApiResponse(apiResponseManager: ApiResponseManager<*>) {
        Log.e("reponsseeee", apiResponseManager.response.toString())
        when (apiResponseManager.type) {
            STAFF_GRAPH_DATA -> {
                if(binding!=null){
                    Log.e("reponsseeee", apiResponseManager.response.toString())
                    staffGraphOnWeek = apiResponseManager.response as StaffGraphcount
                    if (staffGraphOnWeek!!.statusCode == SUCCESS_CODE) {
                        if (staffGraphOnWeek!!.data!! != null) {
                        /** Set a 7days Line chart graph data fun here ... **/
                            tempGraphDataSet(staffGraphOnWeek!!.data!!)
                        }
                    }

                }
            }
            STAFF_LIST_DATA -> {
                if(binding!=null){
                    Log.e("reponsseeee", apiResponseManager.response.toString())
                    staffListOnDay = apiResponseManager.response as StaffListOnDate
                    if (staffListOnDay!!.statusCode == SUCCESS_CODE) {
                        if (staffListOnDay!!.data!! != null) {
                            temp_staffListOnDay = staffListOnDay
                            /**  Set data to display list of Staffs on click... **/
                            setHomeOfferData(temp_staffListOnDay!!.data!!)
                        }
                    }else {
                        binding.rvStaff.visibility =View.GONE
                        binding.tvNoStaffAvailable.visibility = View.VISIBLE
                    }

                }
            }

        }
    }
    /** Once getApiResponse function will called ang get data then create list to that data fun here ... **/
    private fun setHomeOfferData(data: ArrayList<StaffListOnDate.Data>) {
        Log.e("setHomeOfferData",data.toString())
        if(isSwitchChecked){
            Log.e("setHomeOfferData",isSwitchChecked.toString())
            binding.staffListView.visibility =View.VISIBLE
        }else{
            Log.e("setHomeOfferData","else")
              binding.staffListView.visibility =View.GONE
        }
        if (data.size > 0) {
            binding.rvStaff.setHasFixedSize(false)
            val layoutManager = GridLayoutManager(this@StaffDetailsOnWeekActivity, 2)
            binding.rvStaff.layoutManager = layoutManager
            val calendar = Calendar.getInstance()
            // Create a SimpleDateFormat to format the date as "dd/MM/yyyy"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val treatment_reason =
                StaffListAdapter(
                    this@StaffDetailsOnWeekActivity,
                    data, this,
                    userSelectedDate== dateFormat.format(calendar.time)
                )
            // Create adapter object
            binding.rvStaff.adapter = treatment_reason
            binding.rvStaff.visibility=View.VISIBLE
            binding.tvNoStaffAvailable.visibility = View.GONE
            treatment_reason.notifyDataSetChanged()

        } else {
            binding.rvStaff.visibility= View.GONE
            binding.tvNoStaffAvailable.visibility = View.VISIBLE
        }


    }
}




/** set a Label view for user click the chart to show the date and staff count for the selected chart position's ... **/

class CustomMarkerView(context: Context?, layoutResource: Int) : MarkerView(
    context,
    layoutResource
) {
    private val tvContent: TextView = findViewById<View>(R.id.data1) as TextView
    private val tvContent1: TextView = findViewById<View>(R.id.data2) as TextView

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    override fun refreshContent(e: Entry, highlight: Highlight) {
        super.refreshContent(e, highlight)
        tvContent.text = toolTipTime // set the entry-value as the display text
        tvContent1.text = toolTipDate // set the entry-value as the display text
    }
    override fun getOffset(): MPPointF {
        return MPPointF((-(width / 2)).toFloat(), (-height).toFloat()) // place the midpoint of marker over the bar
    }
}