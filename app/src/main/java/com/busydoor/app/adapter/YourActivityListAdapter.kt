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
import com.busydoor.app.customMethods.convertDate
import com.busydoor.app.model.UserActivities

class YourActivityListAdapter(
    private val context: Context,
     private val activityList: ArrayList<UserActivities.Data.Activitiesdetails>,
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
        val model: UserActivities.Data.Activitiesdetails = activityList[position]

        val circularProgressDrawable = CircularProgressDrawable(context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.backgroundColor= R.color.app_color
        circularProgressDrawable.start()
        Log.d(TAG, "onBindViewHolder: $model")

        if(model.image !=null) {
            Log.e("adapterview",model.image.toString())
            Glide.with(context)
                .load(model.image)
                .placeholder(circularProgressDrawable)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(holder.cIvImage)
        }else{
            Glide.with(context)
                .load(R.drawable.icon_users)
                .placeholder(circularProgressDrawable)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(holder.cIvImage)
        }

        holder.tvMessage.text=model.message.toString()
        holder.tvTime.text= convertDate(model.datetime.toString(),"yyyy-MM-dd HH:mm:ss","hh:mm a")
    }

    class InnerViewHolder(view: View,context: Context) : RecyclerView.ViewHolder(view) {
        val cIvImage:ImageView = view.findViewById(R.id.cIvImage)
        val tvMessage: MyCustomTextView = view.findViewById(R.id.tvMessage)
        val tvTime: MyCustomTextView = view.findViewById(R.id.tvTime)


        fun bind(
            position: Int,
             treatmentName: ArrayList<UserActivities.Data.Activitiesdetails>
        ) {

        }
    }
}

