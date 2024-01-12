package com.busydoor.app.activity

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.graphics.DashPathEffect
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.busydoor.app.R
import com.busydoor.app.adapter.StaffListAdapter
import com.busydoor.app.apiService.ApiInitialize
import com.busydoor.app.apiService.ApiRequest
import com.busydoor.app.apiService.ApiResponseInterface
import com.busydoor.app.apiService.ApiResponseManager
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

import kotlin.collections.ArrayList

class StaffDetailsOnWeekActivity : ActivityBase(),ApiResponseInterface,HomeClick {
    private var values = java.util.ArrayList<Entry>()
    private var startDate : String = ""
    private var selectedDate : String = ""
    private var endDate : String = ""
    private var PremiseId : String = ""
    private var staffGraphOnWeek: StaffGraphcount? = null
    private var staffListOnDay: StaffListOnDate? = null
    private var temp_staffListOnDay: StaffListOnDate? = null
    private var isSwitchChecked : Boolean = true
    /*** Create a binding for current Activity*/
    val binding by lazy { ActivityStaffDetailsGraphInweekBinding.inflate(layoutInflater) }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        objSharedPref = PrefUtils(this);
        PremiseId = intent.getStringExtra("premiseId").toString()
        Log.e("premise_id",PremiseId.toString())


        binding.calendarIcon.setOnClickListener {
            showDatePicker()
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
        binding.offsiteHeading.text = convertDate(globalDate,"yyyy-MM-dd","EEE - dd MMM',' yyyy")
        binding.tvStaffSelectedLabel.text = convertDate(globalDate,"yyyy-MM-dd","EEEE, MMMM dd, yyyy")
        /*** Initial APi call to staff 7days Data*/
        staffGraphGet(globalDate)
        /*** Initially to call staffList Api to get a data in Current date*/
        staffListGet(globalDate)
    }

    /*** Function to show date picker*/
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

                // Update the TextView to display the selected date with the format
                binding.offsiteHeading.text= convertDate(formattedDate,"yyyy-MM-dd","EEE - dd 'th' MMM',' yyyy")
                binding.tvStaffSelectedLabel.text = convertDate(formattedDate,"yyyy-MM-dd","EEEE, MMMM dd, yyyy")
                /*** Function to staffListGet when click datePicker select a date to call api request */
                staffGraphGet(formattedDate)
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
                startDate = data.totalPresentCountBydate[i].date.toString()
                endDate = data.totalPresentCountBydate[i].date.toString()
            }
        }
        binding.tvStaffSelectedLabel.text =convertDate(
            endDate,
            "yyyy-MM-dd",
            "EEEE, MMMM dd, yyyy"
        )
        // api call to user click graph point
        staffListGet(startDate)

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
            val treatment_reason =
                StaffListAdapter(
                    this@StaffDetailsOnWeekActivity,
                    data, this
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