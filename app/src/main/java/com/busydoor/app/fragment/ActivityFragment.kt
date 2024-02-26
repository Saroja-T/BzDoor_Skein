package com.busydoor.app.fragment
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.busydoor.app.R
import com.busydoor.app.activity.CryptLib2
import com.busydoor.app.apiService.ApiInitialize
import com.busydoor.app.apiService.ApiRequest
import com.busydoor.app.apiService.ApiResponseInterface
import com.busydoor.app.apiService.ApiResponseManager
import com.busydoor.app.customMethods.ACTIVITY_PREMISE_ID
import com.busydoor.app.customMethods.ALL_REQUEST_OFFSITE
import com.busydoor.app.customMethods.DatePickerUtil
import com.busydoor.app.customMethods.ENCRYPTION_IV
import com.busydoor.app.customMethods.PrefUtils
import com.busydoor.app.customMethods.RetriveRequestOffsiteDate
import com.busydoor.app.customMethods.SUCCESS_CODE
import com.busydoor.app.customMethods.convertDate
import com.busydoor.app.customMethods.encode
import com.busydoor.app.customMethods.isOnline
import com.busydoor.app.customMethods.key
import com.busydoor.app.databinding.FragmentActivityBinding
import com.busydoor.app.model.UserActivities
import com.busydoor.app.model.UserModel
import com.busydoor.app.tabbar.RequestFragment
import com.busydoor.app.tabbar.YourActivityFragment
import com.busydoor.app.viewmodel.ProfileViewModel
import com.busydoor.app.viewmodel.SharedViewModel
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson


class ActivityFragment : Fragment(),ApiResponseInterface {

    private lateinit var binding: FragmentActivityBinding
    open lateinit var objSharedPref: PrefUtils
    var cryptLib: CryptLib2? = null
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager
    private var requestAllDataGet: UserActivities? = null
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var profileViewModel: ProfileViewModel

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
        profileViewModel = ViewModelProvider(requireActivity()).get(ProfileViewModel::class.java)
        // Inflate the layout for this fragment
        tabLayout =root.findViewById(R.id.tabLayout)
        viewPager = root.findViewById(R.id.viewPager)
        objSharedPref = PrefUtils(requireContext())
        cryptLib = CryptLib2()

        // Initialize the ViewModel
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]


        binding.calendarIcon.setOnClickListener{

            DatePickerUtil.showDatePicker(
                requireContext(),
                sharedViewModel.getSharedData()!!
            ) { formattedDate ->
                // Update your UI or perform other actions with the selected date
                binding.offsiteDateTime.text =
                    convertDate(formattedDate, "yyyy-MM-dd", "EEE, MMM dd, yyyy")
                /*** Function to staffListGet when click datePicker select a date to call api request */
                // Set data in MainFragment
                sharedViewModel.setSharedData(formattedDate)
                getAllActivities(formattedDate)
            }
        }


        if(RetriveRequestOffsiteDate !=null&& RetriveRequestOffsiteDate!="null"&& RetriveRequestOffsiteDate!=""){
//            Toast.makeText(requireContext(),RetriveRequestOffsiteDate.toString(), Toast.LENGTH_SHORT).show()
            binding.offsiteDateTime.text =
                convertDate(RetriveRequestOffsiteDate, "yyyy-MM-dd", "EEE - dd MMM',' yyyy")
            getAllActivities(RetriveRequestOffsiteDate)
        }else {
//            Toast.makeText(requireContext(),RetriveRequestOffsiteDate.toString()+"lll", Toast.LENGTH_SHORT).show()
            sharedViewModel.sharedData.observe(viewLifecycleOwner) { data ->
                Log.e("sharedData", data)
                binding.offsiteDateTime.text =
                    convertDate(data, "yyyy-MM-dd", "EEE - dd MMM',' yyyy")
                getAllActivities(data)
            }
        }


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
                Log.e("apiCalled", ACTIVITY_PREMISE_ID)
                ApiRequest(
                    requireActivity(),
                    ApiInitialize.initialize(ApiInitialize.LOCAL_URL).getYourActivitiesList(
                        "Bearer ${getUserModel()!!.data.token}",
                        encrypt(ACTIVITY_PREMISE_ID),
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
                    if (requestAllDataGet!!.data != null || requestAllDataGet!!.data.toString() !="null" ) {
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

    private fun setHomeOfferData(dataModel: UserActivities.Data?) {
        profileViewModel.setProfileData(dataModel!!.userDetails!!.userImage.toString(),
            dataModel.userDetails!!.userFirstName.toString(),
            dataModel.userDetails!!.userLastName.toString(),
            dataModel.userDetails!!.userStatus.toString(),
            dataModel.premiseDetails!!.premiseName +", "+dataModel.premiseDetails!!.city+", "+dataModel.premiseDetails!!.state,
        )

    }

}