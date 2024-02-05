package com.busydoor.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class OTPViewModel : ViewModel() {
    private val _homeData = MutableLiveData<String>()
    var homeData: LiveData<String> = _homeData

    fun sethomeData(data: String) {
        _homeData.value = data
    }
    fun gethomeData(): String? {
        return _homeData.value
    }
}