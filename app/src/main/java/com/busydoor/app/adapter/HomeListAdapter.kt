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
        circularProgressDrawable.backgroundColor= R.color.app_color
        circularProgressDrawable.start()
        Log.d(TAG, "onBindViewHolder: "+model.premiseStatus)

        if(model.photo !=null) {
            Log.e("adapterview",model.photo.toString())

            Glide.with(context)
                .load(model.photo)
                .timeout(1000)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(holder.premiseImage)
        }else{
            Glide.with(context)
                .load(R.drawable.icon_users)
                .timeout(1000)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
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
                holder.llImageView.visibility = View.GONE
                holder.llGifView.visibility = View.VISIBLE
                holder.premiseUserStatus.setImageResource(R.drawable.premiselist_staff_satus_in)
            }
            "inout" -> {
                setIconLinearLayoutVisible(holder.llGifView,holder.llImageView,holder.homePremiseUserStatusImg,R.drawable.premiselist_staff_status_inout)
            }
            "out" -> {
                setIconLinearLayoutVisible(
                    holder.llGifView,
                    holder.llImageView,
                    holder.homePremiseUserStatusImg,
                    R.drawable.premiselist_staff_status_out
                )
//                holder.homePremiseUserStatusImg.setBackgroundResource(R.drawable.premiselist_staff_status_out)
            }
            "offline" -> {
                setIconLinearLayoutVisible(
                    holder.llGifView,
                    holder.llImageView,
                    holder.homePremiseUserStatusImg,
                    R.drawable.premiselist_staff_status_offline
                )
//                holder.premiseUserStatus.setBackgroundResource(R.drawable.premiselist_staff_status_offline)
            }
            else -> {
                setIconLinearLayoutVisible(
                    holder.llGifView,
                    holder.llImageView,
                    holder.homePremiseUserStatusImg,
                    R.drawable.premiselist_staff_status_offline
                )
//                holder.premiseUserStatus.setBackgroundResource(R.drawable.premiselist_staff_status_offline)
            }
        }
    }

    private fun setIconLinearLayoutVisible(
        llGifView: LinearLayout,
        llImageView: LinearLayout,
        homePremiseUserStatusImg: ImageView,
        premiselistStaffStatusInout: Int
    ) {
        llImageView.visibility = View.VISIBLE
        llGifView.visibility = View.GONE
        homePremiseUserStatusImg.setBackgroundResource(premiselistStaffStatusInout)

    }

    class InnerViewHolder(view: View,context: Context) : RecyclerView.ViewHolder(view) {
        val premiseImage:ImageView = view.findViewById(R.id.PremiseImage)
        val premiseName: MyCustomTextView = view.findViewById(R.id.premiseName)
        val premiseCity: MyCustomTextView = view.findViewById(R.id.premiseCity)
        val premiseUserStatus: ImageView = view.findViewById(R.id.homePremiseUserStatus)
        val homePremiseUserStatusImg: ImageView = view.findViewById(R.id.homePremiseUserStatusImg)
        val dashbordView: LinearLayout = view.findViewById(R.id.dashbordView)
        val llImageView: LinearLayout = view.findViewById(R.id.llImageView)
        val llGifView: LinearLayout = view.findViewById(R.id.llGifView)

        fun bind(
            position: Int,
            treatmentName: ArrayList<PremiseResponse.Data>
        ) {

        }
    }
}

