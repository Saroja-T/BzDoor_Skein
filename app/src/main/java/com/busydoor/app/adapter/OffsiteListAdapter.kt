package com.busydoor.app.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.busydoor.app.R
import com.busydoor.app.customMethods.convertDate
import com.busydoor.app.interfaceD.HomeClick
import com.busydoor.app.model.RequestAllOffsiteResponse
import java.util.Locale


class OffsiteListAdapter(
    private val context: Context,
    private val offsiteList: ArrayList<RequestAllOffsiteResponse.Data>,
    private var homeClick: HomeClick
) : RecyclerView.Adapter<OffsiteListAdapter.InnerViewHolder>() {
    private val TAG = "HomeListAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InnerViewHolder {
        return InnerViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.activity_all_requests_offsite_items, parent, false
            )
        ,context)
    }

    override fun getItemCount(): Int {
        return offsiteList.size
    }


    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: InnerViewHolder, position: Int) {
        holder.bind(position, offsiteList)
        val model: RequestAllOffsiteResponse.Data = offsiteList[position]

        Log.d(TAG, "aqw: "+model.toString())

        if(model.startTime !=null){
            holder.offsiteTotalTime.text ="Requested on "+convertDate(model.requestedDate,"yyyy-MM-dd","MMM dd','yyyy")+"|"+convertDate(model.startTime,"HH:mm:ss","hh:mm:a")
        }
        if(model.endTime !=null){
            holder.offsiteStartEndTime.text= convertDate(model.startTime,"HH:mm:ss","hh:mm:a")+" to " + convertDate(model.endTime,"HH:mm:ss","hh:mm:a")
        }

        if(model.commentsByRequester !=null){
            holder.comments.text =model.commentsByRequester;
        }

        if(model.timePermissionReason !=null){
            if(model.timePermissionReason =="personal"){
                holder.requestType.text = "Request "+model.timePermissionReason+" time"
            }else if(model.timePermissionReason =="offsite"){
                holder.requestType.text ="Request "+model.timePermissionReason+" time"
            }

        }

        holder.viewReasonText.setOnClickListener {
            homeClick.homePostionClick(position)
            Log.e("original value dash== ",model.toString())
        }

        when (model.timePermissionStatus) {
            "approved" -> {
                holder.noViewReason.visibility=View.GONE
                holder.viewReason.visibility=View.VISIBLE
                holder.viewReasonUI.visibility=View.VISIBLE
                if(model.commentsByApprover !=null && model.commentsByApprover !=""){
                    holder.viewReasonText.visibility= View.VISIBLE
                }
                holder.approvedByDateTime.text= model.approverFirstName+" has accepted on "+convertDate(model.approvedDate,"yyyy-MM-dd","MMM',' dd")+"|"+convertDate(model.approvedTime,"HH:mm:ss","hh:mm:a")
                holder.approvalStatus.setTextColor(ContextCompat.getColor(context, R.color.approved))
                holder.approvalStatus.text= model.timePermissionStatus.toString().toUpperCase(Locale.ROOT)
            }
            "rejected" -> {
                holder.noViewReason.visibility=View.GONE
                holder.viewReason.visibility=View.VISIBLE
                holder.viewReasonUI.visibility=View.VISIBLE
                if(model.commentsByApprover !=null && model.commentsByApprover !=""){
                    holder.viewReasonText.visibility= View.VISIBLE
                }
                if(model.approverLastName.toString() ==null||model.approverFirstName.toString() ==""){
                    holder.approvedByDateTime.text= "Your has been rejected automatically "+convertDate(model.approvedDate,"yyyy-MM-dd","MMM',' dd")+"|"+convertDate(model.approvedTime,"HH:mm:ss","hh:mm:a")
                }else{
                    holder.approvedByDateTime.text= model.approverFirstName+" has rejected on "+convertDate(model.approvedDate,"yyyy-MM-dd","MMM',' dd")+"|"+convertDate(model.approvedTime,"HH:mm:ss","hh:mm:a")
                }
                holder.approvalStatus.setTextColor(ContextCompat.getColor(context, R.color.reject))
                holder.approvalStatus.text=model.timePermissionStatus.toString().toUpperCase(Locale.ROOT)
            }
            else -> {
                holder.viewReason.visibility=View.GONE
                holder.viewReasonUI.visibility=View.GONE
                holder.noViewReason.visibility=View.VISIBLE
                holder.approvalStatus.setTextColor(ContextCompat.getColor(context, R.color.pending))
                holder.approvalStatus.text="PENDING"
            }
        }
    }

    class InnerViewHolder(view: View,context: Context) : RecyclerView.ViewHolder(view) {
        var offsiteTotalTime:TextView = view.findViewById(R.id.offsiteTotalTime)
        val offsiteStartEndTime: TextView = view.findViewById(R.id.requestedOffsiteTime)
        val comments: TextView = view.findViewById(R.id.Off_time_comments)
        val approvalStatus: TextView = view.findViewById(R.id.approvalStatus)
        val requestType: TextView = view.findViewById(R.id.requestType)
        val viewReasonText: TextView = view.findViewById(R.id.viewAppovedCommand)
        val viewReason:LinearLayout = view.findViewById(R.id.et_ReasonView)
        val viewReasonUI:LinearLayout = view.findViewById(R.id.reason_ui_view)
        val approvedByDateTime:TextView = view.findViewById(R.id.approvedByDateTime)
        val noViewReason:LinearLayout = view.findViewById(R.id.noViewReason)


        fun bind(
            position: Int,
            treatmentName: ArrayList<RequestAllOffsiteResponse.Data>
        ) {

        }
    }
}

