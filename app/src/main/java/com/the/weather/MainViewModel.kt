package com.the.weather

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel: ViewModel() {

    val liveDataCurrent =
        MutableLiveData<WetherModel>()
    val liveDataList =
        MutableLiveData<List<WetherModel>>()
}