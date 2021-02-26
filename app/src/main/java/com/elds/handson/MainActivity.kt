package com.elds.handson

import android.app.ProgressDialog
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.graphics.Bitmap.CompressFormat.JPEG
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import edmt.dev.edmtdevcognitivevision.Contract.AnalysisResult
import edmt.dev.edmtdevcognitivevision.VisionServiceRestClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

private const val PICK_IMAGE = 1
private const val API_KEY = "ba0fcd2b7dc94b539f904bf27e8e78d0"
private const val API_LINK = "https://eldshandson.cognitiveservices.azure.com/vision/v1.0"

class MainActivity : AppCompatActivity() {

    private lateinit var imgView: ImageView
    private lateinit var txtResult: TextView
    private lateinit var btnPick: Button
    private var imageUri: Uri? = null
    private val visionService = VisionServiceRestClient(API_KEY, API_LINK)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.title = "Image Detect";
        txtResult = findViewById(R.id.txt_result)
        imgView = findViewById(R.id.img_view)
        btnPick = findViewById(R.id.btn_pick)

        btnPick.setOnClickListener {
            val gallery = Intent()
            gallery.type = "image/*"
            gallery.action = ACTION_GET_CONTENT
            startActivityForResult(gallery, PICK_IMAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            imageUri = data?.data
            imgView.setImageURI(imageUri)

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(JPEG, 100, outputStream)
            val inputStream = ByteArrayInputStream(outputStream.toByteArray())

            val progressDialog = ProgressDialog(this@MainActivity)
            progressDialog.show()
            progressDialog.setMessage("Recognizing..")

            CoroutineScope(IO).launch {
                val result = visionService.analyzeImage(
                    inputStream, arrayOf("Description"), arrayOf<String>()
                )
                val gson = Gson()
                val resultGson = gson.fromJson(gson.toJson(result), AnalysisResult::class.java)
                var resultText = ""
                for (caption in resultGson.description.captions) {
                    resultText += caption.text
                }
                runOnUiThread { txtResult.text = resultText }
            }.invokeOnCompletion {
                progressDialog.dismiss()
            }
        }
    }
}