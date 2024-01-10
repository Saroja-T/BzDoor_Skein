
package com.busydoor.app.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.busydoor.app.R
import com.busydoor.app.activity.CryptLib2
import com.busydoor.app.activity.EditProfileActivity
import com.busydoor.app.adapter.ActivityAdapter
import com.busydoor.app.customMethods.ENCRYPTION_IV
import com.busydoor.app.customMethods.PrefUtils
import com.busydoor.app.customMethods.encode
import com.busydoor.app.customMethods.key
import com.busydoor.app.databinding.FragmentActivityBinding
import com.busydoor.app.databinding.FragmentManagerBinding
import com.busydoor.app.model.UserModel
import com.busydoor.app.tabbar.RequestFragment
import com.busydoor.app.tabbar.YourActivityFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson


class ActivityFragment : Fragment() {

    private lateinit var binding: FragmentActivityBinding
    open lateinit var objSharedPref: PrefUtils
    var cryptLib: CryptLib2? = null
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

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
}