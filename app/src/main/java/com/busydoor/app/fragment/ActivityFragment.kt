
package com.busydoor.app.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import com.busydoor.app.R
import com.busydoor.app.adapter.ActivityAdapter
import com.busydoor.app.databinding.FragmentActivityBinding
import com.busydoor.app.databinding.FragmentManagerBinding
import com.google.android.material.tabs.TabLayoutMediator


class ActivityFragment : Fragment() {

    private lateinit var binding: FragmentActivityBinding


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
        return root
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up ViewPager with adapter
        val adapter = ActivityAdapter(requireActivity())
        binding.viewPager.adapter = adapter

        // Connect TabLayout and ViewPager
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            if(position==0)
                tab.text = "Your Activities"
            if(position==1)
                tab.text = "Requests"
        }.attach()
    }


}