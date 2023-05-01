package com.finalproyect.wmsaplication.ui.gallery

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.finalproyect.wmsaplication.ContinuousCaptureActivity
import com.finalproyect.wmsaplication.ContinuousCaptureActivity.Companion.EXTRA_RESULT
import com.finalproyect.wmsaplication.R
import com.finalproyect.wmsaplication.adapters.ItemAdapter
import com.finalproyect.wmsaplication.databinding.FragmentGalleryBinding
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import org.json.JSONArray
import org.json.JSONObject

class GalleryFragment : Fragment() {
    private var orderInfo: JSONObject? = null
    private var productArray: JSONArray? = null
    private var _binding: FragmentGalleryBinding? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewModel: GalleryViewModel
    private var scannedResults: MutableList<String> = mutableListOf()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[GalleryViewModel::class.java]
        getOrderInfo { jsonObject,jsonArray ->
            viewModel.setOrderInfo(jsonObject)
            viewModel.setProductArray(jsonArray)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        //val galleryViewModel = ViewModelProvider(this).get(GalleryViewModel::class.java)

        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val divider = ContextCompat.getDrawable(requireContext(), R.drawable.divider)

        recyclerView = binding.reciclerId
        recyclerView.layoutManager = LinearLayoutManager(context)
        val itemDecoration = DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
        itemDecoration.setDrawable(divider!!)
        recyclerView.addItemDecoration(itemDecoration)

        viewModel.orderInfo.observe(viewLifecycleOwner) { jsonObject ->
            if (jsonObject != null) {
                binding.OrdenID.text = orderInfo?.getString("OrderID") ?: "0000"
            }
        }
        viewModel.productArray.observe(viewLifecycleOwner){ jsonArray ->
            if (jsonArray != null){
                val itemAdapter = ItemAdapter(requireContext(),jsonArray)
                recyclerView.adapter = itemAdapter
            }
        }
        viewModel.scannedResults.observe(viewLifecycleOwner){ resultString ->
            if (resultString != null){
                scannedResults = resultString.toMutableList()
            }
        }
        val scanButton = binding.ScannerButton
        scanButton.setOnClickListener {_: View? ->startScan() }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

        private val scanContinuous =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                Log.d("ScanTag", "activity ended $result")
                    val data: Intent? = result.data
                    val resultString = data?.getStringArrayExtra(EXTRA_RESULT)
                    if (resultString != null) {
                        scannedResults.addAll(resultString)
                        Log.d("ScanTag", "startScan() called. scannedResults: $scannedResults")
                        viewModel.updateScannedResults(scannedResults)
                    }
            }
    private fun startScan() {
        val orderInfoJsonString = orderInfo?.toString()
        val intent = Intent(requireActivity(), ContinuousCaptureActivity::class.java)
        intent.putExtra("ORDER_INFO", orderInfoJsonString)
        intent.putExtra("Scanned", scannedResults.toTypedArray())
        Log.d("ScanTag", "startScan() called. scannedResults: $scannedResults")
        scanContinuous.launch(intent)

    }
    //Get Info of an open incoming order
    private fun getOrderInfo(callback: (JSONObject,JSONArray) -> Unit) {
        if (orderInfo != null && productArray != null) {
            // Datos ya obtenidos previamente, devolverlos usando el callback
            callback(orderInfo!!,productArray!!)
            return
        }
        val queue = Volley.newRequestQueue(context)
        val jsonObject = JsonObjectRequest(
            //http get to the server
            "http://52.4.150.68/api/getOrderIn",
            { response ->
                Log.d(ContentValues.TAG, "Response is: $response")
                //Obtain each json data
                val products = response.getString("Products")
                //transform products into a json object
                val productsJSON = JSONObject(products)
                val productIdList = mutableListOf<String>()
                val iterator = productsJSON.keys()
                //get each key and its value
                while (iterator.hasNext()) {
                    val productId = iterator.next() as String
                    val quantity = productsJSON.getInt(productId)
                    //add each product ID to the list
                    productIdList.add(productId)
                }
                //creates a string for with all the product ID values retrieved
                val productIds = productIdList.joinToString(",")
                getInfoList(productIds) { jsonArray ->
                    val productos = JSONArray()
                    //iterate over the JSON objects in the JSONArray to add the quantity
                    for (i in 0 until jsonArray.length()) {
                        //gets one jsonObject from the array
                        val jsonObjeto = jsonArray.getJSONObject(i)
                        //searches the quantity of that product and adds it to the products array
                        jsonObjeto.put("Cantidad", productsJSON.getInt(jsonObjeto.getString("Product_ID")))
                        productos.put(jsonObjeto)
                    }
                    orderInfo = response
                    Log.d(ContentValues.TAG, "Response is: $orderInfo")
                    callback(response,productos)
                }

            },
            { error ->
                // Errors here
                Log.e(ContentValues.TAG, "Error occurred", error)
            }
        )
        queue.add(jsonObject)
    }
    private fun getInfoList(productIds: String, callback: (JSONArray) -> Unit) {
        Log.d(ContentValues.TAG, "Response is: $productIds")
        val queue = Volley.newRequestQueue(context)
        val jsonObject = JsonArrayRequest(
            //http get about the products with the specified product IDs
            "http://52.4.150.68/api/getProduct?ProductID=$productIds",
            { response ->
                //sends the acquired jsonArray back to a callback
                productArray = response
                callback(response)
            },
            { error ->
                // Errors here
                Log.e(ContentValues.TAG, "Error occurred", error)
            }
        )
        queue.add(jsonObject)
    }
}
