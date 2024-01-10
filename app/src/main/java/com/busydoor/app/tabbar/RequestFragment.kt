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
import com.busydoor.app.activity.CryptLib2
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
import com.busydoor.app.databinding.FragmentYourActivityBinding
import com.busydoor.app.model.UserActivities
import com.busydoor.app.model.UserModel
import com.google.gson.Gson

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RequestFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RequestFragment : Fragment(), ApiResponseInterface {

    open lateinit var objSharedPref: PrefUtils
    var cryptLib: CryptLib2? = null
    private var requestAllDataGet: UserActivities? = null
    private lateinit var binding: FragmentYourActivityBinding

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
//            getAllActivities("2024-01-09")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentYourActivityBinding.inflate(inflater, container, false)
        val root = binding.root
        // Inflate the layout for this fragment
        objSharedPref = PrefUtils(requireContext())
        cryptLib = CryptLib2()
        return  root
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        getAllActivities("2024-01-09")

    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onResume() {
        super.onResume()
//        getAllActivities("2024-01-09")

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
//                        setHomeOfferData(requestAllDataGet!!.data as ArrayList<UserActivities.Data>)
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