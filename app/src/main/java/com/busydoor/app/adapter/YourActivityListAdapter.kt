package com.busydoor.app.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.busydoor.app.R
import com.busydoor.app.customMethods.MyCustomTextView
import com.busydoor.app.model.UserActivities

class YourActivityListAdapter(
    private val context: Context,
     private val activityList: ArrayList<UserActivities.Data>,
) : RecyclerView.Adapter<YourActivityListAdapter.InnerViewHolder>() {
    private val TAG = "HomeListAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InnerViewHolder {
        return InnerViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_your_activities_layout, parent, false
            ),context)
    }

    override fun getItemCount(): Int {
        return activityList.size
    }


    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: InnerViewHolder, position: Int) {
        holder.bind(position, activityList)
        val model: UserActivities.Data = activityList[position]

        val circularProgressDrawable = CircularProgressDrawable(context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.backgroundColor= R.color.app_color
        circularProgressDrawable.start()
        Log.d(TAG, "onBindViewHolder: "+model.activitiesdetails)
//
//        if(model.activitiesdetails !=null) {
//            Log.e("adapterview",model.photo.toString())
//
//            Glide.with(context)
//                .load(model.photo)
//                .placeholder(circularProgressDrawable)
//                .diskCacheStrategy(DiskCacheStrategy.NONE)
//                .skipMemoryCache(true)
//                .into(holder.cIvImage)
//        }else{
//            Glide.with(context)
//                .load(R.drawable.icon_users)
//                .placeholder(circularProgressDrawable)
//                .diskCacheStrategy(DiskCacheStrategy.NONE)
//                .skipMemoryCache(true)
//                .into(holder.cIvImage)
//        }
//
//
//        holder.tvMessage.text = model.name
//        holder.tvTime.text = model.premiseStatus
//
//        if (model.country.isNotEmpty()) {
//            holder.tvTime.text =model.city + ", " + model.state
//        } else {
//            holder.tvTime.text = model.city
//        }
//
//
//
//        when (model.premiseStatus) {
//            "in" -> {
////                holder.premiseUserStatus.setImageResource(R.drawable.premiselist_staff_satus_in)
//            }
//            "inout" -> {
////                holder.premiseUserStatus.setBackgroundResource(R.drawable.premiselist_staff_status_inout)
//            }
//            "out" -> {
////                holder.premiseUserStatus.setBackgroundResource(R.drawable.premiselist_staff_status_out)
//            }
//            "offline" -> {
////                holder.premiseUserStatus.setBackgroundResource(R.drawable.premiselist_staff_status_offline)
//            }
//            else -> {
//    //            holder.premiseUserStatus.setBackgroundResource(R.color.colorGreyLight)
//            }
//        }
    }

    class InnerViewHolder(view: View,context: Context) : RecyclerView.ViewHolder(view) {
        val cIvImage:ImageView = view.findViewById(R.id.cIvImage)
        val tvMessage: MyCustomTextView = view.findViewById(R.id.tvMessage)
        val tvTime: MyCustomTextView = view.findViewById(R.id.tvTime)


        fun bind(
            position: Int,
             treatmentName: ArrayList<UserActivities.Data>
        ) {

        }
    }
}

