
package com.busydoor.app.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.busydoor.app.R
import com.busydoor.app.activity.CryptLib2
import com.busydoor.app.activity.EditProfileActivity
import com.busydoor.app.apiService.ApiInitialize
import com.busydoor.app.apiService.ApiRequest
import com.busydoor.app.apiService.ApiResponseInterface
import com.busydoor.app.apiService.ApiResponseManager
import com.busydoor.app.customMethods.ALL_REQUEST_OFFSITE
import com.busydoor.app.customMethods.ENCRYPTION_IV
import com.busydoor.app.customMethods.PrefUtils
import com.busydoor.app.customMethods.SUCCESS_CODE
import com.busydoor.app.customMethods.encode
import com.busydoor.app.customMethods.isOnline
import com.busydoor.app.customMethods.key
import com.busydoor.app.databinding.FragmentActivityBinding
import com.busydoor.app.model.UserActivities
import com.busydoor.app.model.UserModel
import com.busydoor.app.tabbar.RequestFragment
import com.busydoor.app.tabbar.YourActivityFragment
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson


class ActivityFragment : Fragment(),ApiResponseInterface {

    private lateinit var binding: FragmentActivityBinding
    open lateinit var objSharedPref: PrefUtils
    var cryptLib: CryptLib2? = null
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private var requestAllDataGet: UserActivities? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentActivityBinding.inflate(inflater, container, false)
        val root = binding.root
        // Inflate the layout for this fragment
        tabLayout =root.findViewById(R.id.tabLayout)
        viewPager = root.findViewById(R.id.viewPager)
        objSharedPref = PrefUtils(requireContext())
        cryptLib = CryptLib2()

        binding.userProfileView.editProfile.setOnClickListener {
            startActivity(Intent(requireActivity(), EditProfileActivity::class.java))
        }
        binding.calendarIcon.setOnClickListener{
            requireActivity().recreate()
        }
        getAllActivities("2023-12-28")
        val adapter = YourPagerAdapter(childFragmentManager)
        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)

        return root
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    inner class YourPagerAdapter(fm: FragmentManager) :
        FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> YourActivityFragment()
                1 -> RequestFragment()
                else -> throw IllegalArgumentException("Invalid position")
            }
        }

        override fun getCount(): Int {
            return 2
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return when (position) {
                0 -> "Activities"
                1 -> "Requests"
                else -> null
            }
        }
    }
    fun getUserModel(): UserModel? {
        val gson = Gson()
        return gson.fromJson(
            objSharedPref.getString(getString(R.string.userResponse))!!, UserModel::class.java
        )
    }

    open fun encrypt(value: String): String {
        return if (TextUtils.isEmpty(value)) {
            value.replace('\"', ' ', ignoreCase = false)
        } else {
            val values = cryptLib!!.encrypt(value, key, ENCRYPTION_IV)
            Log.e(
                "TAG",
                "original value == $value " +
                        "encrypted message ==> ${
                            values!!.trimEnd().encode()
                        }"
            )
            values.trimEnd().encode()
        }
    }

    /** Api request for get all data fun... **/
    @RequiresApi(Build.VERSION_CODES.R)
    fun getAllActivities(date:String) {
        try {
            if (isOnline(requireContext())) {
                Log.e("apiCalled", " ap")
                ApiRequest(
                    requireActivity(),
                    ApiInitialize.initialize(ApiInitialize.LOCAL_URL).getYourActivitiesList(
                        "Bearer ${getUserModel()!!.data.token}",
                        encrypt("1"),
                        encrypt(date)
                    ),
                    ALL_REQUEST_OFFSITE,
                    true,
                    this
                )
            }else {
                Log.e("Application", "offline")
            }
        } catch (e: Exception) {
            Log.e("APIEXceptions", e.toString())

        }

    }

    override fun getApiResponse(apiResponseManager: ApiResponseManager<*>) {
        when(apiResponseManager.type) {
            ALL_REQUEST_OFFSITE -> {
                Log.e("apiCalled", " getApiResponse0")
                requestAllDataGet = apiResponseManager.response as UserActivities
                if (requestAllDataGet!!.statusCode == SUCCESS_CODE) {
                    Log.e("apiCalled", " getApiResponse1")
                    if (requestAllDataGet!!.data != null) {
                        Log.e("apiCalled", " getApiResponse2")
                        setHomeOfferData(requestAllDataGet!!.data)
                        Log.e("zzzzzzzz",requestAllDataGet!!.data.toString())
                    } else {
                        // no data found
                        Log.e("zzzzzzzz","no data found")

                    }
                } else {

                }
            }
        }

    }

    private fun setHomeOfferData(model: UserActivities.Data?) {
        Log.e("apiCalled", " getApiResponse3")
        binding.userProfileView.userName.text= model!!.userdetails!!.userFirstName+" "+model.userdetails!!.userLastName
        binding.userProfileView.userNumber.text = model.premisedetails!!.premiseName+", "+model.premisedetails!!.city+","+model.premisedetails!!.state


        val circularProgressDrawable = CircularProgressDrawable(requireContext())
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.backgroundColor= R.color.app_color
        circularProgressDrawable.start()
        if(model.userdetails !=null) {
            Log.e("adapterview",model.userdetails.toString())

            Glide.with(requireContext())
                .load(model.userdetails!!.userImage)
                .placeholder(circularProgressDrawable)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(binding.userProfileView.PremiseStaffImage)
        }else{
            Glide.with(requireContext())
                .load(R.drawable.icon_users)
                .placeholder(circularProgressDrawable)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(binding.userProfileView.PremiseStaffImage)
        }
        when (model!!.userdetails!!.userStatus) {
            "in" -> {              binding.userProfileView.PremiseStaffImage.setImageResource(R.drawable.premiselist_staff_satus_in)
            }
            "inout" -> {        binding.userProfileView.staffStatus.setImageResource(R.drawable.icon_profile_status_inout)
            }
            "out" -> {        binding.userProfileView.staffStatus.setImageResource(R.drawable.icon_profile_status_out)
            }
            "offline" -> {        binding.userProfileView.staffStatus.setImageResource(R.drawable.icon_profile_status_offline)
            }
            else -> {        binding.userProfileView.staffStatus.setImageResource(R.drawable.icon_profile_status_offline)
            }
        }
    }

}