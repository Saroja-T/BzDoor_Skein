//package com.bzboss.app.adapter
//
//import android.Manifest
//import android.annotation.SuppressLint
//import android.app.Activity
//import android.app.Dialog
//import android.app.ProgressDialog
//import android.content.ActivityNotFoundException
//import android.content.Context
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.content.pm.ResolveInfo
//import android.graphics.Color
//import android.graphics.DashPathEffect
//import android.net.Uri
//import android.os.AsyncTask
//import android.os.Build
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.view.Window
//import android.widget.*
//import androidx.annotation.RequiresApi
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import androidx.core.content.FileProvider
//import androidx.fragment.app.FragmentManager
//import androidx.recyclerview.widget.RecyclerView
//import androidx.swiperefreshlayout.widget.CircularProgressDrawable
//import com.bumptech.glide.Glide
//import com.bumptech.glide.load.engine.DiskCacheStrategy
//import com.bzboss.app.R
//import com.bzboss.app.activity.CustomMarkerView
//import com.bzboss.app.custom.*
//import com.bzboss.app.model.StaffDetailsGraphData
//import com.bzboss.app.restapi.ApiInitialize
//import com.busydoor.app.interfaceD.StaffClick
//import com.github.mikephil.charting.charts.LineChart
//import com.github.mikephil.charting.components.XAxis
//import com.github.mikephil.charting.data.Entry
//import com.github.mikephil.charting.data.LineData
//import com.github.mikephil.charting.data.LineDataSet
//import com.github.mikephil.charting.formatter.IFillFormatter
//import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
//import com.github.mikephil.charting.highlight.Highlight
//import com.github.mikephil.charting.interfaces.datasets.IDataSet
//import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
//import com.github.mikephil.charting.listener.OnChartValueSelectedListener
//import java.io.File
//import java.text.SimpleDateFormat
//import java.util.*
//
//
//class StaffDetailstAdapter(
//    private val context: Context,
//    private val commingOfferList: ArrayList<StaffDetailsGraphData.Data.StaffDetailsData>,
//    private var StaffClick: StaffClick,
//    var from: String,
//    private var supportFragmentManager: FragmentManager,
//    var premiseID: String,
//    private var staff_Status: String,
//) : RecyclerView.Adapter<StaffDetailstAdapter.InnerViewHolder>(), ActivityCompat.OnRequestPermissionsResultCallback {
//    private var values = ArrayList<Entry>()
//    private var REQUEST_WRITE_PERMISSION = 0
//    private var selecetdMonth : String = ""
//    private var staf_id : String = ""
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InnerViewHolder {
//        Log.e("check status",staff_Status)
//        return InnerViewHolder(
//                LayoutInflater.from(parent.context).inflate(
//                        R.layout.item_view_staff_list, parent, false
//                )
//
//        )
//    }
//
//    override fun getItemCount(): Int {
//        return commingOfferList.size
//    }
//
//
//    override fun onBindViewHolder(holder: InnerViewHolder, position: Int) {
//        holder.bind(position, commingOfferList)
//
//        holder.apply {
//            values = ArrayList()
//            values.clear()
//            val xAxisLabelList = ArrayList<String>()
//            var staffStatus= ""
//            var firstname= ""
//            var id=""
//            var image=""
//            val model: StaffDetailsGraphData.Data.StaffDetailsData = commingOfferList[position]
//            val circularProgressDrawable = CircularProgressDrawable(context)
//            circularProgressDrawable.strokeWidth = 5f
//            circularProgressDrawable.centerRadius = 30f
//            circularProgressDrawable.start()
//
//            llStaffDetails.visibility = View.VISIBLE
//            ll_staff_list.visibility = View.GONE
//            firstname = model.first_name
//            id = model.staff_id.toString()
//            staf_id = model.staff_id.toString()
//            for (j in model.staffdata.indices){
//                image = model.staffdata[j].first_appearance_image
//                if (model.staffdata[j].first_appearance_date_time.isNotEmpty()) {
//                    Log.e("stafffffffDatatatatat", conVertDateTimeToTimeStarp(convertDate(model.staffdata[j].first_appearance_date_time, "yyyy-MM-dd HH:mm:ss", "HH:mm:ss")).toFloat().toString())
//                    values.add(
//                            Entry(
//                                    j.toFloat(),
//                                    conVertDateTimeToTimeStarp(convertDate(model.staffdata[j].first_appearance_date_time, "yyyy-MM-dd HH:mm:ss", "HH:mm:ss")).toFloat()
//                            )
//                    )
//                    xAxisLabelList.add(
//                            convertDate(model.staffdata[j].first_appearance_date_time, "yyyy-MM-dd HH:mm:ss", "dd/MM")
//
//                    )
//                }
//            }
//
//            Glide.with(context)
//                    .load(image)
//                    .placeholder(R.drawable.home_btn_get_started)
//                    .diskCacheStrategy(DiskCacheStrategy.NONE)
//                    .skipMemoryCache(true)
//                    .into(imStaffImage)
//            val imagePopup = ImagePopup(context)
//            imagePopup.windowHeight = 800 // Optional
//
//            imagePopup.windowWidth = 800 // Optional
//
//            imagePopup.backgroundColor = Color.BLACK // Optional
//
//            imagePopup.isFullScreen = true // Optional
//
//            imagePopup.isHideCloseIcon = false // Optional
//
//            imagePopup.isImageOnClickClose = true // Optional
//
//
//            val imageView = imStaffImage
//
//            imageView.setOnClickListener {
//                /** Initiate Popup view
//                 * Initiate Popup view*/
//                imagePopup.initiatePopupWithGlide(image)
//                imagePopup.viewPopup()
//            }
//            tvStaffName.text = firstname
//
//            //staffStatus was send by the previous page
//            //check condition based on show the green or red dot
//            Log.e("check status123", commingOfferList[position].staffdata[0].toString())
//
//            Log.e("testts", staff_Status.length.toString())
//            if(commingOfferList[position].staffdata[0].staffStatus==null){
//                staffdetailStatus.visibility =View.GONE
//            }else if(commingOfferList[position].staffdata[0].staffStatus.trim().lowercase(Locale.getDefault()) =="online"){
//                staffdetailStatus.setImageResource(R.drawable.online)
//                staffdetailStatus.visibility =View.VISIBLE
//            }else if(commingOfferList[position].staffdata[0].staffStatus.trim().lowercase(Locale.getDefault()) =="offline"){
//                staffdetailStatus.setImageResource(R.drawable.offline)
//                staffdetailStatus.visibility =View.VISIBLE
//            }else{
//                staffdetailStatus.visibility =View.GONE
//            }
//
//
//            setLineChartData(10f, xAxisLabelList, lc_my_goal_activity_log_chart, model.staffdata)
//
//            val cal: Calendar = Calendar.getInstance()
//            val sdf = SimpleDateFormat("EEEE, MMM dd,yyyy")
//            selecetdMonth = convertDate(sdf.format(cal.time), "EEEE, MMM dd,yyyy", "yyyy-MM-dd")
//            tvMonthYearSelect.text = convertDate(sdf.format(cal.time), "EEEE, MMM dd,yyyy", "MMMM , yyyy")
//
//            Log.d("TAG", "onBindViewHolder: ------"+"dfjdf")
//            tvMonthYearSelect.setOnClickListener {
//                MonthYearPickerDialog().apply {
//                    setListener { view, year, month, dayOfMonth ->
//                        var monthVal = ""
//                        monthVal = if(month<=9){
//                            "0$month"
//                        }else{
//                            month.toString()
//                        }
//                        Log.d("msn", "onCreate: Set date: 01/$monthVal/$dayOfMonth")
//                        selecetdMonth = "$dayOfMonth-$monthVal-01"
//                        callmethod(tvMonthYearSelect)
//                    }
//                    show(supportFragmentManager, "MonthYearPickerDialog")
//                }
//            }
//
//            ibDownLoad.setOnClickListener {
//                if(selecetdMonth!= ""){
//                    requestPermission(id)
//                }else{
//                    toast("Please select month and year", true, context)
//
//                }
//            }
//
//        }
//        holder.ll_staff_list.setOnClickListener {
//            StaffClick.staffPostionClick(position)
//        }
//
//    }
//
//    private fun callmethod(tvMonthYearSelect: TextView) {
//        tvMonthYearSelect.text = convertDate(selecetdMonth, "yyyy-MM-dd", "MMMM , yyyy")
//
//    }
//
//
//    private fun requestPermission(id: String) {
//        if (ContextCompat.checkSelfPermission(context,
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(
//                (context as Activity), arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
//                    REQUEST_WRITE_PERMISSION)
//        } else {
//            download(id)
//        }
//
//    }
//
//    fun download(id: String) {
//        Log.d("Download complete", "----------")
//        var file_path : String =""
//        var uri: Uri
//        class DownloadFile : AsyncTask<String?, Void?, String?>(){
//
//            var uploading: ProgressDialog? = null
//
//            override fun onPreExecute() {
//                super.onPreExecute()
//                uploading =
//                        ProgressDialog.show(
//                                context,
//                                "Please Wait",
//                                "PDF Downloading...",
//                                false,
//                                false
//                        )
//            }
//            override fun doInBackground(vararg strings: String?): String? {
//                val fileUrl: String = strings[0]!! // -> http://maven.apache.org/maven-1.x/maven.pdf
//                val fileName: String = strings[1]!! // -> maven.pdf
//                val localStorage = context.getExternalFilesDir(null)
//
//                val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US)
//                val now = Date()
//                val tempFilename :String = "staffdetails"+"_"+selecetdMonth+"_"
//                val storagePath = localStorage!!.absolutePath
//                val rootPath = "$storagePath/$tempFilename"
//
//                val file = File(rootPath + formatter.format(now).toString() + ".pdf")
//                try {
//                    val permissionCheck = ContextCompat.checkSelfPermission(
//                            context,
//                            Manifest.permission.WRITE_EXTERNAL_STORAGE
//                    )
//                    if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
//                        if (!file.createNewFile()) {
//                            Log.i("Test", "This file is already exist: " + file.absolutePath)
//                        }
//                        file_path = file.absolutePath
//
//
//                        uri = if (Build.VERSION.SDK_INT < 24) {
//                            Uri.fromFile(file)
//                        } else {
//                            Uri.parse(file.path) // My work-around for new SDKs, worked for me in Android 10 using Solid Explorer Text Editor as the external editor.
//                        }
//
//                    }
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//
//                FileDownloader.downloadFile(fileUrl, file)
//                return null
//            }
//            override fun onPostExecute(result: String?) {
//                super.onPostExecute(result)
//                uploading?.dismiss()
//                Log.d("result123", "onPostExecute: $result")
//                if(file_path!=""){
//                    val d = Dialog(context)
//                    d.setTitle("File Download")
//                    d.setContentView(R.layout.downloaded_dialog_layout)
//                    val btnView: Button = d.findViewById<View>(R.id.btnView) as Button
//                    val btnShare: Button = d.findViewById<View>(R.id.btnShare) as Button
//                    val tvTitle: TextView = d.findViewById<View>(R.id.tvTitle) as TextView
//                    val tvClose: TextView = d.findViewById<View>(R.id.tvClose) as TextView
//
//
//                    btnView.setOnClickListener {
//
//                        val file = File(
//                            file_path
//                        )
//
//                        val photoURI = FileProvider.getUriForFile(context,
//                                context.applicationContext.packageName
//                                        .toString() + ".provider",
//                                file
//                        )
//                        val target = Intent(Intent.ACTION_VIEW)
//                        target.setDataAndType(
//                                photoURI,
//                                "application/pdf"
//                        )
//                        target.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
//                        target.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//
//
//                        val intent = Intent.createChooser(target, "Open File")
//                        try {
//                            context.startActivity(intent)
//                        } catch (e: ActivityNotFoundException) {
//                            // Instruct the user to install a PDF reader here, or something
//                        }
//                        d.dismiss()
//                    }
//                    btnShare.setOnClickListener {
//                        val intentShareFile = Intent(Intent.ACTION_SEND)
//                        val fileWithinMyDir: File = File(file_path)
//                        val photoURI = FileProvider.getUriForFile(context,
//                                context.applicationContext.packageName
//                                        .toString() + ".provider",
//                                fileWithinMyDir
//                        )
//
//                        intentShareFile.type = "application/pdf"
//                        intentShareFile.putExtra(
//                                Intent.EXTRA_STREAM,
//                                photoURI
//                        )
//                        intentShareFile.putExtra(
//                                Intent.EXTRA_SUBJECT,
//                                "Sharing File..."
//                        )
//                        intentShareFile.putExtra(Intent.EXTRA_TEXT, "Sharing File...")
//                        intentShareFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//
//                        val chooser = Intent.createChooser(intentShareFile, "Share File")
//
//                        val resInfoList: List<ResolveInfo> = context.packageManager.queryIntentActivities(
//                                chooser,
//                                PackageManager.MATCH_DEFAULT_ONLY
//                        )
//
//                        for (resolveInfo in resInfoList) {
//                            val packageName = resolveInfo.activityInfo.packageName
//                            context.grantUriPermission(
//                                    packageName,
//                                    photoURI,
//                                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
//                            )
//                        }
//
//
//                        context.startActivity(chooser)
//                    }
//
//                    tvClose.setOnClickListener {
//                        d.dismiss()
//                    }
//
//                    d.show()
//
//                    val window: Window? = d.window
//                    window?.setLayout(
//                            ViewGroup.LayoutParams.WRAP_CONTENT,
//                            ViewGroup.LayoutParams.WRAP_CONTENT
//                    )
//
//                }
//            }
//        }
//        val premise_id = premiseID
//        val date = selecetdMonth
//        val type = "staffdetails"
//
//        val downLoadURL = ApiInitialize.LOCAL_URL+"user/pdfgraph?premise_id=$premise_id&date=$date&staff_id=$id&type=$type"
//        Log.d("Download complete", "----------$downLoadURL")
//
//        DownloadFile().execute(
//                downLoadURL,
//                "maven.pdf"
//        )
//        Log.d("Download complete", "----------")
//    }
//
//    fun conVertDateTimeToTimeStarp(date: String): String {
//        val sdf = SimpleDateFormat("HH:mm:ss")
//        val date: Date = sdf.parse(date)
//        return date.time.toString() + "f"
//    }
//
//  @SuppressLint("SuspiciousIndentation")
//  private fun setLineChartData(fl: Float, weekdays: ArrayList<String>,
//                               row_line_chart: LineChart?, staffData: List<StaffDetailsGraphData.Data.StaffDetailsData.Staffdata>) {
//      row_line_chart!!.clear()
//      val zeroNumberList = ArrayList<Entry>()
//      val otherNumberList = ArrayList<Entry>()
//      zeroNumberList.clear()
//      otherNumberList.clear()
//
//      for (i in values.indices) {
//          zeroNumberList.add(Entry(i.toFloat(), this.values[i].y))
//      }
//
//      for (i in this.values.indices) {
//          otherNumberList.add(Entry(i.toFloat(), this.values[i].y))
//      }
//      val set1 = LineDataSet(zeroNumberList, "")
//      val set2 = LineDataSet(otherNumberList, "")
//      set1.setDrawIcons(false)
//      set2.setDrawIcons(false)
//      row_line_chart.setBackgroundColor(
//              ContextCompat.getColor(
//                      context,
//                      R.color.white
//              )
//      )
//
//
//      row_line_chart.description!!.isEnabled = false
//      row_line_chart.setTouchEnabled(true)
//
//
//      val xAxis = row_line_chart.xAxis
//      xAxis!!.gridColor = ContextCompat.getColor(context, R.color.black)
//      xAxis.gridLineWidth = 1f
//      xAxis.setCenterAxisLabels(false)
//      xAxis.granularity = 1f
//      row_line_chart.xAxis!!.position = XAxis.XAxisPosition.BOTTOM
//      row_line_chart.xAxis!!.setDrawGridLines(false)
//      row_line_chart.xAxis!!.axisLineWidth = 1f
//      xAxis.valueFormatter = IndexAxisValueFormatter(weekdays)
//      xAxis.setDrawAxisLine(true)
//      xAxis.setDrawLabels(true)
//      xAxis.mLabelWidth = 1
//
//      val yAxis = row_line_chart.axisLeft
//
//      yAxis.labelCount = otherNumberList.size
//      row_line_chart.axisLeft.isEnabled = true
//      row_line_chart.axisRight!!.isEnabled = false
//
//      row_line_chart.axisLeft!!.setDrawGridLines(true)
//      yAxis!!.setDrawLabels(true)
//      yAxis.setDrawAxisLine(false)
//      yAxis.granularity = 1f
//      row_line_chart.animateXY(500, 500)
//      row_line_chart.legend!!.isEnabled = false
//      yAxis.valueFormatter = HourYAxisValueFormatter()
//
//
//      // black lines and points
//      set1.color = ContextCompat.getColor(context, R.color.green)
//      set2.color = ContextCompat.getColor(context, R.color.green)
//      set1.setCircleColor(ContextCompat.getColor(context, R.color.green))
//      set2.setCircleColor(ContextCompat.getColor(context, R.color.green))
//      set1.lineWidth = 2.5f
//      set2.lineWidth = 2.5f
//      set1.circleRadius = 6.5f
//      set2.circleRadius = 6.5f
//      set1.circleHoleRadius = 4f
//      set2.circleHoleRadius = 4f
//      set1.setDrawValues(false)
//      set2.setDrawValues(false)
//
//      // draw points as solid circles
//      set1.setDrawCircleHole(true)
//
//
//      // customize legend entry
//      set1.formLineWidth = 1f
//      set2.formLineWidth = 1f
//      set1.formLineDashEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
//      set2.formLineDashEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
//      set1.formSize = 15f
//      set2.formSize = 15f
//
//      // text size of values
//      set1.valueTextSize = 9f
//      set2.valueTextSize = 9f
//
//      // draw selection line as dashed
//      // set the filled area
//
//      set2.setDrawFilled(false)
//      set1.fillFormatter =
//              IFillFormatter { _, _ -> row_line_chart.axisLeft.axisMinimum }
//      set2.fillFormatter =
//              IFillFormatter { _, _ -> row_line_chart.axisLeft.axisMinimum }
//      // set color of filled area
//      val dataSets = ArrayList<ILineDataSet>()
//      dataSets.add(set1)
//      dataSets.add(set2)
//      set1.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
//      set2.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
//
//      // create a data object with the data sets
//      val data = LineData(dataSets)
//      row_line_chart.data = data
//
//        val mv = CustomMarkerView(context, R.layout.tooltip)
//
//      row_line_chart.markerView = mv
//
//        row_line_chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
//            @RequiresApi(Build.VERSION_CODES.R)
//            override fun onValueSelected(e: Entry, h: Highlight?) {
//                row_line_chart.xAxis.valueFormatter.getFormattedValue(
//                        e.x,
//                        row_line_chart.xAxis
//                )
//                toolTipTime = ""
//                toolTipDate = ""
//
//                for (i in staffData.indices) {
//
//                    for (j in staffData.indices) {
//
//                        if (staffData[j].first_appearance_date_time.isNotEmpty()) {
//                            Log.e("stafffffffDatatatatat", conVertDateTimeToTimeStarp(convertDate(staffData[j].first_appearance_date_time, "yyyy-MM-dd HH:mm:ss", "HH:mm:ss")).toFloat().toString())
//
//                            if (conVertDateTimeToTimeStarp(convertDate(staffData[j].first_appearance_date_time, "yyyy-MM-dd HH:mm:ss", "HH:mm:ss")).toFloat() == e.y) {
//                                toolTipTime = convertDate(
//                                        staffData[j].first_appearance_date_time,
//                                        "yyyy-MM-dd HH:mm:ss", "hh:mm a")
//                            }
//                            if (row_line_chart.xAxis.valueFormatter.getFormattedValue(
//                                            e.x,
//                                            row_line_chart.xAxis
//                                    ) == convertDate(staffData[j].first_appearance_date_time, "yyyy-MM-dd HH:mm:ss", "dd/MM")) {
//                                toolTipDate = convertDate(staffData[j].first_appearance_date_time, "yyyy-MM-dd HH:mm:ss", "MMM,dd yyyy")
//                            }
//
//                        }
//                    }
//
//
//                }
//
//                val highlight = row_line_chart.data?.dataSets?.let { arrayOfNulls<Highlight>(it.size) }
//                for (j in 0 until (row_line_chart.data?.dataSets?.size ?: 0)) {
//                    val iDataSet: IDataSet<*> = row_line_chart.data!!.dataSets[j]
//                    for (i in (iDataSet as LineDataSet).values.indices) {
//                        if (iDataSet.values[i].x == e.x) {
//                            highlight?.set(j, Highlight(e.x, e.y, j)) }
//                    }
//                }
//                row_line_chart.highlightValues(highlight)
//                row_line_chart.marker
//
//
//            }
//
//            override fun onNothingSelected() {}
//        })
//
//
//
//    }
//
//    class InnerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        val ll_staff_list: LinearLayout = view.findViewById(R.id.ll_staff_list)
//        val llStaffDetails: LinearLayout = view.findViewById(R.id.llStaffDetails)
//        val imStaffImage: ImageView = view.findViewById(R.id.imStaffImage)
//        val tvStaffName: MyCustomTextView = view.findViewById(R.id.tvStaffName)
//        val ibDownLoad: ImageButton = view.findViewById(R.id.ibDownLoad)
//        val tvMonthYearSelect: TextView = view.findViewById(R.id.tvMonthYearSelect)
//        val lc_my_goal_activity_log_chart: LineChart = view.findViewById(R.id.lc_my_goal_activity_log_chart)
//        val staffdetailStatus: ImageView = view.findViewById(R.id.tv_staff_detailStatus)
//        fun bind(
//                position: Int,
//                treatmentName: ArrayList<StaffDetailsGraphData.Data.StaffDetailsData>
//        ) {
//        }
//    }
//
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        if (requestCode == REQUEST_WRITE_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            download(staf_id)
//        }
//    }
//}
