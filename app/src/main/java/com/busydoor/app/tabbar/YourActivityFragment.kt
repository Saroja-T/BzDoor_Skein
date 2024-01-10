package com.busydoor.app.tabbar

import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.busydoor.app.activity.CryptLib2
import com.busydoor.app.adapter.YourActivityListAdapter
import com.busydoor.app.apiService.ApiInitialize
import com.busydoor.app.apiService.ApiRequest
import com.busydoor.app.apiService.ApiResponseInterface
import com.busydoor.app.apiService.ApiResponseManager
import com.busydoor.app.customMethods.ALL_REQUEST_OFFSITE
import com.busydoor.app.customMethods.ENCRYPTION_IV
import com.busydoor.app.customMethods.PrefUtils
import com.busydoor.app.customMethods.SUCCESS_CODE
import com.busydoor.app.customMethods.convertDate
import com.busydoor.app.customMethods.encode
import com.busydoor.app.customMethods.isOnline
import com.busydoor.app.customMethods.key
import com.busydoor.app.databinding.FragmentYourActivityBinding
import com.busydoor.app.model.RequestAllOffsiteResponse
import com.busydoor.app.model.UserActivities
import com.busydoor.app.model.UserModel
import com.google.gson.Gson


class YourActivityFragment : Fragment(),ApiResponseInterface {
    private var userSelectedDate: String?= null
    private var premiseID: String?= null
    private var displayCurrentDate: String?= null
    private lateinit var binding: FragmentYourActivityBinding
    open lateinit var objSharedPref: PrefUtils
    var cryptLib: CryptLib2? = null
    private var requestAllDataGet: UserActivities? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            Log.e("setHomeOfferData","")

        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.e("setHomeOfferData","")

        // Inflate the layout for this fragment
        binding = FragmentYourActivityBinding.inflate(inflater, container, false)
        val root = binding.root
        // Inflate the layout for this fragment
        objSharedPref = PrefUtils(requireContext())
        cryptLib = CryptLib2()
        premiseID= activity?.intent?.getStringExtra("premise_id")
        userSelectedDate= activity?.intent?.getStringExtra("userSelectDate")
        displayCurrentDate = convertDate(userSelectedDate,"yyyy-MM-dd","EEE - dd MMM',' yyyy")
//        binding.offsiteSelectedDate.text= displayCurrentDate
        return root
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.e("setHomeOfferData","")
        getAllActivities("2024-01-09")

    }
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onResume() {
        super.onResume()
        getAllActivities("2024-01-09")

    }

    private fun setHomeOfferData(
        data: ArrayList<UserActivities.Data>
    ) {
        Log.e("setHomeOfferData","")
         if (data.size > 0) {
        binding.rvYourActivities.setHasFixedSize(true)
        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvYourActivities.layoutManager = layoutManager
        val YourActivityListAdapter =
            YourActivityListAdapter(
                requireContext(),
                  data,
            )
        // Create adapter object
        binding.rvYourActivities.adapter = YourActivityListAdapter
            binding.rvYourActivities.visibility = View.VISIBLE
        YourActivityListAdapter.notifyDataSetChanged()

        } else {
//            binding.premiseNoData.visibility = View.VISIBLE
            binding.rvYourActivities.visibility = View.GONE
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

    fun getUserModel(): UserModel? {
        val gson = Gson()
        return gson.fromJson(
            objSharedPref.getString(getString(com.busydoor.app.R.string.userResponse))!!, UserModel::class.java
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

    override fun getApiResponse(apiResponseManager: ApiResponseManager<*>) {
        when(apiResponseManager.type) {
            ALL_REQUEST_OFFSITE -> {
                requestAllDataGet = apiResponseManager.response as UserActivities
                if (requestAllDataGet!!.statusCode == SUCCESS_CODE) {
                    if (requestAllDataGet!!.data != null) {
                        setHomeOfferData(requestAllDataGet!!.data as ArrayList<UserActivities.Data>)
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


}