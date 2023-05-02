package com.finalproyect.wmsaplication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognizerIntent.EXTRA_RESULTS
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.finalproyect.wmsaplication.ui.gallery.GalleryViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.BeepManager
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import org.json.JSONObject
import java.util.Arrays

/**
 * This sample performs continuous scanning, displaying the barcode and source image whenever
 * a barcode is scanned.
 */
class ContinuousCaptureActivity : Activity() {

    private var barcodeView: DecoratedBarcodeView? = null
    private var beepManager: BeepManager? = null
    companion object {
        const val EXTRA_RESULT = "result_scan_continuous"
        const val EXTRA_SCANNED_RESULTS = "scanned_results"
        private const val STATE_SCANNED_RESULTS = "state_scanned_results"
    }
    private lateinit var viewModel: GalleryViewModel

    private var scannedResults: MutableList<String> = mutableListOf()
    private val callback: BarcodeCallback = object : BarcodeCallback {
        private var lastScanTime = 0L
        private val SCAN_INTERVAL = 1300L // 3 segundos

        override fun barcodeResult(result: BarcodeResult) {


            if (result.text.isNullOrEmpty() && result.text == scannedResults[scannedResults.lastIndex]) {
                return
            }

            // Comprobar si ha pasado suficiente tiempo desde el último escaneo
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastScanTime < SCAN_INTERVAL) {
                return
            }
            // Verificar la longitud del resultado escaneado
            if (result.text.length != 18) {
                // Si el resultado no tiene exactamente 18 caracteres, no lo agregue a la lista de resultados
                barcodeView!!.setStatusText("El código escaneado no es valido")
                return
            }
            // Obtener el objeto JSONObject con la información del pedido
            val orderInfoJsonString = intent.getStringExtra("ORDER_INFO")
            val orderInfo = orderInfoJsonString?.let { JSONObject(it) }
            // Obtener el JSONArray de productos
            val productObject = orderInfo?.getJSONObject("Products")
            val iterator = productObject?.keys()

            var isCodeValid = false
            var correspondingQuantity = 0 // Almacena la cantidad del producto correspondiente a result.text

            if (iterator != null) {
                while (iterator.hasNext()) {
                    val productId = iterator.next() as String
                    val quantity = productObject.getInt(productId)

                    if (productId == result.text) {
                        isCodeValid = true
                        correspondingQuantity = quantity // Almacena la cantidad del producto correspondiente a result.text
                        break
                    }
                }

                // Mueve la verificación del conteo fuera del bucle while
                if (isCodeValid && scannedResults.count { it == result.text } >= correspondingQuantity) {
                    // Si el código escaneado ya se encuentra en la lista igual o más de la cantidad permitida, no agregarlo
                    barcodeView!!.setStatusText("El código escaneado ya se encuentra en la lista igual o más de la cantidad permitida")
                    isCodeValid = false
                }

                if (!isCodeValid) {
                    return
                }
            }

            if (isCodeValid) {
            // Escanear el código
            scannedResults.add(result.text)
            intent.putExtra("Scanned", scannedResults.toTypedArray())
            Log.d("ScanTag", "resultado =$scannedResults")
            barcodeView!!.setStatusText(result.text)
            //beepManager!!.playBeepSoundAndVibrate()

            // Pausar el escaneo durante un tiempo
            barcodeView!!.pause()
            Handler(Looper.getMainLooper()).postDelayed({
                // Reanudar el escaneo después de la pausa
                barcodeView!!.resume()
                lastScanTime = System.currentTimeMillis()

            }, SCAN_INTERVAL)
            } else {
                // Si el productID no se encuentra en el JSONArray de productos, mostrar un mensaje de error
                Toast.makeText(this@ContinuousCaptureActivity, "Código no válido", Toast.LENGTH_SHORT).show()
            }
        }

        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
    }
    override fun onBackPressed() {
        val intent = Intent().apply {
            putExtra("Scanned", scannedResults.toTypedArray())
        }
        setResult(Activity.RESULT_OK, intent)
        super.onBackPressed()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.continuous_scan)
        barcodeView = findViewById<DecoratedBarcodeView>(R.id.barcode_scanner)
        val scannedResultsArray = intent.getStringArrayExtra("Scanned")
        if (scannedResultsArray != null) {
            scannedResults.addAll(scannedResultsArray)
        }
        // Obtener la lista de resultados escaneados del intent

// Inicializar la lista local de resultados escaneados con los valores del intent
        val formats: Collection<BarcodeFormat> =
            Arrays.asList(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39)
        barcodeView!!.getBarcodeView().decoderFactory = DefaultDecoderFactory(formats)
        barcodeView!!.initializeFromIntent(intent)
        barcodeView?.decodeContinuous(callback)
        beepManager = BeepManager(this)
    }

    override fun onResume() {
        super.onResume()
        barcodeView!!.resume()
    }

    override fun onPause() {
        super.onPause()
        barcodeView!!.pause()
    }

    fun pause(view: View?) {
        barcodeView!!.pause()
    }

    fun resume(view: View?) {
        barcodeView!!.resume()
    }

    fun triggerScan(view: View?) {
        barcodeView?.decodeSingle(callback)
    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return barcodeView!!.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }
}
