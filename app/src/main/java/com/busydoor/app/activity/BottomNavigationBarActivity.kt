package com.busydoor.app.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.busydoor.app.R
import com.busydoor.app.customMethods.NameConvertion
import com.busydoor.app.customMethods.PermissionUtils
import com.busydoor.app.customMethods.PrefUtils
import com.busydoor.app.databinding.ActivityBottomNavBarBinding
import com.busydoor.app.model.UserModel
import com.busydoor.app.viewmodel.OTPViewModel
import com.busydoor.app.viewmodel.ProfileViewModel
import com.google.gson.Gson


class BottomNavigationBarActivity : ActivityBase() {
    private lateinit var navController: NavController
    private lateinit var binding: ActivityBottomNavBarBinding
    private var isAdmin:String = ""
    override lateinit var objSharedPref: PrefUtils
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var otpViewModel : OTPViewModel
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBottomNavBarBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupUI(binding.root)
        objSharedPref = PrefUtils(applicationContext)
        isAdmin = getUserModel()?.data?.isAdmin.toString()
        navController = findNavController(R.id.main_fragment)
        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        otpViewModel = ViewModelProvider(this).get(OTPViewModel::class.java)
        if(isAdmin=="1"){
            binding.bottomBarStaff.visibility = View.GONE
            binding.bottomBar.visibility = View.VISIBLE
            setupSmoothBottomMenu()
        }else{
            binding.bottomBarStaff.visibility = View.VISIBLE
            binding.bottomBar.visibility = View.GONE
            setupSmoothBottomMenuStaff()
        }
       // setupActionBarWithNavController(navController)

        profileViewModel.profileData.observe(this) { data ->
            // Handle changes to the shared data in BottomBarFragment
            // The 'data' variable contains the updated value
            // Handle the updated profile data
            if (data != null) {
                // Access individual values like profileData["profileImage"], profileData["firstName"], etc.
                setValueToProfile(data)
            }
        }
        binding.userProfileView.backPage.setOnClickListener{
            finish()
        }
        binding.userProfileView.editProfile.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }
        binding.bottomBar.onItemSelected = {
            Log.e("onItemSelected", "Item $it selected")
        }
        binding.bottomBar.onItemReselected = {
            Log.e("onItemReselected", "Item $it selected")
        }

        setupSmoothBottomMenu()
    }
    private fun setValueToProfile(profileData: Map<String, String>?) {

        val circularProgressDrawable = CircularProgressDrawable(this)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()

        if(binding.userProfileView.PremiseStaffImage !=null) {
            Glide.with(this)
                .load(profileData?.get("profileImage"))
                .placeholder(circularProgressDrawable)
                .timeout(1000)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(binding.userProfileView.PremiseStaffImage)
        }else{
            Glide.with(this)
                .load(R.drawable.icon_users)
                .placeholder(circularProgressDrawable)
                .timeout(1000)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(binding.userProfileView.PremiseStaffImage)
        }

        when (profileData?.get("userStatus")) {
            "in" -> {
                binding.userProfileView.staffStatus.setImageResource(R.drawable.icon_staff_profile_in)
            }
            "inout" -> {
                binding.userProfileView.staffStatus.setImageResource(R.drawable.icon_profile_status_inout)
            }
            "out" -> {
                binding.userProfileView.staffStatus.setImageResource(R.drawable.icon_profile_status_out)
            }
            "offline" -> {
                binding.userProfileView.staffStatus.setImageResource(R.drawable.icon_profile_status_offline)
            }
            else -> {
                binding.userProfileView.staffStatus.setImageResource(R.drawable.icon_profile_status_offline)
            }
        }

        binding.userProfileView.userNumber.text =profileData?.get("address")
        val fullName = "${profileData?.get("firstName")} ${profileData?.get("lastName")}"
        binding.userProfileView.userName.text= NameConvertion().truncateText(fullName)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.another_menu, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.another_item_1 -> {
                showToast("Another Menu Item 1 Selected")
            }

            R.id.another_item_2 -> {
                showToast("Another Menu Item 2 Selected")
            }

            R.id.another_item_3 -> {
                showToast("Another Menu Item 3 Selected")
            }
        }
        return super.onOptionsItemSelected(item)
    }

//    //set an active fragment programmatically
//    fun setSelectedItem(pos:Int){
//        binding.bottomBar.setSelectedItem(pos)
//    }
//    //set badge indicator
//    fun setBadge(pos:Int){
//        binding.bottomBar.setBadge(pos)
//    }
//    //remove badge indicator
//    fun removeBadge(pos:Int){
//        binding.bottomBar.removeBadge(pos)
//    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun setupSmoothBottomMenu() {
        val popupMenu = PopupMenu(this, null)
        popupMenu.inflate(R.menu.menu_bottom)
        val menu = popupMenu.menu
        binding.bottomBar.setupWithNavController(menu, navController)
    }
    private fun setupSmoothBottomMenuStaff() {
        val popupMenu = PopupMenu(this, null)
        popupMenu.inflate(R.menu.menu_bottom_staff)
        val menu = popupMenu.menu
        binding.bottomBarStaff.setupWithNavController(menu, navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}