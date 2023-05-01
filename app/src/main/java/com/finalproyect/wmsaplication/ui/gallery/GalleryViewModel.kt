package com.finalproyect.wmsaplication.ui.gallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.json.JSONArray
import org.json.JSONObject
import java.util.ArrayList

class GalleryViewModel : ViewModel() {

    private val _orderInfo = MutableLiveData<JSONObject?>()
    private val _productArray = MutableLiveData<JSONArray?>()
    private val _scannedResults = MutableLiveData<List<String>>()
    val orderInfo: LiveData<JSONObject?> = _orderInfo
    val productArray: LiveData<JSONArray?> = _productArray
    val scannedResults: LiveData<List<String>> = _scannedResults
    fun setOrderInfo(jsonObject: JSONObject) {
        _orderInfo.value = jsonObject
    }
    fun setProductArray(jsonArray: JSONArray) {
        _productArray.value = jsonArray
    }
    fun updateScannedResults(results: List<String>) {
        _scannedResults.value = results
    }
}