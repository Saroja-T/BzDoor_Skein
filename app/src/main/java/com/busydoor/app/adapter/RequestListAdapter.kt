package com.busydoor.app.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.busydoor.app.R
import com.busydoor.app.customMethods.MyCustomTextView
import com.busydoor.app.customMethods.convertDate
import com.busydoor.app.interfaceD.HomeClick
import com.busydoor.app.model.PremiseResponse
import com.busydoor.app.model.UserActivities
import de.hdodenhof.circleimageview.CircleImageView


class RequestsAdapter(
    private val context: Context,
    private val requestActivityList: ArrayList<UserActivities.Data.Offsitedetails>,
    private var homeClick: HomeClick,
) : RecyclerView.Adapter<RequestsAdapter.InnerViewHolder>() {
    private val TAG = "HomeListAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InnerViewHolder {
        return InnerViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_requests_layout, parent, false
            )
            ,context)
    }

    override fun getItemCount(): Int {
        return requestActivityList.size
    }


    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: InnerViewHolder, position: Int) {
        holder.bind(position, requestActivityList)
        val model: UserActivities.Data.Offsitedetails = requestActivityList[position]

        val circularProgressDrawable = CircularProgressDrawable(context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.backgroundColor= R.color.app_color
        circularProgressDrawable.start()
        Log.d(TAG, "onBindViewHolder: "+model.toString())

        if(model.requesterPhoto !=null) {
            Log.e("adapterview",model.toString())
            Glide.with(context)
                .load(model.requesterPhoto)
                .placeholder(circularProgressDrawable)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(holder.imageUSer)
        }else{
            Glide.with(context)
                .load(R.drawable.icon_users)
                .placeholder(circularProgressDrawable)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(holder.imageUSer)
        }
        if(model.approverPhoto !=null) {
            Log.e("adapterview",model.toString())
            Glide.with(context)
                .load(model.approverPhoto)
                .placeholder(circularProgressDrawable)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(holder.imageManager)
        }else{
            Glide.with(context)
                .load(R.drawable.icon_users)
                .placeholder(circularProgressDrawable)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(holder.imageManager)
        }

        if(model.requesterPhoto !=null) {
            Log.e("adapterview",model.toString())
            Glide.with(context)
                .load(model.requesterPhoto)
                .placeholder(circularProgressDrawable)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(holder.premiseImage)
        }else{
            Glide.with(context)
                .load(R.drawable.icon_users)
                .placeholder(circularProgressDrawable)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(holder.premiseImage)
        }

        holder.requestPosition.setOnClickListener {
            homeClick.homePostionClick(position)
            Log.e("original value dash== ",model.toString())

        }
        holder.hideShowView.setOnClickListener {
        holder.requestbyUserView.visibility =View.GONE
        holder.approvalCmdView.visibility =View.GONE
        holder.approvalStatusView.visibility =View.GONE
        }


        if(model.commentsByRequester!=null){holder.userCmd.text=model.commentsByRequester
        }else{holder.requestbyUserView.visibility= View.GONE}
        if(model.commentsByApprover!=null) {holder.approverCmd.text=model.commentsByApprover}
        else{holder.approvalCmdView.visibility= View.GONE}
        if(model.timePermissionStatus!=null){
            holder.approvalSatus.text=model.timePermissionStatus
            holder.accept_rejectView.visibility= View.GONE
        }else if(model.timePermissionStatus =="rejected"){
            holder.accept_rejectView.visibility= View.GONE
        }else if(model.timePermissionStatus =="approved"){
            holder.accept_rejectView.visibility= View.GONE
        }else if(model.timePermissionStatus =="pending"){
            holder.accept_rejectView.visibility= View.GONE
        }else{
            holder.approvalStatusView.visibility= View.GONE
        }
        holder.requestTime.text=model.timePermissionReason
        holder.approvalTime.text= convertDate(model.approvedTime,"HH:MM:SS","hh:mm a")
    }

    class InnerViewHolder(view: View,context: Context) : RecyclerView.ViewHolder(view) {
        val premiseImage: CircleImageView = view.findViewById(R.id.cIvImage)
        val hideShowView: ImageView = view.findViewById(R.id.hideShowView)
        val imageUSer: CircleImageView = view.findViewById(R.id.ImageUSer)
        val imageManager: CircleImageView = view.findViewById(R.id.ImageManager)
        val requestTime: MyCustomTextView = view.findViewById(R.id.tvMessage)
        val approvalTime: MyCustomTextView = view.findViewById(R.id.approvalTime)
        val userCmd: MyCustomTextView = view.findViewById(R.id.userCmd)
        val approverCmd: MyCustomTextView = view.findViewById(R.id.approvalCmd)
        val approvalSatus: MyCustomTextView = view.findViewById(R.id.approvalStatus)
        val requestPosition: LinearLayout = view.findViewById(R.id.requestNotification)
        val requestbyUserView: LinearLayout = view.findViewById(R.id.userCmdView)
        val approvalCmdView: LinearLayout = view.findViewById(R.id.approvalCmdView)
        val approvalStatusView: LinearLayout = view.findViewById(R.id.approvalStatusView)
        val accept_rejectView: LinearLayout = view.findViewById(R.id.accept_rejectView)


        fun bind(
            position: Int,
            treatmentName: ArrayList<UserActivities.Data.Offsitedetails>
        ) {

        }
    }
}

