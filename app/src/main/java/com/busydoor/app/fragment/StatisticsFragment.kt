
package com.busydoor.app.fragment

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.busydoor.app.customMethods.convertDate
import com.busydoor.app.databinding.FragmentStatisticsBinding
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.LargeValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener


class StatisticsFragment : Fragment(), OnChartValueSelectedListener {
    private lateinit var binding: FragmentStatisticsBinding
    val values1 = ArrayList<BarEntry>()
    val values2 = ArrayList<BarEntry>()
    val values3 = ArrayList<BarEntry>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        // Inflate the layout for this fragment


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
        setGraphData()
        return root
    }

    // private fun setGraphData(dataModel: ArrayList<StatusGraphData.Data>) {
    private fun setGraphData() {

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
        values1.add(BarEntry(7f, 8.0f))
        values1.add(BarEntry(7f, 10.0f))
        values2.add(BarEntry(7f, 7.0f))
        values2.add(BarEntry(7f, 8.0f))
        values3.add(BarEntry(7f, 9.0f))
        values3.add(BarEntry(7f, 6.0f))

        xAxisLabelList.add(
            convertDate("2023-12-25", "yyyy-MM-dd", "dd/MM"))
        xAxisLabelList.add(
            convertDate("2023-12-26", "yyyy-MM-dd", "dd/MM"))
//        for (i in dataModel) {
//            if (i.Date!!.isNotEmpty() && i.Date!=null) {
//
//                if(i.OpenMins!!.toInt() % 60 == 0){
//                    Log.e("ifcondition","qwer"+ i.OpenMins!!.toFloat())
//                    values1.add(BarEntry(7f, (i.OpenMins!!.toInt()/60).toFloat()))
//                }
//                else{
//                    if(i.OpenMins!!.toInt() > 60){
//                        Log.e("ifcondition","qwer"+ i.OpenMins!!.toFloat())
//
//                        val hr1 =((i.OpenMins!!.toInt())/60).toString()
//
//                        (i.OpenMins!!.toInt() % 60).toString()
//
//                        values1.add(BarEntry(7f, hr1.toFloat()))
//
//
//                    }
//                    else{
//                        values1.add(BarEntry(7f,(i.OpenMins!!.toFloat()/100)))
//                        Log.e("xtra","  "+ i.OpenMins!!.toFloat()/10000  + values1 )
//                    }
//
//                }
//
//                if( i.ClosedMins!!.toInt() % 60 == 0 ){
//                    Log.e("ifconditionclosed","qwe"+ i.ClosedMins!!.toFloat())
//                    values2.add(BarEntry(7f, (i.ClosedMins!!.toInt()/60).toFloat()))
//                }
//                else{
//                    if( i.ClosedMins!!.toInt() >60){
//                        Log.e("ifconditionclosed","qwe"+ i.ClosedMins!!.toFloat())
//
//                        val hr3 =((i.ClosedMins!!.toInt())/60).toString()
//
//                        (i.ClosedMins!!.toInt()% 60).toString()
//
//                        values2.add(BarEntry(7f, hr3.toFloat()))
//                    }
//                    else{
//                        values2.add(BarEntry(7f,i.ClosedMins!!.toFloat()/100))
//                    }
//                }
//                if(i.OfflineMins!!.toInt() % 60 == 0 ){
//                    Log.e("ifconditionoffline",""+ i.OfflineMins!!.toFloat())
//
//                    values3.add(BarEntry(7f, (i.OfflineMins!!.toInt()/60).toFloat()))
//
//                }
//                else{
//                    if(i.OfflineMins!!.toInt() > 60){
//                        Log.e("ifconditionoffline",""+ i.OfflineMins!!.toFloat())
//
//
//                        val hr2 =((i.OfflineMins!!.toInt())/60).toString()
//
//
//                        (i.OfflineMins!!.toInt() % 60).toString()
//
//
//                        values3.add(BarEntry(7f, hr2.toFloat()))
//                    }
//                    else {
//                        values3.add(BarEntry(7f,i.OfflineMins!!.toFloat()/100))
//
//
//                    }
//
//                }
//
//                xAxisLabelList.add(
//                    convertDate(i.Date!!.toString(), "yyyy-MM-dd", "dd/MM"))
//
//            }
//        }


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
        // customMarkerView = CustomBarMarkerView(this@PremiseStatusActivity, R.layout.tooltip, dataModel)
        binding.chart.groupBars(0f, groupSpace, barSpace)
        binding.chart.invalidate()
        binding.chart.fitScreen()
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        Log.i("Activity", "Selected: " + e.toString() + ", dataSet: " + h!!.dataSetIndex)
//        customMarkerView.refreshContent(e,h)
//        binding.chart.marker = customMarkerView
    }

    override fun onNothingSelected() {

    }

}
//class CustomBarMarkerView(context: Context?, layoutResource: Int, private val dataModel: ArrayList<StatusGraphData.Data>) : MarkerView(
//    context,
//    layoutResource
//) {
//    private val tvContent: TextView = findViewById<View>(R.id.data1) as TextView
//    private val tvContent1: TextView = findViewById<View>(R.id.data2) as TextView
//
//    // callbacks everytime the MarkerView is redrawn, can be used to update the
//    // content (user-interface)
//    override fun refreshContent(e: Entry, highlight: Highlight) {
//        super.refreshContent(e, highlight)
//        if (e is CandleEntry) {
//            Log.e("e.x",e.x.toString())
//            tvContent1.text = binding.chart.xAxis.valueFormatter
//                .getFormattedValue(
//                    e.x
//                )
//            tvContent.text = Utils.formatNumber(e.y, 0, true)
//        } else {
//
//            val string_temp = e.x.toString()
//            val string_form = string_temp.substring(0, string_temp.indexOf('.'))
//            val t = Integer.valueOf(string_form)
//            Log.e("e.x1",e.y.toString())
//            tvContent1.text =
//
//
//                convertDate(dataModel[t].Date,
//                    "yyyy-MM-dd",
//                    "dd/MM")
//
//            if(highlight.dataSetIndex===0){
//                if(values1[t].y===e.y){
//                    tvContent.text = dataModel[t].OpenHours
//                }
//            }else if(highlight.dataSetIndex===1){
//                if(values2[t].y===e.y){
//                    tvContent.text = dataModel[t].ClosedHours
//                }
//
//            }else if(highlight.dataSetIndex===2) {
//                if(values3[t].y===e.y){
//                    tvContent.text = dataModel[t].OfflineHours
//                }
//            }
//        }
//
//    }
//
//
//    override fun getOffset(): MPPointF {
//        return MPPointF((-(width / 2)).toFloat(), (-height).toFloat()) // place the midpoint of marker over the bar
//    }
//
//}


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