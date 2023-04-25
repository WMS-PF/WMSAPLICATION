package com.finalproyect.wmsaplication.ui.home

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.finalproyect.wmsaplication.databinding.FragmentHomeBinding
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        //val homeViewModel =
        //    ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //val textView: TextView = binding.textHome
        //homeViewModel.text.observe(viewLifecycleOwner) {
        //    textView.text = it
        //}
        val scanButton = binding.buttonScan
        scanButton.setOnClickListener {_: View? ->scanFromFragment() }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private val fragmentLauncher = registerForActivityResult(
        ScanContract()
    ) { result: ScanIntentResult ->
        if (result.contents == null) {
            Toast.makeText(context, "Cancelled from fragment", Toast.LENGTH_LONG).show()

        } else {
            Toast.makeText(
                context,
                "Scanned from fragment: " + result.contents,
                Toast.LENGTH_LONG
            ).show()
            getInfo(result.contents)
        }
    }
    private fun scanFromFragment() {
        val options = ScanOptions()
        options.setOrientationLocked(true)
        options.setBeepEnabled(true)
        fragmentLauncher.launch(options)
    }

    private fun getInfo(productId: String){
        val queue = Volley.newRequestQueue(context)
        val jsonObject = JsonArrayRequest(
            "http://191.109.26.82:3000/api/getProduct?productID=$productId",
            { response ->
                val jsonObject = response.getJSONObject(0)
                // Process the JSON object here
                // Success
                Log.d(TAG, "Response is: $jsonObject")
                // JSON Data
                val productName = jsonObject.getString("Product_Name")
                val productID = jsonObject.getString("Product_ID")
                val weight = jsonObject.getString("Weight").toDouble()
                val length = jsonObject.getString("Length").toDouble()
                val width = jsonObject.getString("Width").toDouble()
                val height = jsonObject.getString("Height").toDouble()
                val brand = jsonObject.getString("Brand")

                val formattedWeight = String.format("%.3f", weight)
                val formattedLength = String.format("%.3f", length)
                val formattedWidth = String.format("%.3f", width)
                val formattedHeight = String.format("%.3f", height)
                val volume = formattedLength+'x'+formattedWidth+'x'+formattedHeight
                // bind TextViews
                val productIdTextView = binding.productID
                val productNameTextView = binding.productName
                val weightTextView = binding.weight
                val volumeTextView = binding.volume
                val brandTextView = binding.brand
                // Update TextViews
                productIdTextView.setText(productID)
                productNameTextView.setText(productName)
                weightTextView.setText(formattedWeight)
                volumeTextView.text = volume
                brandTextView.setText(brand)

            },
            { error ->
                // Errors here
                Log.e(TAG, "Error occurred", error)
            }
        )
        queue.add(jsonObject)
    }
}