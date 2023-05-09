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
    private val _updatedProductArray = MutableLiveData<JSONArray>()

    val orderInfo: LiveData<JSONObject?> = _orderInfo
    val productArray: LiveData<JSONArray?> = _productArray
    val scannedResults: LiveData<List<String>> = _scannedResults
    val updatedProductArray: LiveData<JSONArray> = _updatedProductArray

    init {
        productArray.observeForever { jsonArray ->
            if (jsonArray != null && _updatedProductArray.value == null) {
                _updatedProductArray.value = jsonArray
            }
        }
    }
    fun setOrderInfo(jsonObject: JSONObject) {
        _orderInfo.value = jsonObject
    }
    fun setProductArray(jsonArray: JSONArray) {
        _productArray.value = jsonArray
    }
    fun updateScannedResults(results: List<String>) {
        _scannedResults.value = results
    }
    fun updateProductQuantities(scannedResults: List<String>) {
        val updatedArray = JSONArray()
        val originalArray = _productArray.value ?: return

        for (i in 0 until originalArray.length()) {
            val product = originalArray.getJSONObject(i)
            val productId = product.getString("ItemCode")
            val originalQuantity = product.getInt("Cantidad")
            val scannedCount = scannedResults.count { it == productId }

            val updatedProduct = JSONObject(product.toString()) // Crear una copia del objeto para no modificar el original
            updatedProduct.put("Cantidad", originalQuantity - scannedCount)
            updatedArray.put(updatedProduct)
        }

        _updatedProductArray.value = updatedArray
    }
}