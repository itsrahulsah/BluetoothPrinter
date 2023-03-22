package com.techno.tron.bluetoothprinter

import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.TextRecognizerOptionsInterface
import com.google.mlkit.vision.text.latin.TextRecognizerOptions


class MainActivity : AppCompatActivity() {
    private var editText:EditText? = null
    private var imageView:ImageView? = null
    private var textRecognizer: TextRecognizer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        editText = findViewById(R.id.editText)
        imageView = findViewById(R.id.image)
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        when (intent?.action) {
            Intent.ACTION_SEND -> {
                if ("text/plain" == intent.type) {
                    handleSendText(intent) // Handle text being sent
                } else if (intent.type?.startsWith("image/") == true) {
                    handleSendImage(intent) // Handle single image being sent
                }
            }
            else -> {
                // Handle other intents, such as being started from the home screen
            }
        }
    }

    private fun handleSendText(intent: Intent) {
        intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
            editText?.visibility = View.VISIBLE
            editText?.setText(it)
        }
    }

    private fun handleSendImage(intent: Intent) {
        (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(this.contentResolver, it))
            } else {
                MediaStore.Images.Media.getBitmap(this.contentResolver, it)
            }
            imageView?.visibility = View.VISIBLE
            imageView?.setImageBitmap(bitmap)

            textRecognizer?.process(InputImage.fromBitmap(bitmap, 0))
                ?.addOnSuccessListener {
                    imageView?.visibility  = View.INVISIBLE
                    editText?.setText(it.text)
                    editText?.visibility = View.VISIBLE
                }?.addOnFailureListener{
                    Log.e("TAG","err : $it")
                }
        }
    }

    private fun print(text:String){
        
    }
}