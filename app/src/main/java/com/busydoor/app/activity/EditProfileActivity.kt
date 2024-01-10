package com.busydoor.app.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import com.busydoor.app.R
import com.busydoor.app.customMethods.PrefUtils
import com.busydoor.app.databinding.ActivityEditProfileBinding


class EditProfileActivity : ActivityBase(), AdapterView.OnItemSelectedListener {
    /**  Set binding the this page **/
    private val binding by lazy { ActivityEditProfileBinding.inflate(layoutInflater) }
    override lateinit var objSharedPref: PrefUtils
    override var cryptLib: CryptLib2? = null
    private var accessLevelList: ArrayList<String>? = null
    private var accessLevelName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*** initialize biding here  */
        setContentView(binding.root)
        objSharedPref = PrefUtils(this)
        cryptLib = CryptLib2()
        accessLevelList = ArrayList()

        binding.backPagetoUserList.drawerMenu.setOnClickListener {
            finish()
        }
        accessLevelList!!.add("")
        accessLevelList!!.add("Manager")
        accessLevelList!!.add("Staff")
        binding.etFirstName.setText(getUserModel()!!.data.firstName)
        binding.edLastName.setText(getUserModel()!!.data.lastName)
        binding.edMobileNumber.setText(getUserModel()!!.data.phoneNumber)

        /** Access Level set fun here... **/
        when (getUserModel()!!.data.accessLevel.toLowerCase()) {
            "manager" -> {
                Log.e("accessLevelNames",getUserModel()!!.data.accessLevel)
                binding.spAccessLevel.text="Manager"
            }
            "staff" -> {
                Log.e("accessLevelNames",getUserModel()!!.data.accessLevel)
                binding.spAccessLevel.text="Staff"
            }
            "admin" ->{
                Log.e("accessLevelName",getUserModel()!!.data.accessLevel)
                binding.spAccessLevel.text="Admin"
            }
            else -> {
                Log.e("accessLevelNames",getUserModel()!!.data.accessLevel)
                binding.spAccessLevel.text=""

            }
        }
    }

    private fun setSpinner1(spinner: Spinner, list: java.util.ArrayList<String>, defaultValue: String?) {

        spinner.onItemSelectedListener = this
        val updatedList = ArrayList(list) // Copy the original list to avoid modifying it directly
        defaultValue?.let {
            updatedList.add(0, it) // Add the default value at the beginning of the list
        }
        val adapter = object : ArrayAdapter<String>(
            this@EditProfileActivity,
            R.layout.item_detail__second_row,
            list
        ) {

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup?
            ): View {
                val view = super.getDropDownView(position, convertView, parent!!)
                view as TextView
                return view
            }
        }
        adapter.setDropDownViewResource(R.layout.item_detail__second_row)
        spinner.adapter = adapter
        defaultValue?.let { value ->
            val index = updatedList.indexOf(value)
            if (index != -1) {
                spinner.setSelection(index) // Set the selection to the default value if found
            }
        }
    }


    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent) {
            binding.spAccessLevel -> {
                accessLevelName = accessLevelList!![position]
                Log.e("accessLevelName",accessLevelName)
            }
        }
    }


    override fun onNothingSelected(p0: AdapterView<*>?) {
    }

}