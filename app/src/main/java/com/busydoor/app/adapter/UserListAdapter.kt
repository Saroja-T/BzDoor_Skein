package com.busydoor.app.adapter
import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.busydoor.app.R
import com.busydoor.app.customMethods.MyCustomTextView
import com.busydoor.app.interfaceD.HomeClick
import com.busydoor.app.model.PremiseUserList
import de.hdodenhof.circleimageview.CircleImageView
import pl.droidsonroids.gif.GifImageView


class UserListAdapter(
    val context: Context,
    private val UsersList: ArrayList<PremiseUserList.Data.Staffdetails>,
    private val userId:String,
    private var homeClick: HomeClick
//    var listener: NotificationsFragment.OnItemClickListener
):RecyclerView.Adapter<UserListAdapter.InnerViewHolder>() {



    class InnerViewHolder(View: View):RecyclerView.ViewHolder(View) {

        // Add a variable to track if the switch was clicked programmatically
        var isSwitchClickedProgrammatically = false
        val popupMenu = PopupMenu(View.context, View)

        val userImage: CircleImageView? = View.findViewById(R.id.PremiseStaffImages)
        val userStatus: GifImageView? = View.findViewById(R.id.imageViewCameraIcon)
        val userActive: ImageView? = View.findViewById(R.id.user_Active)
        val userName: MyCustomTextView? = View.findViewById(R.id.tv_staff_name)
        val cardView: CardView? = View.findViewById(R.id.userCardView)


        fun bind(
            position: Int,
            treatmentName: ArrayList<PremiseUserList.Data.Staffdetails>
        ) {

        }
        init {

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): InnerViewHolder {
        return InnerViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.staff_list_item, parent, false
            )
        )
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onBindViewHolder(holder: InnerViewHolder, position: Int) {

        holder.bind(position, UsersList)
        val model: PremiseUserList.Data.Staffdetails = UsersList[position]
        // Set the switch state without triggering the listener
//        holder.sw_status?.setOnCheckedChangeListener(null)

        val circularProgressDrawable = CircularProgressDrawable(context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.backgroundColor= R.color.app_color
        circularProgressDrawable.start()

        if(userId != model.userId.toString()){holder.userActive!!.visibility= View.VISIBLE}else{
            holder.userActive!!.visibility= View.GONE
        }
        // Set click listener for the userActive ImageView/Button
        // Set click listener for the userActive ImageView/Button in onBindViewHolder
        holder.userActive?.setOnClickListener { view ->
            if(userId != model.userId.toString()){
            showActivePopupMenu(view,homeClick,model.userActiveStatus!!,position)}
        }

        if(model.photo !=null) {
            Log.e("adapterview",model.photo.toString())
            Glide.with(context)
                .load(model.photo)
                .placeholder(circularProgressDrawable)
                .into(holder.userImage!!)
        }else{
            Glide.with(context)
                .load(model.photo)
                .placeholder(circularProgressDrawable)
                .into(holder.userImage!!)
        }

        holder.userName!!.text= model.firstName+" "+model.lastName
//        Log.e("adapterview",model.status.toString()).toString()
        when (model.userStatus) {
            "in" -> {
                holder.userStatus!!.setBackgroundResource(R.drawable.icon_staff_profile_in)
            }
            "out" -> {
                holder.userStatus!!.setBackgroundResource(R.drawable.icon_profile_status_out)
            }
            "inout" -> {
                holder.userStatus!!.setBackgroundResource(R.drawable.icon_profile_status_inout)
            }
            "offline" -> {
                holder.userStatus!!.setBackgroundResource(R.drawable.icon_profile_status_offline)
            }
            else -> holder.userStatus!!.setBackgroundResource(R.drawable.icon_profile_status_offline)


        }

    }
    private fun showActivePopupMenu(view: View,homeClick: HomeClick,activeStatus:String,position: Int) {
        val popupMenu = PopupMenu(view.context, view)
        if(activeStatus=="true"){
            popupMenu.menu.add("Inactive")

        }else if(activeStatus=="false"){
            popupMenu.menu.add("Active")
        }
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.title) {
                "Active" -> {
                    homeClick.homePostionClick(position)
                    Toast.makeText(view.context, "User is Active ${activeStatus}", Toast.LENGTH_SHORT).show()
                    true
                }
                "Inactive" -> {
                    homeClick.homePostionClick(position)
                    Toast.makeText(view.context, "User is InActive $activeStatus", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }
    override fun getItemCount(): Int {
        return  UsersList.size

    }

}