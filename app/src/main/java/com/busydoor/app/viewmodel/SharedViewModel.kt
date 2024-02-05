package com.busydoor.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

// SharedViewModel.java


class SharedViewModel : ViewModel() {
    private val _sharedData = MutableLiveData<String>()
    var sharedData: LiveData<String> = _sharedData

    fun setSharedData(data: String) {
        _sharedData.value = data
    }
    fun getSharedData(): String? {
        return _sharedData.value
    }
}
