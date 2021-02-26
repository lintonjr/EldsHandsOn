package com.elds.handson

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.gson.Gson
import dmax.dialog.SpotsDialog
import edmt.dev.edmtdevcognitivevision.Contract.CelebritiesCategory
import edmt.dev.edmtdevcognitivevision.Contract.CelebritiesResult
import edmt.dev.edmtdevcognitivevision.VisionServiceRestClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

private const val PICK_IMAGE = 1
private const val API_KEY = "ba0fcd2b7dc94b539f904bf27e8e78d0"
private const val API_LINK = "https://eldshandson.cognitiveservices.azure.com/vision/v2.0"

class CelebrityActivity : AppCompatActivity() {
    private lateinit var imgV: ImageView
    private lateinit var txtR: TextView
    private lateinit var btnDetect: Button
    private var UriImage: Uri? = null
    private val vService = VisionServiceRestClient(API_KEY, API_LINK)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_celebrity)

        supportActionBar?.title = "Celebrity Detect";

        imgV = findViewById(R.id.img_v)
        btnDetect = findViewById(R.id.btn_detect)
        txtR = findViewById(R.id.txt_desc)

        btnDetect.setOnClickListener {
            val gallery = Intent()
            gallery.type = "image/*"
            gallery.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(gallery, PICK_IMAGE)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            UriImage = data?.data
            imgV.setImageURI(UriImage)

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, UriImage)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            val inputStream = ByteArrayInputStream(outputStream.toByteArray())

            val dialog = SpotsDialog.Builder().setContext(this@CelebrityActivity)
                .build()
            dialog.show()
            dialog.setMessage("Detecting")

            CoroutineScope(Dispatchers.IO).launch {
                val celebritiesResult = vService.detectCelebrities(inputStream)
                val gson = Gson()
                val resultGson = gson.fromJson(gson.toJson(celebritiesResult), CelebritiesResult::class.java)
                var resultText = ""
                var ff: CelebritiesCategory = CelebritiesCategory()
                for (category in resultGson.categories) {
                    ff = category
                }
                for (celebrity in ff.detail.celebrities) {
                    resultText += celebrity.name
                }
                runOnUiThread { txtR.text = resultText }
            }.invokeOnCompletion {
                dialog.dismiss()
            }
        }
    }
}