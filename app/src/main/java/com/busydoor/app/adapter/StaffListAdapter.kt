package com.busydoor.app.adapter

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
import com.busydoor.app.R
import com.busydoor.app.customMethods.MyCustomTextView
import com.busydoor.app.customMethods.NameConvertion
import com.busydoor.app.interfaceD.HomeClick
import com.busydoor.app.model.StaffListOnDate
import pl.droidsonroids.gif.GifImageView


class StaffListAdapter(
    private val context: Context,
    private val staffList: ArrayList<StaffListOnDate.Data>,
    private var StaffClick: HomeClick,
    private var isCurrentDateSelected: Boolean,
) : RecyclerView.Adapter<StaffListAdapter.InnerViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InnerViewHolder {
        return InnerViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.staff_list_item, parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return staffList.size
    }


    override fun onBindViewHolder(holder: InnerViewHolder, position: Int) {
        holder.bind(position, staffList)
        val model: StaffListOnDate.Data = staffList[position]

        val circularProgressDrawable = CircularProgressDrawable(context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.backgroundColor= R.color.app_color
        circularProgressDrawable.start()

        if(model.photo !=null) {
            Log.e("adapterview",model.photo.toString())
            Glide.with(context)
                .load(model.photo)
                .timeout(1000)
                .placeholder(circularProgressDrawable)
                .into(holder.imStaffImage!!)
        }else{
            Glide.with(context)
                .load(model.photo)
                .placeholder(circularProgressDrawable)
                .timeout(1000)
                .into(holder.imStaffImage!!)
        }
        val fullname=model.firstName+" "+model.lastName
        holder.tv_staff_name!!.text= NameConvertion().truncateText(fullname)


        holder.ll_staff_list.setOnClickListener{
            StaffClick.homePostionClick(position)
        }


        Log.e("adapterview",isCurrentDateSelected.toString())

        if(!isCurrentDateSelected){
            holder.staffStatus.visibility = View.GONE
        }else {
            when (model.status) {
                "in" -> {
                    holder.staffStatus!!.setBackgroundResource(R.drawable.icon_staff_profile_in)
                }

                "out" -> {
                    holder.staffStatus!!.setBackgroundResource(R.drawable.icon_profile_status_out)
                }

                "inout" -> {
                    holder.staffStatus!!.setBackgroundResource(R.drawable.icon_profile_status_inout)
                }

                "offline" -> {
                    holder.staffStatus!!.setBackgroundResource(R.drawable.icon_profile_status_offline)
                }

                else -> holder.staffStatus!!.setBackgroundResource(R.drawable.icon_profile_status_offline)

            }
        }


    }

    class InnerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tv_staff_name: MyCustomTextView = view.findViewById(R.id.tv_staff_name)
        val ll_staff_list: LinearLayout = view.findViewById(R.id.ll_staff_list)
        val imStaffImage: ImageView = view.findViewById(R.id.PremiseStaffImages)
        val staffStatus: GifImageView = view.findViewById(R.id.imageViewCameraIcon)
        fun bind(
            position: Int,
            treatmentName: ArrayList<StaffListOnDate.Data>
        ) {
        }
    }
}
