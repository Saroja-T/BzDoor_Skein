package com.busydoor.app.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.busydoor.app.R
import com.busydoor.app.customMethods.MyCustomTextView
import com.busydoor.app.interfaceD.HomeClick
import com.busydoor.app.model.PremiseResponse


class HomeListAdapter(
    private val context: Context,
    private val premiseDashbordList: ArrayList<PremiseResponse.Data>,
    private var homeClick: HomeClick
) : RecyclerView.Adapter<HomeListAdapter.InnerViewHolder>() {
    private val TAG = "HomeListAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InnerViewHolder {
        return InnerViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.dashboard_list_items, parent, false
            )
        ,context)
    }

    override fun getItemCount(): Int {
        return premiseDashbordList.size
    }


    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: InnerViewHolder, position: Int) {
        holder.bind(position, premiseDashbordList)
        val model: PremiseResponse.Data = premiseDashbordList[position]

        val circularProgressDrawable = CircularProgressDrawable(context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.backgroundColor= R.color.app_color
        circularProgressDrawable.start()
        Log.d(TAG, "onBindViewHolder: "+model.premiseStatus)

        if(model.photo !=null) {
            Log.e("adapterview",model.photo.toString())

            Glide.with(context)
                .load(model.photo)
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


        holder.premiseName.text = model.name
        holder.premiseCity.text = model.premiseStatus

        if (model.country.isNotEmpty()) {
            holder.premiseCity.text =model.city + ", " + model.state
        } else {
            holder.premiseCity.text = model.city
        }

        holder.dashbordView.setOnClickListener {
            homeClick.homePostionClick(position)
            Log.e("original value dash== ",model.premiseId.toString())

        }

        when (model.premiseStatus) {
            "in" -> {
                holder.premiseUserStatus.setImageResource(R.drawable.premiselist_staff_satus_in)
            }
            "inout" -> {
                holder.premiseUserStatus.setBackgroundResource(R.drawable.premiselist_staff_status_inout)
            }
            "out" -> {
                holder.premiseUserStatus.setBackgroundResource(R.drawable.premiselist_staff_status_out)
            }
            "offline" -> {
                holder.premiseUserStatus.setBackgroundResource(R.drawable.premiselist_staff_status_offline)
            }
            else -> {
    //            holder.premiseUserStatus.setBackgroundResource(R.color.colorGreyLight)
            }
        }
    }

    class InnerViewHolder(view: View,context: Context) : RecyclerView.ViewHolder(view) {
        val premiseImage:ImageView = view.findViewById(R.id.PremiseImage)
        val premiseName: MyCustomTextView = view.findViewById(R.id.premiseName)
        val premiseCity: MyCustomTextView = view.findViewById(R.id.premiseCity)
        val premiseUserStatus: ImageView = view.findViewById(R.id.homePremiseUserStatus)
        val dashbordView: LinearLayout = view.findViewById(R.id.dashbordView)


        fun bind(
            position: Int,
            treatmentName: ArrayList<PremiseResponse.Data>
        ) {

        }
    }
}

