package com.busydoor.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfileViewModel : ViewModel() {
    private val _profileData = MutableLiveData<Map<String,String>>()
    var profileData: LiveData<Map<String,String>> = _profileData

    fun setProfileData(profileImage: String,
                       firstName: String,
                       lastName: String,
                       userStatus: String,
                       address:String) {
        val profileMap = mutableMapOf<String, String>().apply {
            put("profileImage", profileImage)
            put("firstName", firstName)
            put("lastName", lastName)
            put("userStatus", userStatus)
            put("address", address)
        }
        _profileData.value = profileMap
    }
    fun getProfileData(): Map<String,String>? {
        return _profileData.value
    }
}