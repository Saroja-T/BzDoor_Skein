package com.busydoor.app.activity

import android.app.ActivityManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.busydoor.app.R
import com.busydoor.app.customMethods.ACTIVITY_PREMISE_ID
import com.busydoor.app.customMethods.NameConvertion
import com.busydoor.app.customMethods.NotificationDate
import com.busydoor.app.customMethods.PrefUtils
import com.busydoor.app.customMethods.RetriveRequestOffsiteDate
import com.busydoor.app.customMethods.isNotify
import com.busydoor.app.databinding.ActivityBottomNavBarBinding
import com.busydoor.app.viewmodel.OTPViewModel
import com.busydoor.app.viewmodel.ProfileViewModel
import androidx.activity.addCallback


class BottomNavigationBarActivity : ActivityBase() {
    private lateinit var navController: NavController
    private lateinit var binding: ActivityBottomNavBarBinding
    private var isAdmin:String = ""
    private var selectedTabId:Int = 0
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
        isAdmin = getUserModel()?.data?.accessLevel.toString()
        navController = findNavController(R.id.main_fragment)
        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        otpViewModel = ViewModelProvider(this).get(OTPViewModel::class.java)
        // Extract the selected tab information from the intent
        selectedTabId = intent.getIntExtra("selectedTabId", R.id.fifth_fragment)
        // Set the selected tab programmatically

        if (intent != null) {
            val dataBundle = intent.extras
            if(dataBundle!=null && dataBundle.toString() !="null" && dataBundle.toString() !=""){
                val clickAction= dataBundle!!.getString("click_action")
                if(clickAction !=null && clickAction !="" && clickAction !="null"){
                    ACTIVITY_PREMISE_ID= dataBundle!!.getString("premise_id").toString()
                    RetriveRequestOffsiteDate=dataBundle!!.getString("date").toString()
                    isNotify=true
                }
            }
        }

        if(isAdmin.lowercase()=="admin" || isAdmin.lowercase()=="manager"){
            binding.bottomBarStaff.visibility = View.GONE
            binding.bottomBar.visibility = View.VISIBLE
            setupSmoothBottomMenu()
        }else{
            binding.bottomBarStaff.visibility = View.VISIBLE
            binding.bottomBar.visibility = View.GONE
            setupSmoothBottomMenuStaff()
        }


        profileViewModel.profileData.observe(this) { data ->
            // Handle changes to the shared data in BottomBarFragment
            // The 'data' variable contains the updated value
            // Handle the updated profile data
           Log.e("setValueToProfile","yessss")
            if (data != null) {
                Log.e("setValueToProfile","inside datA")
                // Access individual values like profileData["profileImage"], profileData["firstName"], etc.
                setValueToProfile(data)
            }
        }

        binding.userProfileView.backPage.setOnClickListener{
              onBackClick()

        }
        binding.userProfileView.editProfile.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }
        binding.bottomBar.setOnClickListener {


        }

        binding.bottomBar.onItemSelected = {

            if(it==0){
                navController.navigate(R.id.first_fragment)
                isNotify= false
                RetriveRequestOffsiteDate=""
            }
            Log.e("onItemSelected", "Item $it selected")
        }

        binding.bottomBar.onItemReselected = {
            if(it==0){
                navController.navigate(R.id.first_fragment)
            }
            Log.e("onItemReselected", "Item $it selected")
        }

        binding.bottomBarStaff.onItemSelected = {

            if(it==0){
                navController.navigate(R.id.first_fragment)
                isNotify= false
                RetriveRequestOffsiteDate=""
            }
            Log.e("onItemSelected", "Item $it selected")
        }

        binding.bottomBarStaff.onItemReselected = {
            if(it==0){
                navController.navigate(R.id.first_fragment)
            }
            Log.e("onItemReselected", "Item $it selected")
        }



        //setupSmoothBottomMenu()

        onBackPressedDispatcher.addCallback(this /* lifecycle owner */) {
            // Back is pressed... Finishing the activity
            onBackClick()
        }


    }

    private fun onBackClick() {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val taskList = manager.getRunningTasks(10)
        if (taskList[0].numActivities == 1 && taskList[0].topActivity!!.className == this.javaClass.name) {
            Log.i(TAG, "This is last activity in the stack")
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }else{
            Log.i(TAG, "This is not last activity in the stack")
            finish()
        }
    }

    private fun setValueToProfile(profileData: Map<String, String>?) {
        Log.e("setValueToProfile",objSharedPref.getString("userImage").toString()+profileData?.get("profileImage"))
        val circularProgressDrawable = CircularProgressDrawable(this)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()

            if (binding.userProfileView.PremiseStaffImage != null) {
                Glide.with(this)
                    .load(profileData?.get("profileImage"))
                    .placeholder(circularProgressDrawable)
                    .timeout(1000)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .into(binding.userProfileView.PremiseStaffImage)
            } else {
                Glide.with(this)
                    .load(R.drawable.icon_users)
                    .placeholder(circularProgressDrawable)
                    .timeout(1000)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
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
    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun setupSmoothBottomMenu() {
        val popupMenu = PopupMenu(this, null)
        popupMenu.inflate(R.menu.menu_bottom)
        val menu = popupMenu.menu
        if(isNotify){
            binding.bottomBar.setupWithNavController(menu, navController)
            navController.navigate(R.id.fifth_fragment)
//            isNotify=false
        }else{
            binding.bottomBar.setupWithNavController(menu, navController)
        }
    }
    private fun setupSmoothBottomMenuStaff() {
        val popupMenu = PopupMenu(this, null)
        popupMenu.inflate(R.menu.menu_bottom_staff)
        val menu = popupMenu.menu
        if(isNotify){
            binding.bottomBarStaff.setupWithNavController(menu, navController)
            navController.navigate(R.id.fifth_fragment)
        }else{
            binding.bottomBarStaff.setupWithNavController(menu, navController)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}