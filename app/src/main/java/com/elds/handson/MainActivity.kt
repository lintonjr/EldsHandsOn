package com.elds.handson

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import edmt.dev.edmtdevcognitivevision.Contract.AnalysisResult
import edmt.dev.edmtdevcognitivevision.VisionServiceClient
import edmt.dev.edmtdevcognitivevision.VisionServiceRestClient
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream


class MainActivity : AppCompatActivity() {

    private lateinit var imgView: ImageView
    private lateinit var txtResult: TextView
    private lateinit var btnPick: Button
    private val pickImage = 100
    private var imageUri: Uri? = null
    private lateinit var progressBar: ProgressBar

    companion object {
        val API_KEY="ba0fcd2b7dc94b539f904bf27e8e78d0"
        val API_LINK="https://eldshandson.cognitiveservices.azure.com/vision/v1.0"
    }

    internal var visionServiceClient: VisionServiceClient = VisionServiceRestClient(API_KEY,
        API_LINK)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnPick = findViewById(R.id.btn_pick)
        btnPick.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, pickImage)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == pickImage) {
            imageUri = data?.data

            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
//            val bitmap = BitmapFactory.decodeResource(applicationContext.resources, bit)
            imgView = findViewById(R.id.img_view)
            imgView.setImageBitmap(bitmap)

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            val inputStream = ByteArrayInputStream(outputStream.toByteArray())
            var progressDialog = ProgressDialog(this@MainActivity)
            val visionTask = @SuppressLint("StaticFieldLeak")
            object: AsyncTask<InputStream, String, String>() {

                override fun onPreExecute() {
                    progressDialog.show()
                }

                override fun onProgressUpdate(vararg values: String?) {
                    progressDialog.setMessage(values[0])
                }

                override fun doInBackground(vararg params: InputStream?): String {
                    try {
                        publishProgress("Recognizing...")
                        val features = arrayOf("Description")
                        val details = arrayOf<String>()

                        val result = visionServiceClient.analyzeImage(params[0], features, details)

                        return Gson().toJson(result)
                    } catch (e: Exception) {
                        return ""
                    }
                }

                override fun onPostExecute(result: String?) {
                    progressDialog.dismiss()

                    val result = Gson().fromJson<AnalysisResult>(result, AnalysisResult::class.java)
                    val resultText = StringBuilder()
                    for (caption in result.description.captions) {
                        resultText.append(caption.text)
                        txtResult = findViewById(R.id.txt_result)
                        txtResult.text = resultText.toString()
                    }
                }
            }
            visionTask.execute(inputStream)
        }
    }
}

