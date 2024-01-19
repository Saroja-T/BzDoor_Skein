package com.busydoor.app.fragment

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.busydoor.app.R
import com.busydoor.app.activity.ActivityBase
import com.busydoor.app.activity.AllOffsiteRequestActivity
import com.busydoor.app.activity.CryptLib2
import com.busydoor.app.activity.EditProfileActivity
import com.busydoor.app.activity.RequestOffsiteActivity
import com.busydoor.app.apiService.ApiInitialize
import com.busydoor.app.apiService.ApiRequest
import com.busydoor.app.apiService.ApiResponseInterface
import com.busydoor.app.apiService.ApiResponseManager
import com.busydoor.app.customCalender.CalendarAdapter
import com.busydoor.app.customCalender.CalendarDateModel
import com.busydoor.app.customCalender.HorizontalItemDecoration
import com.busydoor.app.customMethods.*
import com.busydoor.app.databinding.FragmentHomeBinding
import com.busydoor.app.model.HomeDataResponse
import com.busydoor.app.model.UserModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.MPPointF
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit


class HomeFragment : Fragment(),ApiResponseInterface {
    private var pieTitleDate: String = ""
    private var maxDays:Long=0
    private var titleDate: String = ""
    private var apiDate: String =""
    private var apiFormatedDate: String =""
    private var isShowAllRequest: Boolean =false
    private var userID: String =""
    private var premiseID: String=""
    private var homePremisedata: HomeDataResponse? = null
    private lateinit var binding: FragmentHomeBinding
    private val sdf = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
    private val mdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    private val cal = Calendar.getInstance(Locale.ENGLISH)
    private val currentDate = Calendar.getInstance(Locale.ENGLISH)
    private val dates = ArrayList<Date>()
    private lateinit var adapter: CalendarAdapter
    private val calendarList2 = ArrayList<CalendarDateModel>()
    lateinit var objSharedPref: PrefUtils
    private var chart: PieChart? = null
    /*Encryption variables*/
    private var cryptLib: CryptLib2? = null
    private var mProgressDialog: AppCompatDialog? = null
    // This property is only valid between onCreateView and
    var datePickerDialog: DatePickerDialog? = null
    private var tempMaxDays : Int=0
    private var pageNo: Int=0
    lateinit var customMarkerView :CustomMarkerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
        }
    }

    /*** this Function is called initially */
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /*** initialize biding here  */
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        /*** initialize encrypt fun here  */
        cryptLib = CryptLib2()
        /*** initialize shared-preference here  */
        objSharedPref = PrefUtils(requireContext())
        /*** set current data and change format to the Api ("yyyy-MM-dd") here  */
        val cDate = Date()
        apiFormatedDate = SimpleDateFormat("yyyy-MM-dd").format(cDate)
        userID = getUserModel()?.data?.userId.toString()
        premiseID = activity?.intent?.getStringExtra("premiseId").toString()
        Log.e("original value home== ",premiseID.toString())

        binding.userProfileView.editProfile.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }
        /*** Function to setup Date view */
        binding. recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                /*** Function to setup Month Tittle in calendar view */
                updateMonthTitle()
                val layoutManager1 = recyclerView.layoutManager as LinearLayoutManager?
                if (dx > 0) {
                    println("Scrolled Right")
                } else if (dx < 0) {
                    if (layoutManager1 != null) {
                        val firstVisibleItemPosition = layoutManager1.findFirstVisibleItemPosition()
                        println("Scrolled Left position: $firstVisibleItemPosition")
                        if(firstVisibleItemPosition==0){
                            pageNo += 1;
                            val titileDate2 = mdf.parse(titleDate)
                            val initialDate=mdf.parse("2023-01-01")
                            cal.time = titileDate2
                            val cal2 = Calendar.getInstance(Locale.ENGLISH)
                            cal.add(Calendar.MONTH, -1)
                            cal.set(Calendar.DAY_OF_MONTH, cal2.getActualMaximum(Calendar.DAY_OF_MONTH))
                            // checks initial date with title date which is after the initial date
                            if(titileDate2.after(initialDate)){
                                if (cal == currentDate)
                                    setUpCalendar()
                                else
                                    setUpCalendar()}
                        }
                    }
                    println("Scrolled Left")
                } else {
                    println("No Horizontal Scrolled")
                }
            }
        }
        )
        setUpAdapter()
        setUpClickListener()
        setUpCalendar()
        // Inflate the layout for this fragment
        chart = binding.chart1
        /*** Function to Show Tooltip on PIE-chart */
        customMarkerView = CustomMarkerView(requireContext(), R.layout.piechart_toolbar)
        chart?.clear()
        chart?.setUsePercentValues(false)
        chart?.getDescription()?.setEnabled(false)
        chart?.setDragDecelerationFrictionCoef(0.95f)
        chart?.setDrawHoleEnabled(true)
        chart?.setTransparentCircleColor(Color.WHITE)
        chart?.setTransparentCircleAlpha(110)
        chart?.setHoleRadius(58f)
        chart?.setTransparentCircleRadius(61f)
        chart?.setDrawCenterText(true)
        chart?.setRotationAngle(10F)
        // enable rotation of the chart by touch
        chart?.setRotationEnabled(true)
        chart?.setHighlightPerTapEnabled(true)
        // Make sure it's clickable.
        chart?.setClickable(true)
        chart?.setMinAngleForSlices(20f)
        /*** Function to Call premise-Api */
        globalDate = apiFormatedDate
        homeDataGet(apiFormatedDate)
        /*** Function to navigate dashboard page */
        binding.userProfileView.backPage.setOnClickListener{
            requireActivity().finish()
        }
        binding.requestOffcite.setOnClickListener{
            if(!isShowAllRequest) {
                startActivity(
                    Intent(
                        requireActivity(),
                        RequestOffsiteActivity::class.java
                    ).putExtra("userSelectDate", apiFormatedDate).putExtra("premise_id", premiseID)
                )
            }else{
                startActivity(
                    Intent(
                        requireActivity(),
                        AllOffsiteRequestActivity::class.java
                    ).putExtra("userSelectDate", apiFormatedDate).putExtra("premise_id", premiseID)
                )
            }
        }
        return root

    }

    /*** all onclick function are set here  */
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chart?.setOnChartGestureListener(object : OnChartGestureListener {
            override fun onChartTranslate(me: MotionEvent, dX: Float, dY: Float) {

            }

            override fun onChartSingleTapped(me: MotionEvent) {
                Log.e("dffdfdfd","sdfsfsfsf")
            }


            override fun onChartGestureStart(
                me: MotionEvent?,
                lastPerformedGesture: ChartTouchListener.ChartGesture?
            ) {

            }

            override fun onChartGestureEnd(
                me: MotionEvent?,
                lastPerformedGesture: ChartTouchListener.ChartGesture?
            ) {

            }

            override fun onChartLongPressed(me: MotionEvent) {
//                callRequestOffsiteActivity()
            }

            override fun onChartFling(
                me1: MotionEvent, me2: MotionEvent,
                velocityX: Float, velocityY: Float
            ) {

            }

            override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {

            }

            override fun onChartDoubleTapped(me: MotionEvent) {
//                callRequestOffsiteActivity()
            }
        })
    }

    /*** Function to navigate the request offsite page */
    private fun callRequestOffsiteActivity() {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val sdf1 = SimpleDateFormat("EEEE, MMM dd,yyyy")
        val myDate: Date = sdf.parse(apiDate)
        val cal = Calendar.getInstance()
        cal.time = myDate
        cal.add(Calendar.DAY_OF_MONTH, -6)
        val starDate1 = convertDate(sdf1.format(cal.time), "EEEE, MMM dd,yyyy", "yyyy-MM-dd")
        startActivity(
            Intent(
                context,
                RequestOffsiteActivity::class.java
            ).putExtra("start_date",apiFormatedDate)
                .putExtra("premiseId", premiseID)
                .putExtra("status",homePremisedata!!.data!!.donutDetails.toString())

        )
    }

    /*** Set up click listener to click the Date on date Picker */
    private fun setUpClickListener() {
        binding.ivCalendarNext.setOnClickListener {
            pageNo = pageNo-1
            val titileDate2 = mdf.parse(titleDate)
            cal.setTime(titileDate2)
            cal.set(Calendar.DAY_OF_MONTH, 5);
            cal.add(Calendar.MONTH, 1)
            // Checks current date with upcomming date
            if(cal.time.before(Calendar.getInstance().time)){
                setUpCalendar()
            }
        }
        binding.ivCalendarPrevious.setOnClickListener {
            pageNo = pageNo+1
            val titileDate2 = mdf.parse(titleDate)
            val initialDate=mdf.parse("2023-01-01")
            cal.setTime(titileDate2)
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.add(Calendar.MONTH, -1)
            // checks initial date with title date which is after the initial date
            if(titileDate2.after(initialDate)){
                if (cal == currentDate)
                    setUpCalendar()
                else
                    setUpCalendar()}
        }
    }

    /*** Setting up adapter for recyclerview */
    @RequiresApi(Build.VERSION_CODES.R)
    private fun setUpAdapter() {
        binding.recyclerView.addItemDecoration(HorizontalItemDecoration(10))
        val snapHelper: SnapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.recyclerView)
        adapter = CalendarAdapter { calendarDateModel: CalendarDateModel, position: Int ->
            calendarList2.forEachIndexed { index, calendarModel ->

                calendarModel.isSelected = index == position
                if(calendarModel.isSelected){
                    apiFormatedDate = parseDateFormat(calendarDateModel.calendarDateStr).toString()
                    globalDate =apiFormatedDate
                    homeDataGet(apiFormatedDate)
                }

            }
            adapter.setData(calendarList2)
        }
        binding.recyclerView.adapter = adapter
    }

    /**
     * Function to setup updateMonthTitle
     */
    private fun updateMonthTitle() {
        val layoutManager = binding.recyclerView.layoutManager as LinearLayoutManager
        val visibleItemPosition = layoutManager.findLastVisibleItemPosition()
        val date = adapter.getItemDate(visibleItemPosition)
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        titleDate = mdf.format(date)
        binding.tvDateMonth.text = sdf.format(date)
    }

    /*** Function to setup calendar for every month */
    private fun getDatesFromInput(inputDateString: String, numberOfDays: Int): List<String> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val inputDate = dateFormat.parse(inputDateString)

        if (inputDate != null) {
            val calendar2 = Calendar.getInstance()
            calendar2.time = inputDate

            val dateList = mutableListOf<String>()

            for (i in 0 until numberOfDays) {
                dateList.add(dateFormat.format(calendar2.time))
                calendar2.add(Calendar.DAY_OF_MONTH, 1)
            }

            return dateList
        }

        return emptyList()
    }

    /*** Function to setup calculateDifference */
    private fun calculateDifference(fromDate: String,toDate: String): Long {
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
    /*** Function to setup calculateDateDifference */
    private fun calculateDateDifference(pastDateString: String): Long {
        // Define the date format
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")

        // Get the current date
        val currentDate = Date()

        // Parse the past date string into a Date object
        val pastDate = dateFormat.parse(pastDateString)

        // Calculate the difference between current date and past date
        val differenceInMillis = currentDate.time - pastDate.time

        // Convert the time difference to days
        val differenceInDays = TimeUnit.MILLISECONDS.toDays(differenceInMillis)
        println("differenceInDays $differenceInDays")
        return differenceInDays
    }
    /*** Function to setup getCurrentTimeWithInitialDateOfMonth */
    fun getCurrentTimeWithInitialDateOfMonth(): Triple<Int, Int, Int> {
        val calendar = Calendar.getInstance()
        if(apiDate.isNotEmpty()){
            val inputDate = mdf.parse(apiDate) ?: Date()
            calendar.time=inputDate}
        // Set the day of the month to 1
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1-pageNo // Note: Calendar months are zero-based
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        maxDays=calculateDateDifference("$year-$month-$day")+1

        lateinit var date: Triple<Int, Int, Int>
        if(maxDays<6){
            date = Triple(year, month-1, day)
        }else{
            date = Triple(year, month, day)
        }

        return date
    }
    /*** Function to setup setUpCalendar */
    private fun setUpCalendar() {
        val calendarList = ArrayList<CalendarDateModel>()

        // Print statement for debugging
        println("Setting up calendar...")

        binding.tvDateMonth.text = sdf.format(cal.time)
        val monthCalendar = cal.clone() as Calendar
        val maxDaysInMonth = calculateDateDifference("${getCurrentTimeWithInitialDateOfMonth().first}-${getCurrentTimeWithInitialDateOfMonth().second}-${getCurrentTimeWithInitialDateOfMonth().third}")+1
        if(pageNo==0){tempMaxDays= maxDaysInMonth.toInt()}
        println("Cleared dates list $maxDaysInMonth")
        dates.clear()

        monthCalendar.set(getCurrentTimeWithInitialDateOfMonth().first, getCurrentTimeWithInitialDateOfMonth().second-1, getCurrentTimeWithInitialDateOfMonth().third)
        while (dates.size < maxDaysInMonth) {
            val currentDate = monthCalendar.time
            dates.add(currentDate)
            calendarList.add(CalendarDateModel(currentDate))
            println("Added date:$currentDate")
            monthCalendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        calendarList2.clear()
        println("Cleared calendarList2")

        calendarList2.addAll(calendarList)
        println("Added all items from calendarList to calendarList2")

        if(apiDate.isEmpty()){
            calendarList2[calendarList2.lastIndex].isSelected = true;
            if (pageNo==0){
                /// Scroll to end
                binding.recyclerView.scrollToPosition(calculateDifference("${getCurrentTimeWithInitialDateOfMonth().first}-${getCurrentTimeWithInitialDateOfMonth().second}-${getCurrentTimeWithInitialDateOfMonth().third}",mdf.format(cal.time).toString()).toInt())
            }else{
                var tempMaxDays2 = (maxDaysInMonth - tempMaxDays).toInt()
                tempMaxDays= maxDaysInMonth.toInt()
                binding.recyclerView.scrollToPosition(tempMaxDays2)

            }
            adapter.setData(calendarList)
        }else {
            val matchingObjects = calendarList2.filter { mdf.format(it.data) == apiDate }
            if (matchingObjects.isNotEmpty()) {
                matchingObjects.forEach {
                    // Change the value of otherProperty for matched objects
                    it.isSelected = true
                }
            } else {
                calendarList2[calendarList2.lastIndex].isSelected = true;
            }
            if (pageNo==0) {
                // to scroll to api date

                binding.recyclerView.scrollToPosition(
                    calculateDifference(
                        "${getCurrentTimeWithInitialDateOfMonth().first}-${getCurrentTimeWithInitialDateOfMonth().second}-${getCurrentTimeWithInitialDateOfMonth().third}",
                        apiDate
                    ).toInt()
                )
            }else{
                Log.e("testtt","")
                var tempMaxDays2 = (maxDaysInMonth - tempMaxDays).toInt()
                tempMaxDays= maxDaysInMonth.toInt()

                binding.recyclerView.scrollToPosition(tempMaxDays2+3)
            }
            adapter.setData(calendarList)
            println("Set data in adapter")

        }
    }
    /*** Function to setup parseDateFormat */
    fun parseDateFormat(time: String?): String? {
        val inputPattern = "dd-MM-yyyy"
        val outputPattern ="yyyy-MM-dd"
        val inputFormat = SimpleDateFormat(inputPattern)
        val outputFormat = SimpleDateFormat(outputPattern)
        var date: Date? = null
        var str: String? = null
        try {
            date = inputFormat.parse(time)
            str = outputFormat.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return str
    }

    /*** Function to api homeDataGet */
    @RequiresApi(Build.VERSION_CODES.R)
    private fun homeDataGet(date:String) {
        Log.e("original value","Bearer ${getUserModel()!!.data.token}");
        if (isOnline(requireContext())) {
            ApiRequest(
                requireActivity(),
                ApiInitialize.initialize(ApiInitialize.LOCAL_URL).userPremiseData(
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

    /*** Function to setup showProgress */
    private fun showProgress(isShowProgressDialog:Boolean) {
        if (isShowProgressDialog) {
//             if (mProgressDialog == null)
            mProgressDialog = getProgressDialog(requireContext())
        }
    }
    /*** Function to setup dismissProgress */
    private fun dismissProgress(isShowProgressDialog:Boolean) {
        Log.e("dismis","dsds")
        if (isShowProgressDialog) {
            Utils.dismissDialog(activity, mProgressDialog!!)
        }
    }
    /*** Function to setup setPremiseData */
    fun setPremiseData(
        dataModel: HomeDataResponse.Data
    ) {

        val circularProgressDrawable = CircularProgressDrawable(requireContext())
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()

        if(binding.userProfileView.PremiseStaffImage !=null) {
            Glide.with(requireContext())
                .load(dataModel.userDetails!!.staffImage)
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

        when (dataModel.userDetails!!.staffStatus) {
            "in" -> {
                binding.userProfileView.staffStatus.setImageResource(R.drawable.icon_staff_profile_in)
                binding.checkOutTime.setTextColor(Color.GRAY)
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

        binding.userProfileView.userName.text =homePremisedata!!.data!!.userDetails!!.staffFirstName +" "+homePremisedata!!.data!!.userDetails!!.staffLastName;
        binding.userProfileView.userNumber.text =homePremisedata!!.data!!.premiseDetails!!.premiseName +", "+homePremisedata!!.data!!.premiseDetails!!.city+", "+homePremisedata!!.data!!.premiseDetails!!.state
    }

    /*** Function to setup all getApiResponse here */
    override fun getApiResponse(apiResponseManager: ApiResponseManager<*>) {
        Log.e("reponsseeee", apiResponseManager.response.toString())
        when (apiResponseManager.type) {
            HOME_DATA_GET -> {
                if(binding!=null){
                    Log.e("reponsseeee", apiResponseManager.response.toString())
                    homePremisedata = apiResponseManager.response as HomeDataResponse
                    if (homePremisedata!!.statusCode == SUCCESS_CODE) {
                        if (homePremisedata!!.data != null) {
                            setPremiseData(homePremisedata!!.data!!)
                            if(homePremisedata!!.data !=null && homePremisedata!!.data!!.donutDetails  !=null &&
                                homePremisedata!!.data!!.donutDetails.toString().length !=null){
                                Log.e("donutdetails", homePremisedata!!.data!!.donutDetails.toString());
                                binding.donutView.visibility = View.VISIBLE

                                /*** request offsite or alloffsite image **/
                                if(homePremisedata!!.data!!.donutDetails!!.isShowRequestOffsite ==false){
                                    isShowAllRequest=false
                                    binding.requestOffcite.visibility = View.VISIBLE
                                    binding.requestOffcite.setImageResource(R.drawable.icon_request)
                                }else if(homePremisedata!!.data!!.donutDetails!!.isShowRequestOffsite ==true){
                                    isShowAllRequest=true
                                    binding.requestOffcite.visibility = View.VISIBLE
                                    binding.requestOffcite.setImageResource(R.drawable.icon_all_request)
                                }
                                /*** set data to pie_chart image **/
                                setDataLine(homePremisedata!!.data!!.donutDetails!!)
                                /*** set data to texts images **/
                                setDataToUI(homePremisedata!!.data!!.donutDetails!!)
                            }else{
                                chart?.clear()
                                binding.donutView.visibility = View.GONE
                            }
                        }

                    }
                }

            }
        }
    }
    /*** Function to setup all text and images */
    private fun setDataToUI(dataModel: HomeDataResponse.Data.DonutDetails) {
        if(dataModel!!.firstInTime ==null){
            binding.checkInTime.text= "--:--"
        }else {
            binding.checkInTime.text= dataModel.firstInTime.toString()
        }
        if(dataModel!!.lastOutTime ==null){
            binding.checkOutTime.text= "--:--"
        }else {
            binding.checkOutTime.text = dataModel.lastOutTime.toString()
        }
    }
    /*** Function to setup PIE Chart */
    private fun setDataLine(dataModel: HomeDataResponse.Data.DonutDetails) {
        val xAxisLabelList  = java.util.ArrayList<String>()
        chart?.setExtraOffsets(35f, 0f, 35f, 0f)
        val timeHHMMSS = dataModel.totalInHours.toString().split(":")
        if( timeHHMMSS.size > 2 ) {
            binding.centerHourTime.text = timeHHMMSS[0]
            binding.centerMinsTime.text = timeHHMMSS[1]
        }
        else
            binding.centerHourTime.text = "Incorrect time format detected"

        // Custom renderer used to add dots at the end of value lines.
        val entries: java.util.ArrayList<PieEntry> = java.util.ArrayList()
        for (y in xAxisLabelList) {
            Log.e("lable-value",y);
        }
        // Blue - #convertDate
        val in_Color = intArrayOf(
            Color.rgb(45, 100, 188),
        )

        // light_grey - #99cf43
        val Offline_Color = intArrayOf(
            Color.rgb(199, 196, 196),
        )
        // grey - #ffffff
        val Remaining_Color = intArrayOf(
            Color.rgb(234, 234, 234)
        )

        // orange - #ffffff
        val Offsite_color = intArrayOf(
            Color.rgb(251, 169, 96, )
        )

        val colors = java.util.ArrayList<Int>()

        if(dataModel.totalInPercentage!! >0){
            entries.add(
                PieEntry(
                    dataModel.totalInPercentage!!.toFloat(),
                    "Online"
                )

            )
            for (c in in_Color) colors.add(c)

        }

        if(dataModel.totalOfflinePercentage!! >0){
            entries.add(
                PieEntry(
                    dataModel.totalOfflinePercentage!!.toFloat(),
                    "Offline"
                )
            )
            for (c in Offline_Color) colors.add(c)}

        if(dataModel.totalRequestOffsitePercentage!! >0){
            entries.add(
                PieEntry(
                    dataModel.totalRequestOffsitePercentage!!.toFloat(),
                    "Offsite"
                )

            )
            for (c in Offsite_color) colors.add(c)
        }

        if(dataModel.totalRemainingPercentage!! >0) {
            entries.add(
                PieEntry(
                    dataModel.totalRemainingPercentage!!.toFloat(),
                    "remaining"
                )
            )
            for (c in Remaining_Color) colors.add(c)
        }


//        for(y in dataModel.){
//            if(y.Status == "Open"){
//                for (c in Open_Color) colors.add(c)
//            }
//            else if(y.Status == "Offline"){
//                for (c in Offline_Color) colors.add(c)
//            }
//            else if(y.Status == "Closed"){
//                for (c in Closed_Color) colors.add(c)
//            }else if(y.Status == "Remaining"){
//                for (c in Remaining_Color) colors.add(c)
//            }
//        }


//        for (c in Offline_Color) colors.add(c)
//        for (c in Offsite_Color) colors.add(c)
//        for (c in Remaining_Color) colors.add(c)

        val dataSet = PieDataSet(entries, "sds")
        dataSet.setGradientColor(Color.RED,Color.GREEN);

//        dataSet.colors = colors
        // Set the number of colors needed
//        chart?.renderer = CustomPieChartRendererLine(chart!!)
//        dataSet.setGradientColors(colorss)
        //dataSet.setValueTextColors(colors)
        // Value lines
//        dataSet.valueLinePart1Length = 0.5f
//        dataSet.valueLinePart2Length = 0.2f
//        dataSet.valueLineWidth = 1.5f
//        dataSet.valueLinePart1OffsetPercentage = 115f  // Line starts outside of chart
//        dataSet.isUsingSliceColorAsValueLineColor = true

        // Value text appearance
//        dataSet.xValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
//        dataSet.valueTypeface = Typeface.DEFAULT_BOLD
//        dataSet.setValueFormatter(HourYAxisValueFormatter())



        chart?.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {

            override fun onValueSelected(e: Entry?, h: Highlight?) {
                if (e != null) {
                    Log.e("onValueSelected", (e.x + e.y).toString())
                    customMarkerView.refreshContent(e,h)
                    chart?.marker = customMarkerView
                }
            }

            override fun onNothingSelected() {

            }
        })
        dataSet.selectionShift = 3f
        dataSet.setDrawValues(false)
        chart?.legend?.isEnabled = false
        chart?.description?.isEnabled = false
        chart?.isRotationEnabled = false
        chart?.dragDecelerationFrictionCoef = 0.9f
        chart?.rotationAngle = 270f
        chart?.isHighlightPerTapEnabled = true
        chart?.animateY(1400, Easing.EaseInOutQuad)
        // Hole
        chart?.isDrawHoleEnabled = true
        chart?.holeRadius = 85f
//        chart?.setEntryLabelTextSize(9f)
//        // Center text
//        chart?.setEntryLabelColor(Color.BLACK)
//        chart?.setEntryLabelTypeface(Typeface.DEFAULT_BOLD)
        chart?.setDrawEntryLabels(false)
        chart?.setDrawCenterText(false)
        chart?.setCenterTextSize(20f)
        chart?.setCenterTextTypeface(Typeface.DEFAULT_BOLD)
        chart?.setCenterTextColor(Color.parseColor("#222222"))
        chart?.centerText = pieTitleDate
        dataSet.colors = colors
        chart?.setMinAngleForSlices(20f);

        // Disable legend & description
        chart?.legend?.isEnabled = false
        chart?.description = null
        chart?.data = PieData(dataSet)
        chart?.invalidate()

    }

}

/*** Function to setup Tooltip fun */
class CustomMarkerView(context: Context?, layoutResource: Int) : MarkerView(
    context,
    layoutResource
) {
    private val tvContent: TextView = findViewById<View>(R.id.tvContent) as TextView

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    override fun refreshContent(e: Entry, highlight: Highlight?) {
        super.refreshContent(e, highlight)
        Log.e("onValueSelected", (e.x + e.y).toString())
        // convert the Int minutes entry-value as the display Time format
        val startTime = "00:00"
        val minutes = e.y.toInt()
        val h = minutes / 60 + startTime.substring(0, 1).toInt()
        val m = minutes % 60 + startTime.substring(3, 4).toInt()
        val newtime = "$h:$m hrs"
        // set the entry-value as the display text
        tvContent.text = newtime
    }


    override fun getOffset(): MPPointF {
        return MPPointF((-(width / 2)).toFloat(), (-height).toFloat()) // place the midpoint of marker over the bar
    }


    fun conVertDateTimeToTimeStarp(date: String): String {
        var tempDate:String = ""
        if(date!=""){
            val sdf = SimpleDateFormat("HH:mm:ss")
            val date: Date = sdf.parse(date)
            println("Given Time in milliseconds : " + date.time)
            tempDate = date.time.toString() + "f"
        }

        return tempDate
    }
}