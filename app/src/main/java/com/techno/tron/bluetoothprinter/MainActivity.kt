package com.techno.tron.bluetoothprinter

import BpPrinter.mylibrary.*
import android.Manifest.permission.*
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.IOException

@RequiresApi(Build.VERSION_CODES.S)
class MainActivity : AppCompatActivity(),CardScanner,Scrybe{
    private var bitmap: Bitmap? = null
    private var editText:EditText? = null
    private var imageView:ImageView? = null
    private var btnConvert:Button? = null
    private var btnPrint:Button? = null
    private var textRecognizer: TextRecognizer? = null
    private var printer:BpPrinter? = null
    private lateinit var printerDevice:BluetoothConnectivity
    private val INITIAL_REQUEST = 1337
    private val key = "deviceName"
    private val  permissions = listOf(
        BLUETOOTH_SCAN,
        BLUETOOTH,
        BLUETOOTH_CONNECT,
        BLUETOOTH_ADVERTISE,
        BLUETOOTH_ADMIN
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        editText = findViewById(R.id.editText)
        imageView = findViewById(R.id.image)
        btnPrint = findViewById(R.id.btn_print)
        btnConvert = findViewById(R.id.btn_conver_to_text)
        ActivityCompat.requestPermissions(this, permissions.toTypedArray(), INITIAL_REQUEST)
        printerDevice = BluetoothConnectivity(this)
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        btnConvert?.setOnClickListener{
            if(bitmap != null) {
                textRecognizer?.process(InputImage.fromBitmap(bitmap!!, 0))
                    ?.addOnSuccessListener {
                        imageView?.visibility = View.INVISIBLE
                        editText?.setText(it.text)
                        editText?.visibility = View.VISIBLE
                    }?.addOnFailureListener {
                        Log.e("TAG", "err : $it")
                        Toast.makeText(applicationContext,"err: $it",Toast.LENGTH_SHORT).show()
                    }
            }
        }
        btnPrint?.setOnClickListener{
            onPrintButtonClick()
        }
    }

    override fun onResume() {
        super.onResume()
        when (intent?.action) {
            Intent.ACTION_SEND -> {
                if ("text/plain" == intent.type) {
                    handleSendText(intent) // Handle text being sent
                } else if (intent.type?.startsWith("image/") == true) {
                    handleSendImage(intent) // Handle single image being sent
                }
            }
        }
    }

    private fun handleSendText(intent: Intent) {
        intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
            editText?.visibility = View.VISIBLE
            imageView?.visibility = View.GONE
            editText?.setText(it)
        }
    }

    private fun handleSendImage(intent: Intent) {
        editText?.visibility = View.GONE
        val uri = when{
            Build.VERSION.SDK_INT>= 33 -> intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)

            else -> @Suppress("DEPRECATION") intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri
        }
        uri?.let {
             this.bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(this.contentResolver, it))
            } else {
                 @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(this.contentResolver, it)
            }
            btnConvert?.visibility  = View.VISIBLE
            imageView?.visibility = View.VISIBLE
            imageView?.setImageBitmap(bitmap!!)
        }
    }

    override fun onScanMSR(p0: String?, p1: CardReader.CARD_TRACK?) {

    }

    override fun onScanDLCard(p0: String?) {
    }

    override fun onScanRCCard(p0: String?) {
    }

    override fun onScanRFD(p0: String?) {
    }

    override fun onScanPacket(p0: String?) {
    }

    override fun onScanFwUpdateRespPacket(p0: ByteArray?) {
    }

    override fun onDiscoveryComplete(p0: ArrayList<String>?) {
    }

    private fun onPrintButtonClick(){
        var printerName =""
        if(getConnectedDeviceName().isNullOrBlank()){
            try {
                val dialog = PairDeviceFragment(printerDevice.pairedPrinters as ArrayList<String>) {
                    printerName = it
                    if (connectPrinter(it)) {
                        setDeviceName(it)
                        printerDevice.getCardReader(this)
                        printer = printerDevice.aemPrinter
                        btPrint()
                    }else{
                        Toast.makeText(baseContext,
                            "Not Connected\n$printerName is unreachable or off otherwise it is connected with other device", Toast.LENGTH_SHORT).show()
                    }
                }
                dialog.show(supportFragmentManager, "Select Printer Fragment")

            }catch (e:IOException) {
                if (e.message?.contains("Service discovery failed") == true) {
                    Toast.makeText(baseContext,
                        "Not Connected\n$printerName is unreachable or off otherwise it is connected with other device", Toast.LENGTH_SHORT).show()
                } else if (e.message?.contains("Device or resource busy") == true) {
                    Toast.makeText(baseContext, "the device is already connected", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(baseContext, "Unable to connect", Toast.LENGTH_SHORT).show()
                }
            }
        }else{
            if (!connectPrinter(getConnectedDeviceName()!!)) {
                removeDeviceName()
                onPrintButtonClick()
            }else{
                printerDevice.getCardReader(this)
                printer = printerDevice.aemPrinter
                btPrint()
            }
        }
    }

    private fun removeDeviceName() {
        val editor = getSharedPreferences("Device", MODE_PRIVATE).edit()
        editor.remove(key)
        editor.apply()
    }

    private fun btPrint(){
        val greeting = "\n\nThank You visit again... \nAryan Enterprisess"
        try {
            if(printer != null){
                val text = editText?.text.toString() + greeting
                printer!!.print(text)
                printer!!.setCarriageReturn()
                printer!!.setCarriageReturn()
                printer!!.setCarriageReturn()
                printer!!.setCarriageReturn()
            }
        }catch (e:IOException){
            Toast.makeText(baseContext,e.message, Toast.LENGTH_SHORT).show() 
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        printerDevice.disConnectPrinter()
        finish()
    }

    private fun getConnectedDeviceName():String?{
        val sf = getSharedPreferences("Device", MODE_PRIVATE)
        return if(sf.contains(key)){
             sf.getString(key,"")
        }else{
            null
        }
    }

    private fun setDeviceName(deviceName:String){
        val editor = getSharedPreferences("Device", MODE_PRIVATE).edit()
        editor.putString("deviceName",deviceName)
        editor.apply()
    }


    private fun connectPrinter(deviceName: String):Boolean{
       return try {
            printerDevice.connectToPrinter(deviceName)

        }catch (e:IOException) {
            if (e.message?.contains("Service discovery failed") == true) {
                Toast.makeText(
                    baseContext,
                    "Not Connected\n$deviceName is unreachable or off otherwise it is connected with other device",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (e.message?.contains("Device or resource busy") == true) {
                Toast.makeText(
                    baseContext,
                    "the device is already connected",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(baseContext, "Unable to connect", Toast.LENGTH_SHORT)
                    .show()
            }
           false
        }
    }
}