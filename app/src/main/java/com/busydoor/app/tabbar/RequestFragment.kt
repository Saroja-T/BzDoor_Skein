package com.busydoor.app.tabbar

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.busydoor.app.activity.CryptLib2
import com.busydoor.app.adapter.RequestsAdapter
import com.busydoor.app.apiService.ApiInitialize
import com.busydoor.app.apiService.ApiRequest
import com.busydoor.app.apiService.ApiResponseInterface
import com.busydoor.app.apiService.ApiResponseManager
import com.busydoor.app.customMethods.ACTIVITY_PREMISE_ID
import com.busydoor.app.customMethods.ALL_REQUEST_OFFSITE
import com.busydoor.app.customMethods.AcceptRejectOffsite
import com.busydoor.app.customMethods.ENCRYPTION_IV
import com.busydoor.app.customMethods.MyCustomEdittext
import com.busydoor.app.customMethods.PrefUtils
import com.busydoor.app.customMethods.RetriveRequestOffsiteDate
import com.busydoor.app.customMethods.SUCCESS_CODE
import com.busydoor.app.customMethods.encode
import com.busydoor.app.customMethods.isOnline
import com.busydoor.app.customMethods.key
import com.busydoor.app.databinding.FragmentRequestBinding
import com.busydoor.app.interfaceD.RequestClick
import com.busydoor.app.model.AcceptOffsiteRes
import com.busydoor.app.model.UserActivities
import com.busydoor.app.model.UserModel
import com.busydoor.app.viewmodel.SharedViewModel
import com.google.gson.Gson


class RequestFragment : Fragment(), ApiResponseInterface,RequestClick {

    open lateinit var objSharedPref: PrefUtils
    var cryptLib: CryptLib2? = null
    private var requestAllDataGet: UserActivities? = null
    private var aceeptRejectData: AcceptOffsiteRes? = null
    private lateinit var binding: FragmentRequestBinding
    private var notificationId: Int? =null
    private lateinit var sharedViewModel: SharedViewModel

    @RequiresApi(Build.VERSION_CODES.R)
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
        // Inflate the layout for this fragment
        binding = FragmentRequestBinding.inflate(inflater, container, false)
        val root = binding.root
        // Inflate the layout for this fragment
        objSharedPref = PrefUtils(requireContext())
        cryptLib = CryptLib2()
        // Retrieve data in TabBarFragment
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        Log.e("zzzzzzzzzzzzz",RetriveRequestOffsiteDate.toString())
            sharedViewModel.sharedData.observe(viewLifecycleOwner) { data ->
                // Handle changes to the shared data in TabBarFragment
                // The 'data' variable contains the updated value
                Log.e("sharedData", data)
                getAllActivities(data)
            }
        return  root
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onResume() {
        super.onResume()

    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun activityPositionClick(postion: Int,status: String,comments: String){
        notificationId= postion
        if(status=="accept"){
            Log.e("activityPositionClick",status)
            showAlertBox(status)
        }else if(status=="reject"){
            Log.e("activityPositionClick",status)
            showAlertBox(status)
        }
    }

    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.R)
    fun showAlertBox(type:String){
        val dialog = AlertDialog.Builder(requireContext())
        val view: View = layoutInflater.inflate(com.busydoor.app.R.layout.custom_alert_dialog, null)
        dialog.setView(view)
        dialog.setCancelable(false)
        val alert = dialog.create()
        alert.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val content = view.findViewById<View>(com.busydoor.app.R.id.dialog_text) as TextView
        val comments = view.findViewById<View>(com.busydoor.app.R.id.Activity_comments) as LinearLayout
        val commentsbyAdmin = view.findViewById<View>(com.busydoor.app.R.id.commentsTextView) as MyCustomEdittext
        val tittle = view.findViewById<View>(com.busydoor.app.R.id.dialog_tittle_text) as TextView
        val cancel = view.findViewById<View>(com.busydoor.app.R.id.cancel_action) as Button
        cancel.setOnClickListener { alert.dismiss() }
        val ok = view.findViewById<View>(com.busydoor.app.R.id.ok_action) as Button
        when(type){
            "accept"->{
                tittle.text="Accept Request";
                content.visibility=View.GONE
                comments.visibility=View.VISIBLE
                ok.text="Accept"
                ok.setOnClickListener {
                    alert.dismiss()
                    sendAcceptRequest(notificationId.toString(),commentsbyAdmin.text.toString(),"approved")
                }
            }
            "reject"->{
                tittle.text="Cancel Request";
                content.visibility=View.GONE
                comments.visibility=View.VISIBLE
                ok.text="Reject"
                ok.setOnClickListener{
                    sendAcceptRequest(notificationId.toString(),commentsbyAdmin.text.toString(),"rejected")
                    alert.dismiss()
                }
            }
            "api-success"->{
                // automatically close the AlertDialog after 2 seconds
                val handler = Handler()
                handler.postDelayed({
                    if (alert.isShowing) {
                        alert.dismiss()
                        getAllActivities(sharedViewModel.getSharedData().toString())

                    }
                }, 2000)
                alert.setCancelable(true)
                tittle.text="Success"
                content.text="The request was successfully updated."
                cancel.visibility =View.GONE
                ok.visibility =View.GONE
                ok.text= "Close"
            }
            "api-failure"->{// automatically close the AlertDialog after 2 seconds
                val handler = Handler()
                handler.postDelayed({
                    if (alert.isShowing) {
                        alert.dismiss()
                    }
                }, 2000)
                alert.setCancelable(true)
                tittle.text="Alert!"
                content.text=aceeptRejectData!!.message.toString();
                ok.text= "Close"
                ok.visibility= View.VISIBLE
                cancel.visibility= View.GONE
                ok.setOnClickListener {
                    alert.dismiss()
                }
            }
        }
        alert.show()
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

    @RequiresApi(Build.VERSION_CODES.R)
    override fun getApiResponse(apiResponseManager: ApiResponseManager<*>) {
        when(apiResponseManager.type) {
            ALL_REQUEST_OFFSITE -> {
                requestAllDataGet = apiResponseManager.response as UserActivities
                if (requestAllDataGet!!.statusCode == SUCCESS_CODE) {
                    if (requestAllDataGet!!.data!!.offsitedetails != null) {
                        setHomeOfferData(requestAllDataGet!!.data!!.offsitedetails as ArrayList<UserActivities.Data.Offsitedetails>)
                    } else {
                        // no data found
                        binding.noData.text="No Request Off site's found this Date"
                    }
                } else {

                }
            }
            AcceptRejectOffsite -> {
                aceeptRejectData = apiResponseManager.response as AcceptOffsiteRes
                Log.e("AcceptRejectOffsite",aceeptRejectData.toString())
                if(aceeptRejectData!!.statusCode == SUCCESS_CODE){
//                    getAllActivities(globalDate)
                    showAlertBox("api-success")
                }else{
                    showAlertBox("api-failure")
                }
            }
        }

    }

    private fun setHomeOfferData(data: ArrayList<UserActivities.Data.Offsitedetails>) {
        Log.e("setHomeOfferData","")
        if (data.size > 0) {
            binding.rvRequests.setHasFixedSize(true)
            val layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            binding.rvRequests.layoutManager = layoutManager
            val requestListAdapter =
                RequestsAdapter(
                    requireContext(),
                    data,
                    this,getUserModel()!!.data.accessLevel)
            // Create adapter object
            binding.rvRequests.adapter = requestListAdapter
            binding.rvRequests.visibility = View.VISIBLE
            binding.activityNodataView.visibility = View.GONE
            requestListAdapter.notifyDataSetChanged()

        } else {
            binding.activityNodataView.visibility = View.VISIBLE
            binding.noData.text="No Request offsite were found for this date"
            binding.rvRequests.visibility = View.GONE
        }
    }


    /** Api request for get all data fun... **/
    @RequiresApi(Build.VERSION_CODES.R)
    fun sendAcceptRequest(id:String,comments:String,status:String) {
        try {
            if (isOnline(requireContext())) {
                Log.e("apiCalled", "comments ('pending','approved','rejected')")
                ApiRequest(
                    requireActivity(),
                    ApiInitialize.initialize(ApiInitialize.LOCAL_URL).setPermissionoffsite(
                        "Bearer ${getUserModel()!!.data.token}",
                        encrypt(id),
                        encrypt(status),
                        encrypt(comments)
                    ),
                    AcceptRejectOffsite,
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

}